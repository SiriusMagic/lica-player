package com.lica.player.core.media

import android.net.Uri

/** URIs MediaStore exposes without an extra query. */
object MediaUris {
    private val ALBUM_ART_BASE: Uri = Uri.parse("content://media/external/audio/albumart")

    fun albumArt(albumId: Long): Uri = Uri.withAppendedPath(ALBUM_ART_BASE, albumId.toString())
}
