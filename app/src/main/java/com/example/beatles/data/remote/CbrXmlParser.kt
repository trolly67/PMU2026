package com.example.beatles.data.remote

import com.example.beatles.domain.GoldRate
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object CbrXmlParser {
    private val FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    fun parseGoldRate(input: InputStream, fetchedAt: Long): GoldRate? {
        val parser = XmlPullParserFactory.newInstance().newPullParser()
        parser.setInput(input, "windows-1251")

        var eventType = parser.eventType
        var latestDate: LocalDate? = null
        var latestPrice: BigDecimal? = null

        var currentDateStr: String? = null
        var currentCode: String? = null
        var currentBuy: String? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name

            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (tagName.equals("Record", ignoreCase = true)) {
                        currentDateStr = parser.getAttributeValue(null, "Date")
                        currentCode = parser.getAttributeValue(null, "Code")
                        currentBuy = null
                    } else if (tagName.equals("Buy", ignoreCase = true)) {
                        currentBuy = parser.nextText()
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (tagName.equals("Record", ignoreCase = true)) {
                        if (currentCode == "1" && !currentBuy.isNullOrBlank() && !currentDateStr.isNullOrBlank()) {
                            try {
                                val date = LocalDate.parse(currentDateStr, FMT) // Парсим "13.11.2026"
                                val price = currentBuy.replace(',', '.').toBigDecimalOrNull()

                                if (date != null && price != null) {
                                    if (latestDate == null || date.isAfter(latestDate)) {
                                        latestDate = date
                                        latestPrice = price
                                    }
                                }
                            } catch (e: Exception) {
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return if (latestDate != null && latestPrice != null) {
            GoldRate(latestPrice!!, latestDate!!, fetchedAt)
        } else null
    }
}