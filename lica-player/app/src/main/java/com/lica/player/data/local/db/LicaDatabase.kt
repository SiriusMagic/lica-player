package com.lica.player.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lica.player.data.local.db.dao.SongDao
import com.lica.player.data.local.db.dao.VideoDao
import com.lica.player.data.model.Song
import com.lica.player.data.model.Video

@Database(
    entities = [Song::class, Video::class],
    version = 1,
    exportSchema = false,
)
abstract class LicaDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun videoDao(): VideoDao
}
