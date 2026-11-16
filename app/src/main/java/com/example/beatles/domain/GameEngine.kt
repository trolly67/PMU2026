package com.example.beatles.domain

import kotlin.math.hypot
import kotlin.random.Random

data class EngineConfig(
    val baseSpeed: Float,
    val maxBugs: Int,
    val bonusIntervalMs: Long
)

data class EngineTick(
    val score: Int,
    val hits: Int,
    val misses: Int,
    val timeLeftMs: Long
)

class GameEngine(
    private val config: EngineConfig,
    private val random: Random = Random.Default
) {
    private val bugs = mutableListOf<Bug>()
    var bonus: Bonus? = null
        private set

    var score: Int = 0
        private set
    var hits: Int = 0
        private set
    var misses: Int = 0
        private set

    var timeLeftMs: Long = 0L
        private set

    private var bonusTimerMs = 0L
    private var bonusElapsedMs = 0L
    private var goldenBugTimerMs = 0L

    fun start(timeLeftMs: Long) {
        this.timeLeftMs = timeLeftMs
        bugs.clear()
        repeat(config.maxBugs) { spawnBug() }
    }

    fun tick(dtSec: Float): EngineTick {
        timeLeftMs = (timeLeftMs - (dtSec * 1000).toLong()).coerceAtLeast(0L)
        moveBugs(dtSec)
        tickBonus(dtSec)
        tickGoldenBug(dtSec)
        return EngineTick(score, hits, misses, timeLeftMs)
    }

    fun onTouch(logicalX: Float, logicalY: Float, goldPrice: () -> java.math.BigDecimal?) {
        val b = bonus
        if (b != null &&
            logicalX in b.x..(b.x + b.size) &&
            logicalY in b.y..(b.y + b.size)
        ) {
            bonus = null
            bonusElapsedMs = 0L
            score += BONUS_SCORE
            return
        }
        val hitBug = bugs.findLast {
            logicalX in it.x..(it.x + it.size) &&
                    logicalY in it.y..(it.y + it.size)
        }
        if (hitBug != null) {
            bugs.remove(hitBug)
            score += if (hitBug.isGolden) {
                goldenBugTimerMs = 0L
                goldScore(goldPrice())
            } else hitBug.scoreValue
            hits++
            if (bugs.size < config.maxBugs) spawnBug()
        } else {
            score -= 5
            misses++
        }
    }

    fun snapshotBugs(): List<Bug> = bugs.toList()

    private fun moveBugs(dt: Float) {
        for (bug in bugs) {
            bug.x += bug.vx * dt
            bug.y += bug.vy * dt
            if (bug.x < 0f) { bug.x = 0f; bug.vx = -bug.vx }
            if (bug.x + bug.size > 1f) { bug.x = 1f - bug.size; bug.vx = -bug.vx }
            if (bug.y < 0f) { bug.y = 0f; bug.vy = -bug.vy }
            if (bug.y + bug.size > 1f) { bug.y = 1f - bug.size; bug.vy = -bug.vy }
        }
    }

    private fun tickBonus(dtSec: Float) {
        val b = bonus
        if (b == null) {
            bonusTimerMs += (dtSec * 1000).toLong()
            if (bonusTimerMs >= config.bonusIntervalMs) {
                bonusTimerMs = 0L
                bonus = Bonus(
                    x = random.nextFloat() * 0.85f,
                    y = random.nextFloat() * 0.85f,
                    size = BONUS_SIZE,
                    imageResId = 0
                )
            }
        } else {
            bonusElapsedMs += (dtSec * 1000).toLong()
            if (bonusElapsedMs >= BONUS_LIFETIME_MS) {
                bonus = null
                bonusElapsedMs = 0L
            }
        }
    }

    private fun tickGoldenBug(dtSec: Float) {
        if (bugs.any { it.isGolden }) return
        goldenBugTimerMs += (dtSec * 1000).toLong()
        if (goldenBugTimerMs >= GOLDEN_BUG_INTERVAL_MS) {
            goldenBugTimerMs = 0L
            spawnGoldenBug()
        }
    }

    private fun spawnBug() {
        val type = pickBugType()
        val speed = config.baseSpeed * type.speedMultiplier
        val angle = random.nextFloat() * 2f * Math.PI.toFloat()
        bugs += Bug(
            type = type,
            x = random.nextFloat() * 0.9f,
            y = random.nextFloat() * 0.9f,
            vx = Math.cos(angle.toDouble()).toFloat() * speed,
            vy = Math.sin(angle.toDouble()).toFloat() * speed,
            baseSize = 0.08f,
            imageResId = 0
        )
    }

    private fun spawnGoldenBug() {
        val speed = config.baseSpeed * 1.2f
        val angle = random.nextFloat() * 2f * Math.PI.toFloat()
        bugs += Bug(
            type = BugType.NORMAL,
            x = random.nextFloat() * 0.85f,
            y = random.nextFloat() * 0.85f,
            vx = Math.cos(angle.toDouble()).toFloat() * speed,
            vy = Math.sin(angle.toDouble()).toFloat() * speed,
            baseSize = GOLDEN_BUG_SIZE,
            imageResId = 0,
            isGolden = true
        )
    }

    private fun pickBugType(): BugType {
        val total = BugType.values().sumOf { it.spawnWeight }
        var r = random.nextInt(total)
        for (t in BugType.values()) {
            r -= t.spawnWeight
            if (r < 0) return t
        }
        return BugType.NORMAL
    }

    private companion object {
        private const val BONUS_LIFETIME_MS = 5_000L
        private const val BONUS_SIZE = 0.12f
        private const val BONUS_SCORE = 100
        private const val GOLDEN_BUG_INTERVAL_MS = 20_000L
        private const val GOLDEN_BUG_SIZE = 0.10f
    }
}