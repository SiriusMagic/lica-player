package com.sren.music.ui.screens.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Switch
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.sren.music.suggestion.SuggestionScheduler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sren.music.R
import com.sren.music.data.settings.AudioProfile
import com.sren.music.data.settings.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: AppSettings,
    private val scheduler: SuggestionScheduler,
) : ViewModel() {
    val profile: StateFlow<AudioProfile> = settings.profile
    val dailySuggestion: StateFlow<Boolean> = settings.dailySuggestion

    fun select(profile: AudioProfile) = settings.setProfile(profile)

    /** True the first time only: the caller may show the notification prompt now. */
    fun claimNotificationPrompt(): Boolean {
        if (settings.notificationPermissionAsked) return false
        settings.notificationPermissionAsked = true
        return true
    }

    fun setDailySuggestion(enabled: Boolean) {
        settings.setDailySuggestion(enabled)
        scheduler.sync()
    }
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val selected by viewModel.profile.collectAsStateWithLifecycle()
    val dailySuggestion by viewModel.dailySuggestion.collectAsStateWithLifecycle()
    val context = LocalContext.current
    // Turning the suggestion on without notification access: ask once, or open the
    // system screen if Android no longer shows the prompt.
    val askPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (!ok) context.openNotificationSettings()
    }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
    ) {
        SectionTitle(R.string.settings_audio_profile)
        Column(Modifier.selectableGroup()) {
            AudioProfile.entries.forEach { profile ->
                ProfileRow(
                    title = profile.titleRes,
                    body = profile.bodyRes,
                    selected = profile == selected,
                    onClick = { viewModel.select(profile) },
                )
            }
        }

        SectionTitle(R.string.settings_advanced)
        SwitchRow(
            title = R.string.setting_daily_suggestion,
            body = R.string.setting_daily_suggestion_body,
            checked = dailySuggestion,
            onChange = { on ->
                viewModel.setDailySuggestion(on)
                if (on && !context.notificationsAllowed()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        askPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        context.openNotificationSettings()
                    }
                }
            },
        )
    }
}

@Composable
private fun SectionTitle(@StringRes text: Int) {
    Text(
        stringResource(text),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun SwitchRow(@StringRes title: Int, @StringRes body: Int, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .toggleable(value = checked, onValueChange = onChange, role = Role.Switch)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 16.dp)) {
            Text(stringResource(title), style = MaterialTheme.typography.bodyLarge)
            Text(
                stringResource(body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}

private fun Context.notificationsAllowed(): Boolean =
    (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED) &&
        NotificationManagerCompat.from(this).areNotificationsEnabled()

private fun Context.openNotificationSettings() {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
    }
    runCatching { startActivity(intent) }
}

@Composable
private fun ProfileRow(@StringRes title: Int, @StringRes body: Int, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        RadioButton(selected = selected, onClick = null)
        Column(Modifier.padding(start = 16.dp)) {
            Text(stringResource(title), style = MaterialTheme.typography.bodyLarge)
            Text(
                stringResource(body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val AudioProfile.titleRes: Int
    get() = when (this) {
        AudioProfile.LONG_LIFE -> R.string.profile_long_life
        AudioProfile.BALANCED -> R.string.profile_balanced
        AudioProfile.HIGH_FIDELITY -> R.string.profile_high_fidelity
    }

private val AudioProfile.bodyRes: Int
    get() = when (this) {
        AudioProfile.LONG_LIFE -> R.string.profile_long_life_body
        AudioProfile.BALANCED -> R.string.profile_balanced_body
        AudioProfile.HIGH_FIDELITY -> R.string.profile_high_fidelity_body
    }
