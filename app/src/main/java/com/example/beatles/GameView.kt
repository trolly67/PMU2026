package com.example.beatles

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.random.Random

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback, Runnable {

    private var thread: Thread? = null
    @Volatile private var isRunning = false
    @Volatile private var isGameOver = false

    private val bugs = mutableListOf<Bug>()
    private val paint = Paint(Paint.FILTER_BITMAP_FLAG)

    private val originalBitmaps = mutableMapOf<Int, Bitmap>()
    private val scaledBitmaps = mutableMapOf<Pair<Int, Int>, Bitmap>()

    private var lastFrameTime = 0L

    var score: Int = 0
        private set
    var hits: Int = 0
        private set
    var misses: Int = 0
        private set

    var onScoreChanged: ((Int) -> Unit)? = null
    var onGameOver: ((score: Int, hits: Int, misses: Int) -> Unit)? = null
    var onTimerChanged: ((Int) -> Unit)? = null

    private var roundDurationSec: Int = 60
    private var timeLeftMs: Long = roundDurationSec * 1000L

    private var baseSpeed = 0.15f
    private var maxBugs = 5

    private var lastPostedSecond = -1

    init {
        holder.addCallback(this)
    }

    fun applySettings(speed: Int, maxBugs: Int, roundDurationSec: Int) {
        this.baseSpeed = 0.05f * speed
        this.maxBugs = maxBugs
        this.roundDurationSec = roundDurationSec * 60
        this.timeLeftMs = this.roundDurationSec * 1000L
        this.isGameOver = false
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        isRunning = true
        lastFrameTime = System.currentTimeMillis()
        thread = Thread(this).also { it.start() }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        scaledBitmaps.clear()
        synchronized(bugs) {
            if (bugs.isEmpty()) {
                repeat(maxBugs) { spawnBug() }
            }
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isRunning = false
        thread?.join()
        thread = null
    }

    override fun run() {
        while (isRunning) {
            val frameStart = System.currentTimeMillis()
            val dt = (frameStart - lastFrameTime) / 1000f
            lastFrameTime = frameStart

            if (!isGameOver) {
                timeLeftMs -= (dt * 1000).toLong()
                if (timeLeftMs <= 0) {
                    timeLeftMs = 0
                    isGameOver = true
                    post { onGameOver?.invoke(score, hits, misses) }
                }
            }

            val secondsLeft = (timeLeftMs / 1000).toInt()
            if (secondsLeft != lastPostedSecond) {
                lastPostedSecond = secondsLeft
                post { onTimerChanged?.invoke(secondsLeft) }
            }

            update(dt)
            draw()

            val frameTime = System.currentTimeMillis() - frameStart
            val sleepTime = 16 - frameTime
            if (sleepTime > 0) Thread.sleep(sleepTime)
        }
    }

    private fun update(dt: Float) {
        synchronized(bugs) {
            for (bug in bugs) {
                bug.x += bug.vx * dt
                bug.y += bug.vy * dt

                if (bug.x < 0f) { bug.x = 0f; bug.vx = -bug.vx }
                if (bug.x + bug.size > 1f) { bug.x = 1f - bug.size; bug.vx = -bug.vx }
                if (bug.y < 0f) { bug.y = 0f; bug.vy = -bug.vy }
                if (bug.y + bug.size > 1f) { bug.y = 1f - bug.size; bug.vy = -bug.vy }
            }
        }
    }

    private fun draw() {
        val canvas: Canvas = holder.lockCanvas() ?: return
        try {
            canvas.drawColor(Color.WHITE)

            val w = width.toFloat()
            val h = height.toFloat()
            val minSide = minOf(w, h)

            synchronized(bugs) {
                for (bug in bugs) {
                    val pixelSize = (bug.size * minSide).toInt().coerceAtLeast(1)
                    val bitmap = getScaledBitmap(bug.imageResId, pixelSize)
                    canvas.drawBitmap(bitmap, bug.x * w, bug.y * h, paint)
                }
            }
        } finally {
            holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun getScaledBitmap(resId: Int, size: Int): Bitmap {
        val key = resId to size
        scaledBitmaps[key]?.let { return it }

        val original = originalBitmaps.getOrPut(resId) {
            BitmapFactory.decodeResource(resources, resId)
        }
        val scaled = Bitmap.createScaledBitmap(original, size, size, true)
        scaledBitmaps[key] = scaled
        return scaled
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && !isGameOver) {
            val w = width.toFloat()
            val h = height.toFloat()
            val logicalX = event.x / w
            val logicalY = event.y / h

            var hitBug: Bug? = null
            synchronized(bugs) {
                hitBug = bugs.findLast { bug ->
                    logicalX >= bug.x && logicalX <= bug.x + bug.size &&
                            logicalY >= bug.y && logicalY <= bug.y + bug.size
                }
                if (hitBug != null) bugs.remove(hitBug)
            }

            if (hitBug != null) {
                score += hitBug!!.scoreValue
                hits++
                onScoreChanged?.invoke(score)
                synchronized(bugs) {
                    if (bugs.size < maxBugs) spawnBug()
                }
            } else {
                score -= 5
                misses++
                onScoreChanged?.invoke(score)
            }
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun spawnBug() {
        val type = pickBugType()
        val imageRes = when (type) {
            BugType.NORMAL -> R.drawable.bug1
            BugType.FAST -> R.drawable.bug2
            BugType.RARE -> R.drawable.bug3
        }

        val speed = baseSpeed * type.speedMultiplier
        val angle = Random.nextFloat() * 2f * Math.PI
        val vx = (Math.cos(angle).toFloat()) * speed
        val vy = (Math.sin(angle).toFloat()) * speed

        bugs.add(
            Bug(
                type = type,
                x = Random.nextFloat() * 0.9f,
                y = Random.nextFloat() * 0.9f,
                vx = vx,
                vy = vy,
                baseSize = 0.08f,
                imageResId = imageRes
            )
        )
    }

    private fun pickBugType(): BugType {
        val totalWeight = BugType.values().sumOf { it.spawnWeight }
        var r = Random.nextInt(totalWeight)
        for (type in BugType.values()) {
            r -= type.spawnWeight
            if (r < 0) return type
        }
        return BugType.NORMAL
    }
}