package com.example.beatles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
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

        setupSeekBar(
            binding.speedSeekBar,
            binding.speedLabel,
            R.string.settings_speed_format
        ) { progress -> progress + 1 }

        setupSeekBar(
            binding.maxBugsSeekBar,
            binding.maxBugsLabel,
            R.string.settings_max_bugs_format
        ) { progress -> progress + 1 }

        setupSeekBar(
            binding.bonusIntervalSeekBar,
            binding.bonusIntervalLabel,
            R.string.settings_bonus_interval_format
        ) { progress -> progress + 1 }

        setupSeekBar(
            binding.roundDurationSeekBar,
            binding.roundDurationLabel,
            R.string.settings_round_duration_format
        ) { progress -> progress + 1 }
    }

    private fun setupSeekBar(
        seekBar: SeekBar,
        label: android.widget.TextView,
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}