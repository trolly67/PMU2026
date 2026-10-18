package com.example.beatles

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.beatles.data.RecordRow

class RecordsAdapter : RecyclerView.Adapter<RecordsAdapter.ViewHolder>() {

    private var records: List<RecordRow> = emptyList()

    fun submitList(newRecords: List<RecordRow>) {
        records = newRecords
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = records[position]
        holder.name.text = record.playerName
        holder.score.text = "Очки: ${record.score}"
        holder.difficulty.text = "Сложность: ${record.difficulty} | Точность: ${record.accuracy}%"
    }

    override fun getItemCount(): Int = records.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.recordNameText)
        val score: TextView = view.findViewById(R.id.recordScoreText)
        val difficulty: TextView = view.findViewById(R.id.recordDifficultyText)
    }
}