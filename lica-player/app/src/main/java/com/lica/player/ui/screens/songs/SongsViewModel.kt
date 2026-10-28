package com.lica.player.ui.screens.songs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lica.player.data.model.Song
import com.lica.player.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class SongsUiState(
    val songs: List<Song> = emptyList(),
    val isSyncing: Boolean = false,
)

@HiltViewModel
class SongsViewModel @Inject constructor(
    repository: MediaRepository,
) : ViewModel() {

    // Room is the only source of truth: on a normal launch this list appears
    // straight from the cache, with no MediaStore read. Syncing is driven by
    // LibraryViewModel while the app is visible.
    val uiState: StateFlow<SongsUiState> =
        combine(repository.songs(), repository.isSyncing) { songs, syncing ->
            SongsUiState(songs = songs, isSyncing = syncing)
        }.stateIn(
            scope = viewModelScope,
            // Stops reading Room 5 s after the UI goes away (rotation-safe).
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SongsUiState(isSyncing = true),
        )
}
