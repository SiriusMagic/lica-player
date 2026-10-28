package com.lica.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lica.player.data.settings.PlaybackSettings
import com.lica.player.ui.navigation.LicaNavHost
import com.lica.player.ui.screens.PermissionGate
import com.lica.player.ui.theme.LicaTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/** The single launcher activity. Hosts the Compose UI behind the media-permission gate. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settings: PlaybackSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Longest-battery profile: ask for the lowest refresh rate >= 60 Hz.
        // On 90/120 Hz screens this saves power whenever the app is on screen.
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                settings.profile.collect { capRefreshRate(it.capsRefreshRate) }
            }
        }
        setContent {
            LicaTheme {
                PermissionGate {
                    LicaNavHost()
                }
            }
        }
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
