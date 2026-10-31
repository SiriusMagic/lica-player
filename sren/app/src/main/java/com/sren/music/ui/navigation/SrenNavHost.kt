package com.sren.music.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.TopAppBarDefaults
import com.sren.music.ui.player.PlayerScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import com.sren.music.ui.theme.SrenBackground
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sren.music.R
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
import com.sren.music.playback.PlayerViewModel
import com.sren.music.ui.player.NowPlayingBar
import com.sren.music.ui.screens.LibraryViewModel
import com.sren.music.ui.screens.home.HomeScreen
import com.sren.music.ui.screens.settings.SettingsScreen
import com.sren.music.ui.screens.settings.SettingsViewModel
import com.sren.music.ui.screens.library.CollectionKind
import com.sren.music.ui.screens.library.CollectionScreen
import com.sren.music.ui.screens.library.CollectionViewModel
import com.sren.music.ui.screens.library.LibraryScreen
import com.sren.music.ui.screens.library.SearchScreen
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import android.net.Uri

/** Root scaffold: bottom bar across four tabs, each tab owns its screen. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SrenNavHost() {
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
            val title = when {
                currentRoute?.route == COLLECTION_ROUTE -> R.string.tab_library
                else -> tabs.firstOrNull { currentRoute?.route == it.route }?.labelRes ?: R.string.app_name
            }
            val playerState by player.state.collectAsStateWithLifecycle()
            val syncing by library.isSyncing.collectAsStateWithLifecycle()
            SrenTopBar(
                title = stringResource(title),
                nowPlaying = playerState.nowPlaying?.let { "${it.title} · ${it.artist}" },
                syncing = syncing,
                onRescan = library::rescan,
            )
        },
        bottomBar = {
            if (onPlayer) return@Scaffold
            Column {
                NowPlayingBar(
                    viewModel = player,
                    onOpen = { navController.navigate(PLAYER_ROUTE) { launchSingleTop = true } },
                )
                // Stone 1.3.2: fondo SrenBackground, íconos/labels con primary cuando activo.
                NavigationBar(containerColor = SrenBackground) {
                    tabs.forEach { tab ->
                        // An opened album/artist/folder belongs to the Library tab.
                        val selected = currentRoute?.hierarchy?.any { it.route == tab.route } == true ||
                            (tab == Destination.Library && currentRoute?.route == COLLECTION_ROUTE)
                        NavigationBarItem(
                            selected = selected,
                            onClick = { navController.navigateToTab(tab) },
                            icon = {
                                Icon(
                                    tab.icon,
                                    contentDescription = null,
                                    tint = if (selected) MaterialTheme.colorScheme.primary
                                           else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            label = {
                                Text(
                                    stringResource(tab.labelRes),
                                    color = if (selected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
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
                HomeScreen(
                    onPlay = player::playSongs,
                    onPlayAlbumOf = player::playAlbumOf,
                    onOpenLibrary = { navController.navigateToTab(Destination.Library) },
                    playerViewModel = player,
                )
            }
            composable(Destination.Library.route) {
                LibraryScreen(
                    onPlay = player::playSongs,
                    onShuffle = player::playShuffled,
                    onOpen = { kind, key, title -> navController.navigate(collectionRoute(kind, key, title)) },
                    playerViewModel = player,
                )
            }
            composable(
                COLLECTION_ROUTE,
                arguments = listOf(
                    navArgument(CollectionViewModel.ARG_KIND) { type = NavType.StringType },
                    navArgument(CollectionViewModel.ARG_KEY) { type = NavType.StringType },
                    navArgument(ARG_TITLE) { type = NavType.StringType; defaultValue = "" },
                ),
            ) { entry ->
                CollectionScreen(
                    title = entry.arguments?.getString(ARG_TITLE).orEmpty(),
                    onPlay = player::playSongs,
                    onShuffle = player::playShuffled,
                    playerViewModel = player,
                )
            }
            composable(Destination.Search.route) {
                SearchScreen(onPlay = player::playSongs, playerViewModel = player)
            }
            composable(Destination.Settings.route) {
                SettingsScreen(
                    onRescan = library::rescan,
                    onOpenPlayer = {
                        navController.navigate(PLAYER_ROUTE) { launchSingleTop = true }
                    },
                )
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

/** Sren's top bar: screen name, the song playing underneath, and a rescan button. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SrenTopBar(title: String, nowPlaying: String?, syncing: Boolean, onRescan: () -> Unit) {
    // Stone 1.3.2: fondo SrenBackground, sin elevación ni sombra.
    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title)
                if (nowPlaying != null) {
                    Text(
                        nowPlaying,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onRescan, enabled = !syncing) {
                Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.action_rescan))
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = SrenBackground,
            scrolledContainerColor = SrenBackground,
        ),
    )
}

private const val ARG_TITLE = "title"
private const val COLLECTION_ROUTE =
    "collection/{${CollectionViewModel.ARG_KIND}}/{${CollectionViewModel.ARG_KEY}}?$ARG_TITLE={$ARG_TITLE}"

private fun collectionRoute(kind: CollectionKind, key: String, title: String) =
    "collection/${kind.name}/${Uri.encode(key)}?$ARG_TITLE=${Uri.encode(title)}"

/** Switches tab keeping each tab's own back stack and scroll position. */
private fun NavHostController.navigateToTab(tab: Destination) = navigate(tab.route) {
    popUpTo(graph.startDestinationId) { saveState = true }
    launchSingleTop = true
    restoreState = true
}
