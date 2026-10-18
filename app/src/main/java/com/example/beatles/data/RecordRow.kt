package com.example.beatles.data

data class RecordRow(
    val sessionId: Long,
    val playerName: String,
    val score: Int,
    val difficulty: Int,
    val hits: Int,
    val misses: Int,
    val finishedAtEpochMillis: Long
) {
    val accuracy: Int
        get() {
            val total = hits + misses
            return if (total > 0) (hits * 100 / total) else 0
        }
}