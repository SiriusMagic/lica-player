package com.sren.music.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sren.music.data.local.db.dao.SongDao
import com.sren.music.data.local.db.dao.VideoDao
import com.sren.music.data.model.Song
import com.sren.music.data.model.Video

@Database(
    entities = [Song::class, Video::class],
    version = 2, // v2: songs.folder. A cache: a schema change drops it and rescans.
    exportSchema = false,
)
abstract class LibraryDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun videoDao(): VideoDao
}
