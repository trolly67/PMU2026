package com.example.beatles.domain

import java.math.BigDecimal
import java.time.LocalDate

data class GoldRate(
    val priceRubPerGram: BigDecimal,
    val rateDate: LocalDate,
    val fetchedAtEpochMillis: Long
)

sealed interface GoldRateResult {
    data class Success(val rate: GoldRate) : GoldRateResult
    data class Error(val error: GoldRateError) : GoldRateResult
}

sealed interface GoldRateError {
    data object Offline : GoldRateError
    data object ServiceUnavailable : GoldRateError
    data object InvalidData : GoldRateError
    data object RateNotFound : GoldRateError
}