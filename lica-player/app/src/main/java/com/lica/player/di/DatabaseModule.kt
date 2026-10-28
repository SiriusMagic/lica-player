package com.lica.player.di

import android.content.Context
import androidx.room.Room
import com.lica.player.data.local.db.LicaDatabase
import com.lica.player.data.local.db.dao.SongDao
import com.lica.player.data.local.db.dao.VideoDao
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
}
