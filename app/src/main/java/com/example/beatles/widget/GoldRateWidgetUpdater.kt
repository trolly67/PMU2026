package com.example.beatles.widget

import android.content.Context
import androidx.glance.appwidget.updateAll

object GoldRateWidgetUpdater {
    suspend fun updateAll(context: Context) {
        GoldRateWidget().updateAll(context)
    }
}