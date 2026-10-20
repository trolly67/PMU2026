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

    private val drawSnapshot = ArrayList<Bug>()

    private val paint = Paint()

    private val scaledBitmaps = mutableMapOf<Long, Bitmap>()

    private var lastFrameTimeNs = 0L

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
        lastFrameTimeNs = System.nanoTime()
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
            val frameStartNs = System.nanoTime()
            var dt = (frameStartNs - lastFrameTimeNs) / 1_000_000_000f
            lastFrameTimeNs = frameStartNs
            if (dt > MAX_FRAME_DT) dt = MAX_FRAME_DT

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

            val elapsedMs = (System.nanoTime() - frameStartNs) / 1_000_000L
            val sleepMs = FRAME_INTERVAL_MS - elapsedMs
            if (sleepMs > 0) {
                try {
                    Thread.sleep(sleepMs)
                } catch (_: InterruptedException) {
                    break
                }
            }
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

            synchronized(bugs) {
                drawSnapshot.clear()
                drawSnapshot.addAll(bugs)
            }

            val w = width.toFloat()
            val h = height.toFloat()
            val minSide = minOf(w, h)

            for (bug in drawSnapshot) {
                val pixelSize = (bug.size * minSide).toInt().coerceAtLeast(1)
                val bitmap = getScaledBitmap(bug.imageResId, pixelSize)
                canvas.drawBitmap(bitmap, bug.x * w, bug.y * h, paint)
            }
        } finally {
            holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun getScaledBitmap(resId: Int, size: Int): Bitmap {
        val key = (resId.toLong() shl 32) or (size.toLong() and 0xFFFFFFFFL)
        scaledBitmaps[key]?.let { return it }

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(resources, resId, bounds)

        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, size)
        }
        val decoded = BitmapFactory.decodeResource(resources, resId, options)

        val scaled = when {
            decoded == null -> Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            decoded.width == size && decoded.height == size -> decoded
            else -> Bitmap.createScaledBitmap(decoded, size, size, true).also { decoded.recycle() }
        }
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

    private companion object {
        private const val FRAME_INTERVAL_MS = 16L
        private const val MAX_FRAME_DT = 0.05f
    }
}