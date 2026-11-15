package com.example.beatles.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.example.beatles.sync.GoldRateSyncWorker

object GoldRefreshAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        GoldRateSyncWorker.syncNow(context)
    }
}