package com.example.beatles

import android.content.Intent
import android.os.Bundle
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import com.example.beatles.databinding.ActivityGameBinding

class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.gameView.onScoreChanged = { score ->
            binding.scoreText.text = getString(R.string.score_format, score)
        }

        binding.menuButton.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.game_menu, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_settings -> openMainTab(3)  // Настройки
                    R.id.menu_authors -> openMainTab(2)   // Авторы
                    R.id.menu_rules -> openMainTab(1)     // Правила
                    R.id.menu_records -> openMainTab(4)   // Рекорды (позже)
                    else -> return@setOnMenuItemClickListener false
                }
                true
            }
            popup.show()
        }
    }

    private fun openMainTab(position: Int) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("tab_position", position)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }
}