package com.lica.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.lica.player.core.media.MediaUris

/**
 * Album-art thumbnail. Loads the image at [sizePx] pixels max — Coil
 * downsamples it to that size before decoding, so the bitmap in memory
 * is tiny even for large cover files.
 *
 * Falls back to a music-note icon on error or while loading.
 */
@Composable
fun AlbumArt(
    albumId: Long,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    sizePx: Int = 128,  // Low-res for lists; caller passes 512 for Now Playing.
) {
    val artUri = MediaUris.albumArt(albumId)

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(artUri)
            .size(sizePx)
            .scale(Scale.FILL)
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(6.dp)),
        error = { ArtPlaceholder(modifier = Modifier.size(size)) },
        loading = { ArtPlaceholder(modifier = Modifier.size(size)) },
    )
}

@Composable
private fun ArtPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Filled.MusicNote,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
