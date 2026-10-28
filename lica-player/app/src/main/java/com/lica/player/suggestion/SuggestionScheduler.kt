package com.lica.player.suggestion

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lica.player.data.settings.AppSettings
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * One inexact, non-wakeup alarm a day, around 6 p.m. "Non-wakeup" (RTC, not
 * RTC_WAKEUP): if the phone is asleep, Android delivers it the next time the
 * phone wakes for any other reason. The app never wakes the phone just for this.
 * No WorkManager: one system alarm costs no library and no background process.
 */
@Singleton
class SuggestionScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: AppSettings,
) {
    /** Schedules or cancels to match the setting. Cheap and idempotent: called on every app start. */
    fun sync() {
        val alarms = context.getSystemService(AlarmManager::class.java) ?: return
        if (settings.dailySuggestion.value) {
            alarms.setInexactRepeating(AlarmManager.RTC, nextSixPm(), AlarmManager.INTERVAL_DAY, alarmIntent())
        } else {
            alarms.cancel(alarmIntent())
        }
    }

    private fun alarmIntent(): PendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, SuggestionReceiver::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    private fun nextSixPm(): Long {
        val now = Calendar.getInstance()
        val at = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, HOUR)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!at.after(now)) at.add(Calendar.DAY_OF_YEAR, 1)
        return at.timeInMillis
    }

    private companion object {
        const val HOUR = 18
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SuggestionEntryPoint {
    fun notifier(): SuggestionNotifier
    fun scheduler(): SuggestionScheduler
}

/** Fired by the daily alarm. Not exported: only this app's alarm can trigger it. */
class SuggestionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notifier = EntryPointAccessors
            .fromApplication(context, SuggestionEntryPoint::class.java)
            .notifier()
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                notifier.showDaily()
            } finally {
                pending.finish()
            }
        }
    }
}

/** Alarms are cleared on reboot; this puts the daily one back. Does nothing else. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        EntryPointAccessors
            .fromApplication(context, SuggestionEntryPoint::class.java)
            .scheduler()
            .sync()
    }
}
