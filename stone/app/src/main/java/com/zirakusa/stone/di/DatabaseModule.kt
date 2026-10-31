package com.zirakusa.stone.di

import android.content.Context
import androidx.room.Room
import com.zirakusa.stone.data.local.db.LibraryDatabase
import com.zirakusa.stone.data.local.db.dao.SongDao
import com.zirakusa.stone.data.local.db.dao.VideoDao
import com.zirakusa.stone.data.user.FavoriteDao
import com.zirakusa.stone.data.user.PlayStatsDao
import com.zirakusa.stone.data.user.UserDatabase
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
    fun provideDatabase(@ApplicationContext context: Context): LibraryDatabase =
        Room.databaseBuilder(context, LibraryDatabase::class.java, "library.db")
            // The table is only a cache of MediaStore: on a schema change, drop and rescan.
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideSongDao(db: LibraryDatabase): SongDao = db.songDao()

    @Provides
    fun provideVideoDao(db: LibraryDatabase): VideoDao = db.videoDao()

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
