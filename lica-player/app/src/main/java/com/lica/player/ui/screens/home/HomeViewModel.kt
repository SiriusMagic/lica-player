package com.lica.player.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lica.player.data.model.Song
import com.lica.player.data.repository.MediaRepository
import com.lica.player.data.settings.AppSettings
import com.lica.player.data.user.UserLibrary
import com.lica.player.suggestion.SuggestionPicker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HomeUiState(
    val loading: Boolean = true,
    val songCount: Int = 0,
    val suggestion: Song? = null,
    val recentlyPlayed: List<Song> = emptyList(),
    val mostPlayed: List<Song> = emptyList(),
    val favorites: List<Song> = emptyList(),
    val recentlyAdded: List<Song> = emptyList(),
)

/**
 * Everything on the Home tab comes from Room (no MediaStore reads) and is
 * read only while the tab is on screen (WhileSubscribed).
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: MediaRepository,
    library: UserLibrary,
    settings: AppSettings,
    picker: SuggestionPicker,
) : ViewModel() {

    /**
     * Today's pick: the same song the daily notification suggested. With no
     * notification yet, one is picked now and remembered, so both agree.
     */
    private val suggestion = flow {
        val last = settings.lastSuggestedSongId
        val song = (if (last >= 0) library.song(last) else null)
            ?: picker.pick(last)?.also { settings.lastSuggestedSongId = it.id }
        emit(song)
    }

    private val history = combine(
        library.recentlyPlayed(ROW_SIZE),
        library.mostPlayed(ROW_SIZE),
        library.favorites(),
    ) { recent, most, favorites -> Triple(recent, most, favorites) }

    val state: StateFlow<HomeUiState> = combine(
        repository.songCount(),
        repository.recentlyAdded(ROW_SIZE),
        suggestion,
        history,
    ) { count, added, pick, (recent, most, favorites) ->
        HomeUiState(
            loading = false,
            songCount = count,
            suggestion = pick,
            recentlyPlayed = recent,
            mostPlayed = most,
            favorites = favorites,
            recentlyAdded = added,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    private companion object {
        const val ROW_SIZE = 12
    }
}
