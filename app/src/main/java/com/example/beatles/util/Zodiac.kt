package com.example.beatles.util

import com.example.beatles.domain.ZodiacSign

fun zodiacByDate(day: Int, month: Int): ZodiacSign = when {
    (month == 3 && day >= 21) || (month == 4 && day <= 19) -> ZodiacSign.ARIES
    (month == 4 && day >= 20) || (month == 5 && day <= 20) -> ZodiacSign.TAURUS
    (month == 5 && day >= 21) || (month == 6 && day <= 20) -> ZodiacSign.GEMINI
    (month == 6 && day >= 21) || (month == 7 && day <= 22) -> ZodiacSign.CANCER
    (month == 7 && day >= 23) || (month == 8 && day <= 22) -> ZodiacSign.LEO
    (month == 8 && day >= 23) || (month == 9 && day <= 22) -> ZodiacSign.VIRGO
    (month == 9 && day >= 23) || (month == 10 && day <= 22) -> ZodiacSign.LIBRA
    (month == 10 && day >= 23) || (month == 11 && day <= 21) -> ZodiacSign.SCORPIO
    (month == 11 && day >= 22) || (month == 12 && day <= 21) -> ZodiacSign.SAGITTARIUS
    (month == 12 && day >= 22) || (month == 1 && day <= 19) -> ZodiacSign.CAPRICORN
    (month == 1 && day >= 20) || (month == 2 && day <= 18) -> ZodiacSign.AQUARIUS
    else -> ZodiacSign.PISCES
}