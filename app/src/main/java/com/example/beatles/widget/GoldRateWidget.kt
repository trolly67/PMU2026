package com.example.beatles.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.*
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.example.beatles.domain.GoldRate
import com.example.beatles.repository.GoldRateRepositoryProvider
import java.time.format.DateTimeFormatter

private val DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy")

class GoldRateWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val rate = GoldRateRepositoryProvider.get(context).getLastKnown()
        provideContent { GoldRateWidgetContent(rate) }
    }
}
@Preview
@Composable
private fun GoldRateWidgetPreview() = GoldRateWidgetContent(
    GoldRate(java.math.BigDecimal("10708.86"), java.time.LocalDate.now(), 0L)
)
@Composable
fun GoldRateWidgetContent(rate: GoldRate?) {
    Box(
        modifier = GlanceModifier
            .size(160.dp)
            .cornerRadius(80.dp)
            .background(ColorProvider(Color(0xFFFFD54F)))
            .padding(12.dp)
            .clickAction(ActionCallback.GoldRefreshAction()),
        contentAlignment = Alignment.Center
    ) {
        if (rate == null) {
            Text(
                "Нет данных",
                style = TextStyle(color = ColorProvider(Color(0xFF5D4037)), fontSize = 14.sp)
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${rate.priceRubPerGram.toBigInteger()} ₽/г",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF3E2723)),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(GlanceModifier.height(4.dp))
                Text(
                    rate.rateDate.format(DATE_FMT),
                    style = TextStyle(color = ColorProvider(Color(0xFF5D4037)), fontSize = 12.sp)
                )
            }
        }
    }
}