package com.lica.player.ui.screens

import androidx.lifecycle.ViewModel
import com.lica.player.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** Activity-scoped owner of library syncing, so Songs and Videos never scan twice. */
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: MediaRepository,
) : ViewModel() {

    /** Run inside repeatOnLifecycle(STARTED): it stops when the app is hidden. */
    suspend fun watchWhileVisible() = repository.watchLibrary()
}
