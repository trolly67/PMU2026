package com.example.beatles

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.random.Random

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback, Runnable {

    private var thread: Thread? = null
    private var isRunning = false

    private val bugs = mutableListOf<Bug>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bitmapCache = mutableMapOf<Int, Bitmap>()

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        isRunning = true
        thread = Thread(this)
        thread?.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (bugs.isEmpty()) {
            spawnBugs(count = 5)
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isRunning = false
        thread?.join()
        thread = null
    }

    override fun run() {
        while (isRunning) {
            update()
            draw()
            Thread.sleep(16)
        }
    }

    private fun update() {
        val w = width.toFloat()
        val h = height.toFloat()
        for (bug in bugs) {
            bug.x += bug.vx
            bug.y += bug.vy
            if (bug.x < 0 || bug.x + bug.size > w) bug.vx = -bug.vx
            if (bug.y < 0 || bug.y + bug.size > h) bug.vy = -bug.vy
        }
    }

    private fun draw() {
        val canvas: Canvas = holder.lockCanvas() ?: return
        try {
            canvas.drawColor(Color.WHITE)
            for (bug in bugs) {
                val bitmap = getBugBitmap(bug)
                canvas.drawBitmap(bitmap, bug.x, bug.y, paint)
            }
        } finally {
            holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun getBugBitmap(bug: Bug): Bitmap {
        return bitmapCache.getOrPut(bug.imageResId) {
            val original = BitmapFactory.decodeResource(resources, bug.imageResId)
            Bitmap.createScaledBitmap(original, bug.size.toInt(), bug.size.toInt(), false)
        }
    }

    private fun spawnBugs(count: Int) {
        val w = width.toFloat()
        val h = height.toFloat()
        val images = listOf(R.drawable.bug1, R.drawable.bug2, R.drawable.bug3)

        repeat(count) {
            bugs.add(
                Bug(
                    x = Random.nextFloat() * (w - 100f),
                    y = Random.nextFloat() * (h - 100f),
                    vx = (Random.nextFloat() - 0.5f) * 10f,
                    vy = (Random.nextFloat() - 0.5f) * 10f,
                    size = 100f,
                    imageResId = images.random()
                )
            )
        }
    }
}