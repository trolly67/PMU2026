package com.example.beatles

import android.app.Application
import com.example.beatles.sync.GoldRateSyncWorker

class BeatlesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        GoldRateSyncWorker.schedulePeriodic(this)
        GoldRateSyncWorker.syncNow(this)
    }
}