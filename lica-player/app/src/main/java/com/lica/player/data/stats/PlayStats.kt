package com.lica.player.data.stats

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction

/** How often and how recently a song was listened to. Keyed by MediaStore id. */
@Entity(tableName = "play_stats")
data class PlayStat(
    @PrimaryKey val songId: Long,
    val playCount: Int,
    val lastPlayedAt: Long, // Unix millis
)

@Dao
abstract class PlayStatsDao {

    @Query("UPDATE play_stats SET playCount = playCount + 1, lastPlayedAt = :at WHERE songId = :songId")
    protected abstract suspend fun bump(songId: Long, at: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insert(stat: PlayStat)

    /** Counts one play. (SQLite UPSERT needs Android 11+, so update-then-insert.) */
    @Transaction
    open suspend fun recordPlay(songId: Long, at: Long) {
        if (bump(songId, at) == 0) insert(PlayStat(songId, 1, at))
    }

    @Query("SELECT * FROM play_stats")
    abstract suspend fun all(): List<PlayStat>
}

/**
 * Listening history lives in its own small database. The song library
 * ([com.lica.player.data.local.db.LicaDatabase]) is only a cache that is
 * dropped and rescanned on schema changes; this one is user data and must
 * survive that, so it gets real migrations when its schema changes.
 */
@Database(entities = [PlayStat::class], version = 1, exportSchema = false)
abstract class StatsDatabase : RoomDatabase() {
    abstract fun playStatsDao(): PlayStatsDao
}
