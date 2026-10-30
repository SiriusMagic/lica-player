package com.sren.domain.usecase

import com.sren.domain.model.Song
import com.sren.domain.repository.MusicRepository

class SearchSongsUseCase(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(query: String, limit: Int = 50): List<Song> {
        return musicRepository.searchSongs(query, limit)
    }
}
