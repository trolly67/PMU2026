package com.example.beatles

enum class DifficultyPreset(
    val label: String,
    val speed: Int,
    val maxBugs: Int,
    val roundDuration: Int
) {
    EASY("Легко", 1, 3, 2),
    NORMAL("Норма", 2, 5, 3),
    HARD("Сложно", 3, 8, 4),
    INSANE("Безумие", 4, 12, 5),
    HARDCORE("Хардкор", 5, 20, 6),
    CUSTOM("Custom", 0, 0, 0);

    companion object {
        fun fromIndex(index: Int): DifficultyPreset =
            values().getOrElse(index) { NORMAL }
    }
}