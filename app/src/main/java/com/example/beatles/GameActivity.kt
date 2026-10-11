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

        binding.gameView.onTimerChanged = { seconds ->
            binding.timerText.text = getString(R.string.timer_format, seconds)
        }

        binding.gameView.onGameOver = { score, hits, misses ->
            showGameOver(score, hits, misses)
        }

        binding.restartButton.setOnClickListener {
            restartGame()
        }

        binding.menuButton.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.game_menu, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_settings -> openMainTab(3)
                    R.id.menu_authors -> openMainTab(2)
                    R.id.menu_rules -> openMainTab(1)
                    R.id.menu_records -> openMainTab(4)
                    else -> return@setOnMenuItemClickListener false
                }
                true
            }
            popup.show()
        }

        binding.gameView.applySettings(
            speed = GameSettings.getSpeed(this),
            maxBugs = GameSettings.getMaxBugs(this),
            roundDurationSec = GameSettings.getRoundDuration(this)
        )
    }

    private fun showGameOver(score: Int, hits: Int, misses: Int) {
        val total = hits + misses
        val accuracy = if (total > 0) (hits * 100 / total) else 0

        binding.finalScoreText.text = getString(R.string.final_score_format, score)
        binding.finalHitsText.text = getString(R.string.final_hits_format, hits)
        binding.finalMissesText.text = getString(R.string.final_misses_format, misses)
        binding.finalAccuracyText.text = getString(R.string.final_accuracy_format, accuracy)

        binding.gameOverLayout.visibility = android.view.View.VISIBLE
    }

    private fun restartGame() {
        binding.gameOverLayout.visibility = android.view.View.GONE
        recreate()
    }

    private fun openMainTab(position: Int) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("tab_position", position)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }
}