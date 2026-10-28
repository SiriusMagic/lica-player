package com.lica.player.playback

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.util.LruCache
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import com.lica.player.core.media.MediaUris
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Soft accent color per album, computed once and remembered.
 *
 * Cost: the cover is decoded at 64×64 px (~16 KB) only for the extraction, then
 * dropped. The cache stores plain ints, so 64 albums cost about 1 KB.
 */
@Singleton
class PaletteCache @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val cache = LruCache<Long, Int>(64)

    /** Returns the album's soft accent color, or null when it has no usable cover. */
    suspend fun accentFor(albumId: Long): Int? {
        cache.get(albumId)?.let { return it.takeIf { it != NO_COLOR } }

        val request = ImageRequest.Builder(context)
            .data(MediaUris.albumArt(albumId))
            .size(64)
            .allowHardware(false) // Palette needs to read pixels.
            .build()
        val bitmap = (context.imageLoader.execute(request).drawable as? BitmapDrawable)?.bitmap
        val color = bitmap?.let {
            withContext(Dispatchers.Default) {
                val palette = Palette.from(it).maximumColorCount(8).generate()
                val raw = palette.darkMutedSwatch?.rgb
                    ?: palette.mutedSwatch?.rgb
                    ?: palette.dominantSwatch?.rgb
                // Darken toward the app background so white text stays readable and nothing looks garish.
                raw?.let { c -> ColorUtils.blendARGB(c, BASE_DARK, 0.35f) }
            }
        }
        cache.put(albumId, color ?: NO_COLOR)
        return color
    }

    private companion object {
        const val NO_COLOR = 0
        const val BASE_DARK = 0xFF101416.toInt()
    }
}
