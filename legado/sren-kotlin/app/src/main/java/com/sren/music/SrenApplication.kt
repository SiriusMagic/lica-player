package com.sren.music

import android.app.Application
import com.sren.music.di.AppContainer

class SrenApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
