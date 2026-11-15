package com.example.beatles.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.beatles.R
import com.example.beatles.domain.GoldRate
import com.example.beatles.repository.GoldRateRepositoryProvider
import java.time.format.DateTimeFormatter

private val DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy")

class GoldRateWidget : GlanceAppWidget() {
    override val sizeMode = androidx.glance.appwidget.SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val rate = try {
            GoldRateRepositoryProvider.get(context).getLastKnown()
        } catch (e: Exception) {
            null
        }

        provideContent {
            GoldRateWidgetContent(rate)
        }
    }
}

@Composable
fun GoldRateWidgetContent(rate: GoldRate?) {
    Box(
        modifier = GlanceModifier
            .size(130.dp)
            .background(ImageProvider(R.drawable.bg_widget_circle))
            .padding(8.dp)
            .clickable(actionRunCallback<GoldRefreshAction>()),
        contentAlignment = Alignment.Center
    ) {
        if (rate == null) {
            Text(
                text = "Нет данных",
                style = TextStyle(
                    color = ColorProvider(Color(0xFF000000)),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${rate.priceRubPerGram.toBigInteger()} ₽/г",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF000000)),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(GlanceModifier.height(4.dp))
                Text(
                    text = rate.rateDate.format(DATE_FMT),
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF5D4037)),
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}