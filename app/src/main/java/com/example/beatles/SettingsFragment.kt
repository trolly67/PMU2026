package com.example.beatles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.beatles.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = requireContext()

        binding.speedSeekBar.progress = GameSettings.getSpeed(ctx) - 1
        binding.maxBugsSeekBar.progress = GameSettings.getMaxBugs(ctx) - 1
        binding.bonusIntervalSeekBar.progress = GameSettings.getBonusInterval(ctx) - 1
        binding.roundDurationSeekBar.progress = GameSettings.getRoundDuration(ctx) - 1

        setupSeekBar(
            binding.speedSeekBar,
            binding.speedLabel,
            R.string.settings_speed_format
        ) { progress ->
            val value = progress + 1
            GameSettings.setSpeed(ctx, value)
            value
        }

        setupSeekBar(
            binding.maxBugsSeekBar,
            binding.maxBugsLabel,
            R.string.settings_max_bugs_format
        ) { progress ->
            val value = progress + 1
            GameSettings.setMaxBugs(ctx, value)
            value
        }

        setupSeekBar(
            binding.bonusIntervalSeekBar,
            binding.bonusIntervalLabel,
            R.string.settings_bonus_interval_format
        ) { progress ->
            val value = progress + 1
            GameSettings.setBonusInterval(ctx, value)
            value
        }

        setupSeekBar(
            binding.roundDurationSeekBar,
            binding.roundDurationLabel,
            R.string.settings_round_duration_format
        ) { progress ->
            val value = progress + 1
            GameSettings.setRoundDuration(ctx, value)
            value
        }
    }

    private fun setupSeekBar(
        seekBar: SeekBar,
        label: TextView,
        formatResId: Int,
        valueMapper: (Int) -> Int
    ) {
        seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    sb: SeekBar?, progress: Int, fromUser: Boolean
                ) {
                    val value = valueMapper(progress)
                    label.text = getString(formatResId, value)
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            }
        )
        label.text = getString(formatResId, valueMapper(seekBar.progress))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}