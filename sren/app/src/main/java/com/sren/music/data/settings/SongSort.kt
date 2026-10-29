package com.sren.music.data.settings

import com.sren.music.data.model.Song
import java.text.Collator

/** How the Songs list is ordered. Sorting happens in memory: one pass, no extra queries. */
enum class SongSort {
    RECENT,
    TITLE,
    ARTIST,
    ALBUM,
    DURATION;

    fun apply(songs: List<Song>): List<Song> {
        // Collator orders "Álvaro" next to "Alvaro", as people expect.
        val text = Collator.getInstance().apply { strength = Collator.PRIMARY }
        return when (this) {
            RECENT -> songs.sortedByDescending { it.dateAdded }
            TITLE -> songs.sortedWith { a, b -> text.compare(a.title, b.title) }
            ARTIST -> songs.sortedWith { a, b -> text.compare(a.artist, b.artist).takeIf { it != 0 } ?: text.compare(a.title, b.title) }
            ALBUM -> songs.sortedWith { a, b ->
                text.compare(a.album, b.album).takeIf { it != 0 } ?: a.trackNumber.compareTo(b.trackNumber)
            }
            DURATION -> songs.sortedByDescending { it.duration }
        }
    }

    companion object {
        val DEFAULT = TITLE
    }
}
