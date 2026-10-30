package com.zirakusa.stone.ui.screens.videos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zirakusa.stone.data.model.Video
import com.zirakusa.stone.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class VideosUiState(
    val videos: List<Video> = emptyList(),
    val isSyncing: Boolean = false,
)

@HiltViewModel
class VideosViewModel @Inject constructor(
    repository: MediaRepository,
) : ViewModel() {

    val uiState: StateFlow<VideosUiState> =
        combine(repository.videos(), repository.isSyncing) { videos, syncing ->
            VideosUiState(videos = videos, isSyncing = syncing)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = VideosUiState(isSyncing = true),
        )
}
