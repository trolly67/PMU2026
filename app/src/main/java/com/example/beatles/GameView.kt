package com.example.beatles

import android.content.Context
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
        // Останавливаем игровой поток
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
                val bitmap = android.graphics.BitmapFactory.decodeResource(
                    resources, bug.imageResId
                )
                val scaled = android.graphics.Bitmap.createScaledBitmap(
                    bitmap, bug.size.toInt(), bug.size.toInt(), false
                )
                canvas.drawBitmap(scaled, bug.x, bug.y, paint)
            }
        } finally {
            holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun spawnBugs(count: Int) {
        val w = width.toFloat()
        val h = height.toFloat()
        val images = listOf(R.drawable.bug1, R.drawable.bug2)

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