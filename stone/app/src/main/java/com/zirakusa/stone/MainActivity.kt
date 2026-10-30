package com.zirakusa.stone

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.zirakusa.stone.core.locale.AppLanguage
import com.zirakusa.stone.data.settings.AppSettings
import com.zirakusa.stone.playback.PlayerViewModel
import com.zirakusa.stone.suggestion.SuggestionNotifier
import com.zirakusa.stone.suggestion.SuggestionScheduler
import com.zirakusa.stone.ui.navigation.StoneNavHost
import com.zirakusa.stone.ui.screens.PermissionGate
import com.zirakusa.stone.ui.screens.onboarding.LanguageScreen
import com.zirakusa.stone.ui.screens.onboarding.WelcomeScreen
import com.zirakusa.stone.ui.theme.StoneTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/** The single launcher activity. Hosts the Compose UI behind the media-permission gate. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settings: AppSettings
    @Inject lateinit var suggestions: SuggestionScheduler

    // Same instance the Compose tree gets from hiltViewModel() (activity-scoped).
    private val player: PlayerViewModel by viewModels()

    // Textos en el idioma elegido en Stone (no el del teléfono).
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLanguage.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Re-arms the daily suggestion (alarms are lost on reboot or app update). One cheap call.
        suggestions.sync()
        // Longest-battery profile: ask for the lowest refresh rate >= 60 Hz.
        // On 90/120 Hz screens this saves power whenever the app is on screen.
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                settings.profile.collect { capRefreshRate(it.capsRefreshRate) }
            }
        }
        if (savedInstanceState == null) handleSuggestion(intent)
        setContent {
            StoneTheme {
                // Bienvenida de Stone, solo la primera vez: idioma → "Permitir acceso" → app.
                var step by rememberSaveable {
                    mutableIntStateOf(
                        when {
                            settings.onboarded -> STEP_APP
                            settings.languageChosen -> STEP_WELCOME
                            else -> STEP_LANGUAGE
                        },
                    )
                }
                when (step) {
                    STEP_LANGUAGE -> LanguageScreen { code ->
                        val changed = code != AppLanguage.current(this)
                        AppLanguage.save(this, code)
                        settings.languageChosen = true
                        step = STEP_WELCOME
                        if (changed) recreate() // vuelve a abrir con los textos del idioma nuevo
                    }
                    STEP_WELCOME -> WelcomeScreen {
                        settings.onboarded = true
                        step = STEP_APP
                    }
                    else -> PermissionGate { StoneNavHost() }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleSuggestion(intent)
    }

    /** Tapping the daily suggestion opens the app and plays that song's album. */
    private fun handleSuggestion(intent: Intent?) {
        if (intent?.action != SuggestionNotifier.ACTION_PLAY_SUGGESTION) return
        val songId = intent.getLongExtra(SuggestionNotifier.EXTRA_SONG_ID, -1L)
        if (songId >= 0) player.playAlbumOf(songId)
    }

    private fun capRefreshRate(cap: Boolean) {
        val modeId = if (cap) {
            val display = ContextCompat.getDisplayOrDefault(this)
            val current = display.mode
            display.supportedModes
                .filter {
                    it.physicalWidth == current.physicalWidth &&
                        it.physicalHeight == current.physicalHeight &&
                        it.refreshRate >= 59f
                }
                .minByOrNull { it.refreshRate }
                ?.modeId ?: 0
        } else {
            0 // no preference: the system decides
        }
        if (window.attributes.preferredDisplayModeId != modeId) {
            window.attributes = window.attributes.also { it.preferredDisplayModeId = modeId }
        }
    }

    private companion object {
        const val STEP_LANGUAGE = 0
        const val STEP_WELCOME = 1
        const val STEP_APP = 2
    }
}
