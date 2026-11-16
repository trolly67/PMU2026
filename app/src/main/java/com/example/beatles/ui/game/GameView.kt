package com.example.beatles.ui.game

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.beatles.R
import com.example.beatles.domain.Bonus
import com.example.beatles.domain.Bug
import com.example.beatles.domain.BugType
import com.example.beatles.util.calculateInSampleSize

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    interface Listener {
        fun onTouchAt(logicalX: Float, logicalY: Float)
    }

    var listener: Listener? = null
    private val paint = Paint()
    private val scaledBitmaps = mutableMapOf<Long, Bitmap>()

    fun render(bugs: List<Bug>, bonus: Bonus?) {
        val canvas = holder.lockCanvas() ?: return
        try {
            canvas.drawColor(Color.WHITE)
            val w = width.toFloat()
            val h = height.toFloat()
            val minSide = minOf(w, h)
            for (bug in bugs) {
                val px = (bug.size * minSide).toInt().coerceAtLeast(1)
                val bmp = getScaledBitmap(imageResFor(bug), px)
                canvas.drawBitmap(bmp, bug.x * w, bug.y * h, paint)
            }
            if (bonus != null) {
                val px = (bonus.size * minSide).toInt().coerceAtLeast(1)
                val bmp = getScaledBitmap(R.drawable.bonus, px)
                canvas.drawBitmap(bmp, bonus.x * w, bonus.y * h, paint)
            }
        } finally {
            holder.unlockCanvasAndPost(canvas)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val w = width.toFloat().takeIf { it > 0f } ?: return true
            val h = height.toFloat().takeIf { it > 0f } ?: return true
            listener?.onTouchAt(event.x / w, event.y / h)
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {}
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        scaledBitmaps.clear()
    }
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        scaledBitmaps.values.forEach { it.recycle() }
        scaledBitmaps.clear()
    }

    private fun imageResFor(bug: Bug): Int = when {
        bug.isGolden -> R.drawable.golden_bug
        bug.type == BugType.FAST -> R.drawable.bug2
        bug.type == BugType.RARE -> R.drawable.bug3
        else -> R.drawable.bug1
    }

    private fun getScaledBitmap(resId: Int, size: Int): Bitmap {
        val key = (resId.toLong() shl 32) or (size.toLong() and 0xFFFFFFFFL)
        scaledBitmaps[key]?.let { return it }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(resources, resId, bounds)
        val opts = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, size)
        }
        val decoded = BitmapFactory.decodeResource(resources, resId, opts)
        val scaled = when {
            decoded == null -> Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            decoded.width == size && decoded.height == size -> decoded
            else -> Bitmap.createScaledBitmap(decoded, size, size, true).also { decoded.recycle() }
        }
        scaledBitmaps[key] = scaled
        return scaled
    }
}