package com.example.beatles.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.beatles.domain.GoldRate
import java.time.LocalDate

@Entity(tableName = "gold_rate")
data class GoldRateEntity(
    @PrimaryKey val id: Int = 1,      // единственная строка
    val priceRubPerGram: String,      // BigDecimal как строка — без потерь
    val rateDateEpochDay: Long,
    val fetchedAtEpochMillis: Long
)

fun GoldRateEntity.toDomain() = GoldRate(
    priceRubPerGram = priceRubPerGram.toBigDecimal(),
    rateDate = LocalDate.ofEpochDay(rateDateEpochDay),
    fetchedAtEpochMillis = fetchedAtEpochMillis
)

fun GoldRate.toEntity() = GoldRateEntity(
    priceRubPerGram = priceRubPerGram.toPlainString(),
    rateDateEpochDay = rateDate.toEpochDay(),
    fetchedAtEpochMillis = fetchedAtEpochMillis
)