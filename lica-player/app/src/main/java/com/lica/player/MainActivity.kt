package com.lica.player

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lica.player.data.settings.AppSettings
import com.lica.player.playback.PlayerViewModel
import com.lica.player.suggestion.SuggestionNotifier
import com.lica.player.suggestion.SuggestionScheduler
import com.lica.player.ui.navigation.LicaNavHost
import com.lica.player.ui.screens.PermissionGate
import com.lica.player.ui.theme.LicaTheme
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
            LicaTheme {
                PermissionGate {
                    LicaNavHost()
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
}
