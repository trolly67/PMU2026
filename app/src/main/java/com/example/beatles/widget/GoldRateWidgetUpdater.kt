package com.example.beatles.widget

import android.content.Context

object GoldRateWidgetUpdater {
suspend fun updateAll(context: Context) {
    GoldRateWidget().updateAll(context)
}
}