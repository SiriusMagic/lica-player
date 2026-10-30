package com.sren.music.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sren.music.data.local.dao.PlaylistDao
import com.sren.music.data.local.dao.SongDao
import com.sren.music.data.local.entity.PlaylistEntity
import com.sren.music.data.local.entity.PlaylistSongEntity
import com.sren.music.data.local.entity.SongEntity

@Database(
    entities = [SongEntity::class, PlaylistEntity::class, PlaylistSongEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class SrenDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        val migration1To2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE songs ADD COLUMN playbackUri TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE songs ADD COLUMN folderPath TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE songs ADD COLUMN folderName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE songs ADD COLUMN trackNumber INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE songs ADD COLUMN discNumber INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE songs ADD COLUMN isHidden INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE songs ADD COLUMN deletedAt INTEGER")
                db.execSQL("ALTER TABLE songs ADD COLUMN lyricsPath TEXT")
                db.execSQL("UPDATE songs SET playbackUri = path WHERE playbackUri = ''")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_songs_folderPath ON songs(folderPath)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_songs_isHidden_deletedAt ON songs(isHidden, deletedAt)")
            }
        }
    }
}
