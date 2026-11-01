package com.example.beatles

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.hypot
import kotlin.random.Random

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback, Runnable, SensorEventListener {

    private var thread: Thread? = null
    @Volatile private var isRunning = false
    @Volatile private var isGameOver = false

    private val bugs = mutableListOf<Bug>()
    private val drawSnapshot = ArrayList<Bug>()

    @Volatile private var bonus: Bonus? = null
    @Volatile private var bonusTimerMs = 0L
    @Volatile private var bonusElapsedMs = 0L
    @Volatile private var bonusIntervalMs = 15_000L

    @Volatile private var tiltModeActive = false
    @Volatile private var tiltModeRemainingMs = 0L
    @Volatile private var tiltX = 0f
    @Volatile private var tiltY = 0f

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(1)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()
    @Volatile private var screamSoundId = 0
    @Volatile private var screamStreamId = 0

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
        screamSoundId = soundPool.load(context, R.raw.bugs_scream, 1)
    }

    fun applySettings(speed: Int, maxBugs: Int, roundDurationSec: Int, bonusIntervalSec: Int) {
        this.baseSpeed = 0.05f * speed
        this.maxBugs = maxBugs
        this.roundDurationSec = roundDurationSec * 60
        this.timeLeftMs = this.roundDurationSec * 1000L
        this.bonusIntervalMs = bonusIntervalSec * 1000L
        this.bonusTimerMs = 0L
        this.bonusElapsedMs = 0L
        this.bonus = null
        this.tiltModeActive = false
        this.tiltModeRemainingMs = 0L
        this.isGameOver = false
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        isRunning = true
        lastFrameTimeNs = System.nanoTime()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
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
        sensorManager.unregisterListener(this)
        stopScream()
        thread?.join()
        thread = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopScream()
        soundPool.release()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            tiltX = event.values[0] / SensorManager.GRAVITY_EARTH
            tiltY = event.values[1] / SensorManager.GRAVITY_EARTH
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
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
        if (tiltModeActive) {
            tiltModeRemainingMs -= (dt * 1000).toLong()
            if (tiltModeRemainingMs <= 0L) {
                tiltModeActive = false
                tiltModeRemainingMs = 0L
                onTiltModeEnded()
            }
        }

        synchronized(bugs) {
            for (bug in bugs) {
                bug.x += bug.vx * dt
                bug.y += bug.vy * dt

                if (tiltModeActive) {
                    bug.vx -= tiltX * TILT_ACCEL * dt
                    bug.vy += tiltY * TILT_ACCEL * dt
                    val maxSpeed = baseSpeed * bug.type.speedMultiplier * TILT_MAX_MULT
                    val speed = hypot(bug.vx, bug.vy)
                    if (speed > maxSpeed) {
                        bug.vx = bug.vx / speed * maxSpeed
                        bug.vy = bug.vy / speed * maxSpeed
                    }
                }

                if (bug.x < 0f) { bug.x = 0f; bug.vx = -bug.vx }
                if (bug.x + bug.size > 1f) { bug.x = 1f - bug.size; bug.vx = -bug.vx }
                if (bug.y < 0f) { bug.y = 0f; bug.vy = -bug.vy }
                if (bug.y + bug.size > 1f) { bug.y = 1f - bug.size; bug.vy = -bug.vy }
            }
        }

        if (!isGameOver) {
            if (bonus == null) {
                bonusTimerMs += (dt * 1000).toLong()
                if (bonusTimerMs >= bonusIntervalMs) {
                    bonusTimerMs = 0L
                    spawnBonus()
                }
            } else {
                bonusElapsedMs += (dt * 1000).toLong()
                if (bonusElapsedMs >= BONUS_LIFETIME_MS) {
                    bonus = null
                    bonusElapsedMs = 0L
                }
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

            bonus?.let { b ->
                val pixelSize = (b.size * minSide).toInt().coerceAtLeast(1)
                val bitmap = getScaledBitmap(b.imageResId, pixelSize)
                canvas.drawBitmap(bitmap, b.x * w, b.y * h, paint)
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

            val b = bonus
            if (b != null &&
                logicalX >= b.x && logicalX <= b.x + b.size &&
                logicalY >= b.y && logicalY <= b.y + b.size
            ) {
                bonus = null
                bonusElapsedMs = 0L
                score += BONUS_SCORE
                onScoreChanged?.invoke(score)
                onBonusActivated()
                return true
            }

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

    private fun onBonusActivated() {
        tiltModeActive = true
        tiltModeRemainingMs = TILT_DURATION_MS
        playScream()
    }

    private fun onTiltModeEnded() {
        stopScream()
    }

    private fun playScream() {
        if (screamSoundId == 0) return
        stopScream()
        screamStreamId = soundPool.play(screamSoundId, 1f, 1f, 1, -1, 1f)
    }

    private fun stopScream() {
        if (screamStreamId != 0) {
            soundPool.stop(screamStreamId)
            screamStreamId = 0
        }
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

    private fun spawnBonus() {
        bonus = Bonus(
            x = Random.nextFloat() * 0.85f,
            y = Random.nextFloat() * 0.85f,
            size = BONUS_SIZE,
            imageResId = BONUS_IMAGE_RES
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
        private const val BONUS_LIFETIME_MS = 5_000L
        private const val BONUS_SIZE = 0.12f
        private const val BONUS_SCORE = 100
        private val BONUS_IMAGE_RES = R.drawable.bonus
        private const val TILT_DURATION_MS = 10_000L
        private const val TILT_ACCEL = 1.5f
        private const val TILT_MAX_MULT = 3f
    }
}