package com.example.beatles.repository

import android.content.Context
import com.example.beatles.data.AppDatabase
import com.example.beatles.data.remote.CbrClient

object GoldRateRepositoryProvider {
    @Volatile private var instance: GoldRateRepository? = null

    fun get(context: Context): GoldRateRepository =
        instance ?: synchronized(this) {
            instance ?: GoldRateRepository(
                api = CbrClient.api,
                dao = AppDatabase.get(context).goldRateDao()
            ).also { instance = it }
        }
}