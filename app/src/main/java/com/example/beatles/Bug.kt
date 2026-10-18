package com.example.beatles

enum class BugType(
    val speedMultiplier: Float,
    val sizeMultiplier: Float,
    val scoreValue: Int,
    val spawnWeight: Int
) {
    NORMAL(1.0f, 1.0f, 10, 70),
    FAST(2.0f, 0.7f, 20, 25),
    RARE(1.5f, 1.5f, 50, 5)
}

data class Bug(
    val id: Long,
    val type: BugType,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val baseSize: Float,
    val imageResId: Int
) {
    val size: Float get() = baseSize * type.sizeMultiplier
    val scoreValue: Int get() = type.scoreValue
}