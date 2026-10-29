package com.sren.music.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sren.music.data.model.Video
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {

    @Query("SELECT * FROM videos ORDER BY title ASC")
    fun observeAll(): Flow<List<Video>>

    @Query("SELECT * FROM videos ORDER BY dateAdded DESC")
    fun observeRecentlyAdded(): Flow<List<Video>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(videos: List<Video>)

    @Query("SELECT id FROM videos")
    suspend fun allIds(): List<Long>

    /** Callers pass at most 500 IDs per call (SQLite's variable limit is 999 on old Android). */
    @Query("DELETE FROM videos WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM videos")
    suspend fun count(): Int
}
