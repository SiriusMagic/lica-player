package com.lica.player

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.hilt.android.HiltAndroidApp

/** Process entry point. Hilt builds the dependency graph from here. */
@HiltAndroidApp
class LicaApp : Application(), ImageLoaderFactory {

    /**
     * Image loading tuned for low memory. Coil's defaults allow 25% of the
     * app's memory for decoded images and up to 2% of free disk space for the
     * disk cache. Covers here are small thumbnails, so far less is enough.
     */
    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .components { add(VideoFrameDecoder.Factory()) } // Video thumbnails.
        .memoryCache {
            MemoryCache.Builder(this)
                .maxSizePercent(0.10)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(cacheDir.resolve("art"))
                .maxSizeBytes(32L * 1024 * 1024)
                .build()
        }
        .build()
    // Coil already clears its memory cache on low-memory callbacks.
}
