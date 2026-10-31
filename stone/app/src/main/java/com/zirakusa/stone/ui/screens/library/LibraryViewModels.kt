package com.zirakusa.stone.ui.screens.library

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zirakusa.stone.data.model.Album
import com.zirakusa.stone.data.model.Artist
import com.zirakusa.stone.data.model.Folder
import com.zirakusa.stone.data.model.Song
import com.zirakusa.stone.data.repository.MediaRepository
import com.zirakusa.stone.data.settings.AppSettings
import com.zirakusa.stone.data.settings.SongSort
import com.zirakusa.stone.data.user.UserLibrary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** null = still loading from Room. */
private fun <T> Flow<T>.stateWhileVisible(vm: ViewModel): StateFlow<T?> =
    stateIn(vm.viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

data class SongsUiState(val songs: List<Song> = emptyList(), val sort: SongSort = SongSort.DEFAULT, val syncing: Boolean = false)

@HiltViewModel
class SongsViewModel @Inject constructor(
    repository: MediaRepository,
    private val settings: AppSettings,
) : ViewModel() {
    val state: StateFlow<SongsUiState?> = combine(repository.songs(), settings.songSort, repository.isSyncing) { songs, sort, syncing ->
        SongsUiState(sort.apply(songs), sort, syncing)
    }.flowOn(Dispatchers.Default).stateWhileVisible(this)

    fun setSort(sort: SongSort) = settings.setSongSort(sort)
}

@HiltViewModel
class FavoritesViewModel @Inject constructor(library: UserLibrary) : ViewModel() {
    val songs: StateFlow<List<Song>?> = library.favorites().stateWhileVisible(this)
}

@HiltViewModel
class GroupsViewModel @Inject constructor(repository: MediaRepository) : ViewModel() {
    val albums: StateFlow<List<Album>?> = repository.albums().stateWhileVisible(this)
    val artists: StateFlow<List<Artist>?> = repository.artists().stateWhileVisible(this)
    val folders: StateFlow<List<Folder>?> = repository.folders().stateWhileVisible(this)
}

/** One album, artist or folder, opened from the Library. Arguments come from the route. */
@HiltViewModel
class CollectionViewModel @Inject constructor(
    repository: MediaRepository,
    savedState: SavedStateHandle,
) : ViewModel() {
    val kind: CollectionKind = CollectionKind.valueOf(savedState.get<String>(ARG_KIND)!!)
    val key: String = savedState.get<String>(ARG_KEY)!!

    val songs: StateFlow<List<Song>?> = when (kind) {
        CollectionKind.ALBUM -> repository.songsOfAlbum(key.toLong())
        CollectionKind.ARTIST -> repository.songsOfArtist(key)
        CollectionKind.FOLDER -> repository.songsInFolder(key)
    }.stateWhileVisible(this)

    companion object {
        const val ARG_KIND = "kind"
        const val ARG_KEY = "key"
    }
}

enum class CollectionKind { ALBUM, ARTIST, FOLDER }

@HiltViewModel
class SearchViewModel @Inject constructor(repository: MediaRepository) : ViewModel() {
    val query = MutableStateFlow("")

    /** Waits until typing pauses (250 ms) so each keystroke does not hit the database. */
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val results: StateFlow<List<Song>?> = query
        .debounce(250)
        .map { it.trim() }
        .flatMapLatest { text -> if (text.isEmpty()) flowOf(emptyList()) else repository.search(text) }
        .stateWhileVisible(this)
}
