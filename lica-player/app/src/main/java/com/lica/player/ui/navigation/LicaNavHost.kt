package com.lica.player.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ScaffoldDefaults
import com.lica.player.ui.player.PlayerScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lica.player.playback.PlayerViewModel
import com.lica.player.ui.player.NowPlayingBar
import com.lica.player.ui.screens.LibraryViewModel
import com.lica.player.ui.screens.PlaceholderScreen
import com.lica.player.ui.screens.home.HomeScreen
import com.lica.player.ui.screens.settings.SettingsScreen
import com.lica.player.ui.screens.settings.SettingsViewModel
import com.lica.player.ui.screens.songs.SongsScreen
import com.lica.player.ui.screens.videos.VideosScreen

/** Root scaffold: bottom bar across four tabs, each tab owns its screen. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicaNavHost() {
    val navController = rememberNavController()
    // Activity-scoped: one player state shared by every tab.
    val player: PlayerViewModel = hiltViewModel()
    val library: LibraryViewModel = hiltViewModel()

    // Library sync follows the app's visibility: it checks for changes each
    // time the app comes to the front, listens while it is visible, and stops
    // completely when the app is hidden or the screen turns off.
    // This runs only after PermissionGate has granted access.
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) { library.watchWhileVisible() }
    }

    RequestNotificationsOnce()

    val tabs = Destination.entries
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination
    // The full-screen player draws edge to edge, without the app's bars.
    val onPlayer = currentRoute?.route == PLAYER_ROUTE

    Scaffold(
        contentWindowInsets = if (onPlayer) WindowInsets(0, 0, 0, 0) else ScaffoldDefaults.contentWindowInsets,
        topBar = {
            if (onPlayer) return@Scaffold
            val title = tabs.firstOrNull { currentRoute?.route == it.route }?.labelRes
            TopAppBar(title = { Text(title?.let { stringResource(it) } ?: "Lica Player") })
        },
        bottomBar = {
            if (onPlayer) return@Scaffold
            Column {
                NowPlayingBar(
                    viewModel = player,
                    onOpen = { navController.navigate(PLAYER_ROUTE) { launchSingleTop = true } },
                )
                NavigationBar {
                    tabs.forEach { tab ->
                        val selected = currentRoute?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(stringResource(tab.labelRes)) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(PLAYER_ROUTE) {
                PlayerScreen(viewModel = player, onClose = { navController.popBackStack() })
            }
            composable(Destination.Home.route) {
                HomeScreen(onPlay = player::playSongs, onPlayAlbumOf = player::playAlbumOf)
            }
            composable(Destination.Songs.route) {
                SongsScreen(onSongClick = player::playSongs)
            }
            composable(Destination.Videos.route) {
                VideosScreen()
            }
            composable(Destination.Playlists.route) {
                PlaceholderScreen(
                    title = stringResource(Destination.Playlists.labelRes),
                    icon = Destination.Playlists.icon,
                )
            }
            composable(Destination.Settings.route) {
                SettingsScreen()
            }
        }
    }
}

/**
 * Android 13+: asks for notification access once, on first launch, for the daily
 * suggestion. Media controls do not need it. If the user says no, it is never
 * asked again here; Settings can still turn the suggestion on.
 */
@Composable
private fun RequestNotificationsOnce() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val settings: SettingsViewModel = hiltViewModel()
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted && settings.claimNotificationPrompt()) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

private const val PLAYER_ROUTE = "player"
