package com.example.beatles

import android.content.Context
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

        photo.setImageResource(author.photoResId)
        name.text = author.name

        return view
    }
}