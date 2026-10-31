package com.zirakusa.stone.ui.screens.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Switch
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.zirakusa.stone.suggestion.SuggestionScheduler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.app.Activity
import com.zirakusa.stone.BuildConfig
import com.zirakusa.stone.R
import com.zirakusa.stone.core.locale.AppLanguage
import com.zirakusa.stone.data.settings.AppSettings
import com.zirakusa.stone.data.settings.AudioProfile
import com.zirakusa.stone.ui.theme.StoneBackground
import com.zirakusa.stone.ui.theme.StoneSurface
import com.zirakusa.stone.ui.theme.StoneSurfaceHighlight
import com.zirakusa.stone.ui.theme.StoneTextSecondary
import com.zirakusa.stone.ui.theme.StoneYellow
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
    val gapless: StateFlow<Boolean> = settings.gapless
    val defaultSpeed: StateFlow<Float> = settings.defaultSpeed

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

    fun setGapless(enabled: Boolean) = settings.setGapless(enabled)

    fun setDefaultSpeed(speed: Float) = settings.setDefaultSpeed(speed)
}

private val CARD_SHAPE = RoundedCornerShape(16.dp)
private val SPEEDS = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)

private fun formatSpeed(speed: Float): String =
    if (speed % 1f == 0f) "${speed.toInt()}x" else "${speed}x"

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onRescan: () -> Unit = {},
    onOpenPlayer: () -> Unit = {},
) {
    val selected by viewModel.profile.collectAsStateWithLifecycle()
    val dailySuggestion by viewModel.dailySuggestion.collectAsStateWithLifecycle()
    val gapless by viewModel.gapless.collectAsStateWithLifecycle()
    val defaultSpeed by viewModel.defaultSpeed.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showSpeedSheet by remember { mutableStateOf(false) }

    val askPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (!ok) context.openNotificationSettings()
    }

    // Message for the "open player first" toast, captured here so it can be
    // used inside a lambda without capturing context directly.
    val openPlayerFirst = stringResource(R.string.setting_sleep_timer_open_player)

    Column(
        Modifier
            .fillMaxSize()
            .background(StoneBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        // ── Sección: Audio y batería ──────────────────────────────────────────
        SectionTitle(R.string.settings_audio_profile)
        SettingsCard {
            Column(Modifier.selectableGroup()) {
                AudioProfile.entries.forEachIndexed { index, profile ->
                    ProfileRow(
                        title = profile.titleRes,
                        body = profile.bodyRes,
                        selected = profile == selected,
                        showDivider = index < AudioProfile.entries.size - 1,
                        onClick = { viewModel.select(profile) },
                    )
                }
            }
        }

        // ── Sección: Biblioteca ───────────────────────────────────────────────
        SectionTitle(R.string.settings_library)
        SettingsCard {
            TapRow(
                title = R.string.setting_rescan,
                showDivider = true,
                onClick = onRescan,
            )
            TapRow(
                title = R.string.setting_sleep_timer,
                showDivider = false,
                onClick = {
                    // The sleep timer lives inside the player. If it is not
                    // open yet we show a toast so the user knows why.
                    Toast.makeText(context, openPlayerFirst, Toast.LENGTH_SHORT).show()
                    onOpenPlayer()
                },
            )
        }

        // ── Sección: Reproducción ─────────────────────────────────────────────
        SectionTitle(R.string.settings_playback)
        SettingsCard {
            SwitchRow(
                title = R.string.setting_gapless,
                body = R.string.setting_gapless_body,
                checked = gapless,
                showDivider = true,
                onChange = { viewModel.setGapless(it) },
            )
            ActionRow(
                title = R.string.setting_default_speed,
                meta = formatSpeed(defaultSpeed),
                showDivider = false,
                onClick = { showSpeedSheet = true },
            )
        }

        // ── Sección: Preferencias ─────────────────────────────────────────────
        SectionTitle(R.string.settings_preferences)
        SettingsCard {
            TapRow(
                title = R.string.setting_notifications,
                showDivider = false,
                onClick = { context.openNotificationSettings() },
            )
        }

        // Idioma: los 6 idiomas de Stone, guardados en los ajustes de la app (no en los del sistema).
        LanguageSection()

        // ── Sección: Control avanzado ─────────────────────────────────────────
        SectionTitle(R.string.settings_advanced)
        SettingsCard {
            SwitchRow(
                title = R.string.setting_daily_suggestion,
                body = R.string.setting_daily_suggestion_body,
                checked = dailySuggestion,
                showDivider = false,
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

        // ── Sección: Acerca de ────────────────────────────────────────────────
        SectionTitle(R.string.settings_about)
        SettingsCard {
            InfoRow(
                title = R.string.setting_version,
                meta = BuildConfig.VERSION_NAME,
                showDivider = true,
            )
            TapRow(
                title = R.string.setting_send_feedback,
                showDivider = false,
                onClick = {
                    runCatching {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/SiriusMagic/lica-player/issues"),
                            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    }
                },
            )
        }
    }

    if (showSpeedSheet) {
        SpeedSheet(
            current = defaultSpeed,
            onPick = { viewModel.setDefaultSpeed(it); showSpeedSheet = false },
            onDismiss = { showSpeedSheet = false },
        )
    }
}

/** Idioma (Ajustes de Stone): los 6 idiomas; al cambiarlo la pantalla se vuelve a abrir traducida. */
@Composable
private fun LanguageSection() {
    val context = LocalContext.current
    val current = AppLanguage.current(context)
    SectionTitle(R.string.settings_language)
    Column(Modifier.selectableGroup()) {
        AppLanguage.all.forEach { (code, label) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = code == current,
                        role = Role.RadioButton,
                        onClick = {
                            if (code != current) {
                                AppLanguage.save(context, code)
                                (context as? Activity)?.recreate()
                            }
                        },
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = code == current, onClick = null)
                Text(label, Modifier.padding(start = 16.dp), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

// ── Card container ────────────────────────────────────────────────────────────

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    // Stone 1.3.2: fondo StoneSurface, borde StoneSurfaceHighlight, cornerRadius 16dp
    Column(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(CARD_SHAPE)
            .background(StoneSurface, CARD_SHAPE)
            .border(1.dp, StoneSurfaceHighlight, CARD_SHAPE),
    ) {
        content()
    }
}

// ── Section title ─────────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(@StringRes text: Int) {
    Text(
        stringResource(text),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 16.dp, bottom = 8.dp),
    )
}

// ── Row: Switch ───────────────────────────────────────────────────────────────

@Composable
private fun SwitchRow(
    @StringRes title: Int,
    @StringRes body: Int,
    checked: Boolean,
    showDivider: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(value = checked, onValueChange = onChange, role = Role.Switch)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 16.dp)) {
            Text(
                stringResource(title),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
            )
            Text(
                stringResource(body),
                style = MaterialTheme.typography.bodyMedium,
                color = StoneTextSecondary,
            )
        }
        // Stone: thumb blanco, track StoneYellow cuando ON
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = StoneYellow,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = StoneSurfaceHighlight,
                uncheckedBorderColor = StoneSurfaceHighlight,
            ),
        )
    }
    if (showDivider) RowDivider()
}

// ── Row: tappable with meta text (navigation arrow optional) ──────────────────

@Composable
private fun ActionRow(
    @StringRes title: Int,
    meta: String,
    showDivider: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(title),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            modifier = Modifier.weight(1f),
        )
        Text(
            meta,
            style = MaterialTheme.typography.bodyMedium,
            color = StoneYellow,
        )
    }
    if (showDivider) RowDivider()
}

// ── Row: tappable, title only (no meta) ──────────────────────────────────────

@Composable
private fun TapRow(
    @StringRes title: Int,
    showDivider: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(title),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            modifier = Modifier.weight(1f),
        )
    }
    if (showDivider) RowDivider()
}

// ── Row: static info (no interaction) ────────────────────────────────────────

@Composable
private fun InfoRow(@StringRes title: Int, meta: String, showDivider: Boolean = false) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(title),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            modifier = Modifier.weight(1f),
        )
        Text(
            meta,
            style = MaterialTheme.typography.bodyMedium,
            color = StoneTextSecondary,
        )
    }
    if (showDivider) RowDivider()
}

// ── Row: radio button (audio profile) ────────────────────────────────────────

@Composable
private fun ProfileRow(
    @StringRes title: Int,
    @StringRes body: Int,
    selected: Boolean,
    showDivider: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        RadioButton(selected = selected, onClick = null)
        Column(Modifier.padding(start = 16.dp)) {
            Text(
                stringResource(title),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
            )
            Text(
                stringResource(body),
                style = MaterialTheme.typography.bodyMedium,
                color = StoneTextSecondary,
            )
        }
    }
    if (showDivider) RowDivider()
}

// ── Thin divider between rows inside a card ───────────────────────────────────

@Composable
private fun RowDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(1.dp)
            .background(StoneSurfaceHighlight),
    )
}

// ── Speed bottom sheet (Stone style) ─────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpeedSheet(current: Float, onPick: (Float) -> Unit, onDismiss: () -> Unit) {
    // Stone 1.3.2: fondo StoneSurface, handle StoneSurfaceHighlight
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = StoneSurface,
        dragHandle = {
            Box(
                Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .background(StoneSurfaceHighlight, shape = androidx.compose.foundation.shape.CircleShape),
            )
        },
    ) {
        Column(Modifier.navigationBarsPadding().padding(bottom = 16.dp)) {
            // Title
            Text(
                stringResource(R.string.player_speed),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            SPEEDS.forEach { speed ->
                val isSelected = speed == current
                Text(
                    formatSpeed(speed),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) StoneYellow else Color.White,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPick(speed) }
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                )
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun Context.notificationsAllowed(): Boolean =
    (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED) &&
        NotificationManagerCompat.from(this).areNotificationsEnabled()

private fun Context.openNotificationSettings() {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { startActivity(intent) }
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
