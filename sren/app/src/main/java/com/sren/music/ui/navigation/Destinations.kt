package com.sren.music.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.sren.music.R

/** Sren's bottom tabs. Each maps to a route and a bottom-bar entry. */
enum class Destination(val route: String, val labelRes: Int, val icon: ImageVector) {
    Home("home", R.string.tab_home, Icons.Filled.Home),
    Library("library", R.string.tab_library, Icons.Filled.LibraryMusic),
    Search("search", R.string.tab_search, Icons.Filled.Search),
    Settings("settings", R.string.tab_settings, Icons.Filled.Settings),
}
