package com.lica.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.lica.player.ui.navigation.LicaNavHost
import com.lica.player.ui.screens.PermissionGate
import com.lica.player.ui.theme.LicaTheme
import dagger.hilt.android.AndroidEntryPoint

/** The single launcher activity. Hosts the Compose UI behind the media-permission gate. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LicaTheme {
                PermissionGate {
                    LicaNavHost()
                }
            }
        }
    }
}
