package com.example.beatles.domain

import java.math.BigDecimal
import java.math.RoundingMode

fun goldScore(price: BigDecimal?): Int {
    if (price == null) return 100
    val raw = price
        .divide(BigDecimal(100), 0, RoundingMode.HALF_UP)
        .toInt()
    return raw.coerceIn(50, 500)
}