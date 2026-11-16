package com.example.beatles.util

fun calculateInSampleSize(width: Int, height: Int, target: Int): Int {
    var sampleSize = 1
    val largest = maxOf(width, height)
    while (largest / (sampleSize * 2) >= target) {
        sampleSize *= 2
    }
    return sampleSize
}