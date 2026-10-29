package com.sren.music.data.settings

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User preferences. SharedPreferences (no DataStore dependency): one small
 * XML file, read once at startup. Shared by the UI, the playback service and
 * the suggestion receiver, which all run in the same process.
 */
@Singleton
class AppSettings @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences("playback_settings", Context.MODE_PRIVATE)

    private val _profile = MutableStateFlow(readProfile())
    val profile: StateFlow<AudioProfile> = _profile.asStateFlow()

    private val _dailySuggestion = MutableStateFlow(prefs.getBoolean(KEY_DAILY_SUGGESTION, true))
    val dailySuggestion: StateFlow<Boolean> = _dailySuggestion.asStateFlow()

    /** The last song suggested, so the next day suggests a different one. */
    var lastSuggestedSongId: Long
        get() = prefs.getLong(KEY_LAST_SUGGESTION, -1L)
        set(value) = prefs.edit().putLong(KEY_LAST_SUGGESTION, value).apply()

    /** Android 13+: the notification permission is requested once, on first launch. */
    var notificationPermissionAsked: Boolean
        get() = prefs.getBoolean(KEY_NOTIF_ASKED, false)
        set(value) = prefs.edit().putBoolean(KEY_NOTIF_ASKED, value).apply()

    fun setProfile(profile: AudioProfile) {
        if (_profile.value == profile) return
        prefs.edit().putString(KEY_PROFILE, profile.name).apply()
        _profile.value = profile
    }

    fun setDailySuggestion(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DAILY_SUGGESTION, enabled).apply()
        _dailySuggestion.value = enabled
    }

    private fun readProfile(): AudioProfile =
        prefs.getString(KEY_PROFILE, null)
            ?.let { name -> AudioProfile.entries.firstOrNull { it.name == name } }
            ?: AudioProfile.DEFAULT

    private companion object {
        const val KEY_PROFILE = "audio_profile"
        const val KEY_DAILY_SUGGESTION = "daily_suggestion"
        const val KEY_LAST_SUGGESTION = "last_suggested_song"
        const val KEY_NOTIF_ASKED = "notification_permission_asked"
    }
}
