package com.zirakusa.stone.data.local.mediastore

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A cheap fingerprint of the device's media library.
 *
 * - [version] changes only when MediaStore rebuilds its whole database
 *   (factory reset, OS upgrade, storage wiped). Then we must rescan everything.
 * - [generation] goes up on every insert or update. If only it moved, we read
 *   just the rows changed since the saved generation.
 *
 * Below Android 11 there is no generation, so [version] holds a signature
 * (row count + newest modification date) and [generation] is [NO_GENERATION].
 */
data class LibraryStamp(val version: String, val generation: Long) {

    /** True when we can read only the changes since [saved] instead of everything. */
    fun canDiffFrom(saved: LibraryStamp): Boolean =
        version == saved.version &&
            generation != NO_GENERATION &&
            saved.generation != NO_GENERATION &&
            generation > saved.generation

    companion object {
        const val NO_GENERATION = -1L
    }
}

/** Stores the last synced [LibraryStamp]. SharedPreferences: no extra library, one tiny XML file. */
@Singleton
class LibraryStampStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences("library_sync", Context.MODE_PRIVATE)

    fun load(): LibraryStamp? {
        val version = prefs.getString(KEY_VERSION, null) ?: return null
        return LibraryStamp(version, prefs.getLong(KEY_GENERATION, LibraryStamp.NO_GENERATION))
    }

    fun save(stamp: LibraryStamp) {
        prefs.edit()
            .putString(KEY_VERSION, stamp.version)
            .putLong(KEY_GENERATION, stamp.generation)
            .apply()
    }

    private companion object {
        const val KEY_VERSION = "version"
        const val KEY_GENERATION = "generation"
    }
}
