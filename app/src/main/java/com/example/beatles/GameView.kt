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
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bitmapCache = mutableMapOf<Int, Bitmap>()

    private var nextBugId = 1L
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

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        isRunning = true
        lastFrameTime = System.currentTimeMillis()
        thread = Thread(this)
        thread?.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        synchronized(bugs) {
            if (bugs.isEmpty()) {
                repeat(5) { spawnBug() }
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
            val now = System.currentTimeMillis()
            val dt = (now - lastFrameTime) / 1000f
            lastFrameTime = now

            if (!isGameOver) {
                timeLeftMs -= (dt * 1000).toLong()
                if (timeLeftMs <= 0) {
                    isGameOver = true
                    post { onGameOver?.invoke(score, hits, misses) }
                }
            }
            val secondsLeft = (timeLeftMs / 1000).toInt()
            post { onTimerChanged?.invoke(secondsLeft) }
            update(dt)
            draw()
            Thread.sleep(16)
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

            synchronized(bugs) {
                for (bug in bugs) {
                    val bitmap = getBugBitmap(bug)
                    val pixelX = bug.x * w
                    val pixelY = bug.y * h
                    val pixelSize = bug.size * minOf(w, h)

                    val scaled = Bitmap.createScaledBitmap(
                        bitmap, pixelSize.toInt(), pixelSize.toInt(), false
                    )
                    canvas.drawBitmap(scaled, pixelX, pixelY, paint)
                }
            }
        } finally {
            holder.unlockCanvasAndPost(canvas)
        }
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
                synchronized(bugs) { spawnBug() }
            } else {
                score -= 5
                misses++
                onScoreChanged?.invoke(score)
            }
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun getBugBitmap(bug: Bug): Bitmap {
        return bitmapCache.getOrPut(bug.imageResId) {
            BitmapFactory.decodeResource(resources, bug.imageResId)
        }
    }

    private fun spawnBug() {
        val type = pickBugType()
        val imageRes = when (type) {
            BugType.NORMAL -> R.drawable.bug1
            BugType.FAST -> R.drawable.bug2
            BugType.RARE -> R.drawable.bug3
        }

        val baseSpeed = 0.15f
        val speed = baseSpeed * type.speedMultiplier
        val angle = Random.nextFloat() * 2f * Math.PI
        val vx = (Math.cos(angle).toFloat()) * speed
        val vy = (Math.sin(angle).toFloat()) * speed

        bugs.add(
            Bug(
                id = nextBugId++,
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