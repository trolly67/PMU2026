package com.example.beatles.domain

import java.time.LocalDate

enum class Gender {
    MALE, FEMALE
}

enum class ZodiacSign {
    ARIES, TAURUS, GEMINI, CANCER, LEO, VIRGO,
    LIBRA, SCORPIO, SAGITTARIUS, CAPRICORN, AQUARIUS, PISCES
}

data class PlayerProfile(
    val fullName: String,
    val gender: Gender,
    val course: Int,
    val difficulty: Int,
    val birthDate: LocalDate,
    val zodiac: ZodiacSign
)

sealed interface ProfileResult {
    data class Success(val profile: PlayerProfile) : ProfileResult
    data class Error(val message: String) : ProfileResult
}

fun String.normalizedName(): String =
    trim().replace(Regex("\\s+"), " ")