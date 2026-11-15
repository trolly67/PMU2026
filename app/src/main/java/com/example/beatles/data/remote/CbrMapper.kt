package com.example.beatles.data.remote

import com.example.beatles.domain.GoldRate
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy")

fun CbrMetalsResponse.toLatestGoldRate(fetchedAt: Long): GoldRate? {
val latest = records
        .asSequence()
        .filter { it.code == "1" && !it.date.isNullOrBlank() && !it.buy.isNullOrBlank() }
        .mapNotNull { rec ->
        runCatching {
    val d = LocalDate.parse(rec.date, FMT)
    val p = rec.buy!!.replace(',', '.').toBigDecimalOrNull()
    if (d != null && p != null) d to p else null
}.getOrNull()
}
        .maxByOrNull { it.first }
        ?: return null
                return GoldRate(
        priceRubPerGram = latest.second,
        rateDate = latest.first,
        fetchedAtEpochMillis = fetchedAt
)
}