package com.aegislink

import android.app.Application
import com.aegislink.util.AppContainer

class AegisApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
