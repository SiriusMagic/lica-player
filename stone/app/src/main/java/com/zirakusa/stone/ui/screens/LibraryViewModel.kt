package com.zirakusa.stone.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.zirakusa.stone.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** Activity-scoped owner of library syncing, so Songs and Videos never scan twice. */
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: MediaRepository,
) : ViewModel() {

    /** Run inside repeatOnLifecycle(STARTED): it stops when the app is hidden. */
    suspend fun watchWhileVisible() = repository.watchLibrary()

    val isSyncing: StateFlow<Boolean> = repository.isSyncing

    /** The refresh button: a full rescan, for when the user knows something changed. */
    fun rescan() {
        viewModelScope.launch { repository.syncLibrary(force = true) }
    }
}
