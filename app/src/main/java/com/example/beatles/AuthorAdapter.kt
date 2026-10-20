package com.example.beatles

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class AuthorAdapter(
    context: Context,
    private val authors: List<Author>
) : ArrayAdapter<Author>(context, 0, authors) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_author, parent, false)

        val author = authors[position]

        val photo = view.findViewById<ImageView>(R.id.authorPhoto)
        val name = view.findViewById<TextView>(R.id.authorName)

        photo.setImageBitmap(getScaledPhoto(author.photoResId))
        name.text = author.name

        return view
    }

    private fun getScaledPhoto(resId: Int): Bitmap? {
        photoCache[resId]?.let { return it }

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(context.resources, resId, bounds)

        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, PHOTO_MAX_PX)
        }
        val bitmap = BitmapFactory.decodeResource(context.resources, resId, options)
        if (bitmap != null) {
            photoCache[resId] = bitmap
        }
        return bitmap
    }

    private companion object {
        private const val PHOTO_MAX_PX = 160
        private val photoCache = mutableMapOf<Int, Bitmap>()
    }
}