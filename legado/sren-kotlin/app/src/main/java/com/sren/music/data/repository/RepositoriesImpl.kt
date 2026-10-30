package com.sren.music.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sren.music.data.local.dao.PlaylistDao
import com.sren.music.data.local.dao.SongDao
import com.sren.music.data.local.entity.PlaylistEntity
import com.sren.music.data.local.entity.PlaylistSongEntity
import com.sren.music.data.local.entity.SongEntity
import com.sren.music.data.local.model.PlaylistSummaryEntity
import com.sren.music.data.scanner.LocalMusicScanner
import com.sren.music.data.scanner.ScannedSong
import com.sren.music.domain.model.AiProviderType
import com.sren.music.domain.model.Playlist
import com.sren.music.domain.model.Song
import com.sren.music.domain.repository.PlaylistRepository
import com.sren.music.domain.repository.SettingsRepository
import com.sren.music.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "sren_settings")

private fun SongEntity.toDomain() = Song(
    id = id,
    title = title,
    artist = artist,
    album = album,
    durationMs = durationMs,
    path = path,
    playbackUri = playbackUri,
    mimeType = mimeType,
    isFavorite = isFavorite,
    dateAdded = dateAdded,
    lastPlayedAt = lastPlayedAt,
    folderPath = folderPath,
    folderName = folderName,
    trackNumber = trackNumber,
    discNumber = discNumber,
    isHidden = isHidden,
    deletedAt = deletedAt,
    lyricsPath = lyricsPath,
)

private fun PlaylistSummaryEntity.toDomain() = Playlist(id, name, createdAt, songCount)

private fun ScannedSong.toEntity(existing: SongEntity?) = SongEntity(
    id = existing?.id ?: 0L,
    title = title,
    artist = artist,
    album = album,
    durationMs = durationMs,
    path = path,
    playbackUri = playbackUri,
    mimeType = mimeType,
    isFavorite = existing?.isFavorite ?: false,
    dateAdded = dateAdded,
    lastPlayedAt = existing?.lastPlayedAt,
    folderPath = folderPath,
    folderName = folderName,
    trackNumber = trackNumber,
    discNumber = discNumber,
    isHidden = existing?.isHidden ?: false,
    deletedAt = existing?.deletedAt,
    lyricsPath = lyricsPath ?: existing?.lyricsPath,
)

class SongRepositoryImpl(
    private val songDao: SongDao,
    private val scanner: LocalMusicScanner,
) : SongRepository {
    override fun observeSongs(): Flow<List<Song>> =
        songDao.observeSongs().map { it.map(SongEntity::toDomain) }

    override fun observeFavoriteSongs(limit: Int?): Flow<List<Song>> =
        songDao.observeFavoriteSongs().map { list ->
            list.map(SongEntity::toDomain).let { songs -> limit?.let(songs::take) ?: songs }
        }

    override fun observeRecentSongs(limit: Int?): Flow<List<Song>> =
        songDao.observeRecentSongs().map { list ->
            list.map(SongEntity::toDomain).let { songs -> limit?.let(songs::take) ?: songs }
        }

    override fun observeHiddenSongs(): Flow<List<Song>> =
        songDao.observeHiddenSongs().map { it.map(SongEntity::toDomain) }

    override fun observeDeletedSongs(): Flow<List<Song>> =
        songDao.observeDeletedSongs().map { it.map(SongEntity::toDomain) }

    override fun searchSongs(query: String): Flow<List<Song>> =
        songDao.searchSongs(query.trim()).map { it.map(SongEntity::toDomain) }

    override suspend fun getSong(songId: Long): Song? = songDao.getSong(songId)?.toDomain()

    override suspend fun getSongs(songIds: List<Long>): List<Song> =
        songDao.getSongs(songIds).map(SongEntity::toDomain)

    override suspend fun rescanLibrary(): Int {
        val scanned = scanner.scan()
        if (scanned.isEmpty()) return 0

        val existing = songDao.getAllSongs().associateBy { it.path }
        val merged = scanned.map { it.toEntity(existing[it.path]) }
        songDao.upsertSongs(merged)
        songDao.deleteMissingSongs(merged.map { it.path })
        return merged.size
    }

    override suspend fun toggleFavorite(songId: Long) {
        songDao.toggleFavorite(songId)
    }

    override suspend fun hideSong(songId: Long) {
        songDao.setHidden(songId, true)
    }

    override suspend fun softDeleteSong(songId: Long) {
        songDao.softDelete(songId, System.currentTimeMillis())
    }

    override suspend fun restoreSong(songId: Long) {
        songDao.restore(songId)
    }

    override suspend fun deletePermanently(songId: Long) {
        songDao.deletePermanently(songId)
    }

    override suspend fun updateLastPlayed(songId: Long, playedAt: Long) {
        songDao.updateLastPlayed(songId, playedAt)
    }

    override suspend fun getFavoriteSongs(limit: Int): List<Song> =
        songDao.getFavoriteSongs(limit).map(SongEntity::toDomain)

    override suspend fun getRecentSongs(limit: Int): List<Song> =
        songDao.getRecentSongs(limit).map(SongEntity::toDomain)

    override suspend fun searchSongsNow(query: String, limit: Int): List<Song> =
        if (query.isBlank()) {
            emptyList()
        } else {
            songDao.searchSongsNow(query.trim(), limit).map(SongEntity::toDomain)
        }

    override suspend fun findSongsByCriteria(criteria: String, limit: Int): List<Song> {
        val visibleSongs = songDao.getAllSongs().filter { it.deletedAt == null && !it.isHidden }
        val normalized = criteria.trim().lowercase()
        val ranked = when {
            listOf("entren", "gym", "workout", "rapid", "rapida", "energ", "electr").any(normalized::contains) ->
                visibleSongs.sortedWith(
                    compareByDescending<SongEntity> { it.isFavorite }
                        .thenByDescending { it.lastPlayedAt ?: 0L }
                        .thenByDescending { it.dateAdded },
                )

            listOf("estudi", "focus", "tranquil", "calm", "suave").any(normalized::contains) ->
                visibleSongs.sortedWith(
                    compareByDescending<SongEntity> { it.lastPlayedAt ?: 0L }
                        .thenBy { it.title.length }
                        .thenBy { it.trackNumber },
                )

            else -> songDao.findSongsByCriteria(criteria, limit * 2)
        }
        return ranked.take(limit).map(SongEntity::toDomain)
    }
}

class PlaylistRepositoryImpl(
    private val playlistDao: PlaylistDao,
) : PlaylistRepository {
    override fun observePlaylists(): Flow<List<Playlist>> =
        playlistDao.observePlaylists().map { it.map(PlaylistSummaryEntity::toDomain) }

    override fun observePlaylistSongs(playlistId: Long): Flow<List<Song>> =
        playlistDao.observePlaylistSongs(playlistId).map { it.map(SongEntity::toDomain) }

    override suspend fun createPlaylist(name: String): Long =
        playlistDao.insertPlaylist(
            PlaylistEntity(
                name = name.trim(),
                createdAt = System.currentTimeMillis(),
            ),
        )

    override suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        val nextPosition = playlistDao.getLastPosition(playlistId) + 1
        playlistDao.insertPlaylistSong(PlaylistSongEntity(playlistId, songId, nextPosition))
    }

    override suspend fun createPlaylistFromCriteria(name: String, songs: List<Song>): Long {
        val playlistId = createPlaylist(name)
        songs.forEachIndexed { index, song ->
            playlistDao.insertPlaylistSong(PlaylistSongEntity(playlistId, song.id, index))
        }
        return playlistId
    }
}

class SettingsRepositoryImpl(
    private val context: Context,
) : SettingsRepository {
    override val aiProvider: Flow<AiProviderType> = context.settingsDataStore.data.map { prefs ->
        prefs[KEY_AI_PROVIDER]?.let { runCatching { AiProviderType.valueOf(it) }.getOrNull() }
            ?: AiProviderType.LOCAL_RULES
    }

    override suspend fun setAiProvider(type: AiProviderType) {
        context.settingsDataStore.edit { prefs ->
            prefs[KEY_AI_PROVIDER] = type.name
        }
    }

    private companion object {
        val KEY_AI_PROVIDER = stringPreferencesKey("ai_provider")
    }
}
