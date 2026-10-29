package com.sren.music.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sren.music.data.local.entity.PlaylistEntity
import com.sren.music.data.local.entity.PlaylistSongEntity
import com.sren.music.data.local.entity.SongEntity
import com.sren.music.data.local.model.PlaylistSummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query(
        """
        SELECT playlists.id, playlists.name, playlists.createdAt, COUNT(playlist_songs.songId) AS songCount
        FROM playlists
        LEFT JOIN playlist_songs ON playlists.id = playlist_songs.playlistId
        GROUP BY playlists.id
        ORDER BY playlists.createdAt DESC
        """,
    )
    fun observePlaylists(): Flow<List<PlaylistSummaryEntity>>

    @Query(
        """
        SELECT songs.* FROM songs
        INNER JOIN playlist_songs ON songs.id = playlist_songs.songId
        WHERE playlist_songs.playlistId = :playlistId
          AND songs.deletedAt IS NULL
          AND songs.isHidden = 0
        ORDER BY playlist_songs.position ASC
        """,
    )
    fun observePlaylistSongs(playlistId: Long): Flow<List<SongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSong(playlistSong: PlaylistSongEntity)

    @Query("SELECT COALESCE(MAX(position), -1) FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun getLastPosition(playlistId: Long): Int
}
