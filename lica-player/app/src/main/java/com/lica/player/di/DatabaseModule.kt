package com.lica.player.di

import android.content.Context
import androidx.room.Room
import com.lica.player.data.local.db.LicaDatabase
import com.lica.player.data.local.db.dao.SongDao
import com.lica.player.data.local.db.dao.VideoDao
import com.lica.player.data.user.FavoriteDao
import com.lica.player.data.user.PlayStatsDao
import com.lica.player.data.user.UserDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LicaDatabase =
        Room.databaseBuilder(context, LicaDatabase::class.java, "lica.db")
            // The table is only a cache of MediaStore: on a schema change, drop and rescan.
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideSongDao(db: LicaDatabase): SongDao = db.songDao()

    @Provides
    fun provideVideoDao(db: LicaDatabase): VideoDao = db.videoDao()

    /** The user's own data (history, favorites). Real migrations only, never destructive. */
    @Provides
    @Singleton
    fun provideUserDatabase(@ApplicationContext context: Context): UserDatabase =
        Room.databaseBuilder(context, UserDatabase::class.java, UserDatabase.FILE_NAME)
            .addMigrations(UserDatabase.MIGRATION_1_2)
            .build()

    @Provides
    fun providePlayStatsDao(db: UserDatabase): PlayStatsDao = db.playStatsDao()

    @Provides
    fun provideFavoriteDao(db: UserDatabase): FavoriteDao = db.favoriteDao()
}
