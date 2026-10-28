package com.lica.player.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.ui.graphics.vector.ImageVector
import com.lica.player.R

/** The top-level tabs of the app. Each maps to a route and a bottom-bar entry. */
enum class Destination(val route: String, val labelRes: Int, val icon: ImageVector) {
    Songs("songs", R.string.tab_songs, Icons.Filled.MusicNote),
    Videos("videos", R.string.tab_videos, Icons.Filled.Videocam),
    Playlists("playlists", R.string.tab_playlists, Icons.AutoMirrored.Filled.QueueMusic),
    Settings("settings", R.string.tab_settings, Icons.Filled.Settings),
}
