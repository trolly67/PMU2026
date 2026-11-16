package com.example.beatles

import android.app.Application
import com.example.beatles.sync.GoldRateSyncWorker
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module

class BeatlesApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@BeatlesApp)
            modules(emptyList())
        }

        GoldRateSyncWorker.schedulePeriodic(this)
        GoldRateSyncWorker.syncNow(this)
    }
}