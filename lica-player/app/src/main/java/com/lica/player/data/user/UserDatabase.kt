package com.lica.player.data.user

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

/** How often and how recently a song was listened to. Keyed by MediaStore id. */
@Entity(tableName = "play_stats")
data class PlayStat(
    @PrimaryKey val songId: Long,
    val playCount: Int,
    val lastPlayedAt: Long, // Unix millis
)

/** A song the user marked with the heart. */
@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey val songId: Long,
    val addedAt: Long, // Unix millis
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

    @Query("SELECT songId FROM play_stats ORDER BY lastPlayedAt DESC LIMIT :limit")
    abstract fun observeRecentIds(limit: Int): Flow<List<Long>>

    @Query("SELECT songId FROM play_stats ORDER BY playCount DESC, lastPlayedAt DESC LIMIT :limit")
    abstract fun observeMostPlayedIds(limit: Int): Flow<List<Long>>
}

@Dao
abstract class FavoriteDao {

    @Query("SELECT songId FROM favorites ORDER BY addedAt DESC")
    abstract fun observeIds(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insert(favorite: Favorite)

    @Query("DELETE FROM favorites WHERE songId = :songId")
    protected abstract suspend fun delete(songId: Long): Int

    /** Adds the song, or removes it if it was already a favorite. */
    @Transaction
    open suspend fun toggle(songId: Long, at: Long) {
        if (delete(songId) == 0) insert(Favorite(songId, at))
    }
}

/**
 * The user's own data: listening history, favorites and (later) playlists.
 *
 * Kept apart from the song library ([com.lica.player.data.local.db.LicaDatabase]),
 * which is only a disposable MediaStore cache. This one must never be dropped:
 * every schema change needs a [Migration] below. The schema JSON of each
 * version is exported to `app/schemas/` so migrations can be checked.
 *
 * The file is still called `stats.db` (its first name) so existing history survives.
 */
@Database(
    entities = [PlayStat::class, Favorite::class],
    version = 2,
    exportSchema = true,
)
abstract class UserDatabase : RoomDatabase() {
    abstract fun playStatsDao(): PlayStatsDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        const val FILE_NAME = "stats.db"

        /** v2: favorites. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `favorites` " +
                        "(`songId` INTEGER NOT NULL, `addedAt` INTEGER NOT NULL, PRIMARY KEY(`songId`))",
                )
            }
        }
    }
}
