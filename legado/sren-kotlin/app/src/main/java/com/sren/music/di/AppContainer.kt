package com.sren.music.di

import android.content.Context
import androidx.room.Room
import com.sren.music.assistant.tools.ToolRegistry
import com.sren.music.data.local.SrenDatabase
import com.sren.music.data.repository.PlaylistRepositoryImpl
import com.sren.music.data.repository.SettingsRepositoryImpl
import com.sren.music.data.repository.SongRepositoryImpl
import com.sren.music.data.scanner.LocalMusicScanner
import com.sren.music.domain.repository.PlaylistRepository
import com.sren.music.domain.repository.SettingsRepository
import com.sren.music.domain.repository.SongRepository
import com.sren.music.domain.usecase.HandleAssistantCommandUseCase
import com.sren.music.player.PlayerController
import com.sren.music.player.SrenPlayerController

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val database: SrenDatabase by lazy {
        Room.databaseBuilder(appContext, SrenDatabase::class.java, "sren.db")
            .addMigrations(SrenDatabase.migration1To2)
            .build()
    }

    private val scanner: LocalMusicScanner by lazy {
        LocalMusicScanner(appContext)
    }

    val songRepository: SongRepository by lazy {
        SongRepositoryImpl(database.songDao(), scanner)
    }

    val playlistRepository: PlaylistRepository by lazy {
        PlaylistRepositoryImpl(database.playlistDao())
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(appContext)
    }

    val playerController: PlayerController by lazy {
        SrenPlayerController(appContext, songRepository)
    }

    val toolRegistry: ToolRegistry by lazy {
        ToolRegistry(songRepository, playlistRepository, playerController)
    }

    val handleAssistantCommandUseCase: HandleAssistantCommandUseCase by lazy {
        HandleAssistantCommandUseCase(
            settingsRepository = settingsRepository,
            songRepository = songRepository,
            toolRegistry = toolRegistry,
        )
    }
}
