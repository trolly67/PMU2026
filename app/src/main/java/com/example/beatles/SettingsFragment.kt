package com.example.beatles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.beatles.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private var isUpdatingUi = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = requireContext()

        val presetLabels = DifficultyPreset.values().map { it.label }
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, presetLabels)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.settingsPresetSpinner.adapter = spinnerAdapter

        binding.settingsPresetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isUpdatingUi) return

                if (GameSettings.getPresetIndex(ctx) != position) {
                    GameSettings.setPresetIndex(ctx, position)
                }

                isUpdatingUi = true
                updateSeekBarsFromSettings()
                toggleSeekBarsEnabled(position == 5)
                isUpdatingUi = false
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        setupSeekBars()

        isUpdatingUi = true
        binding.settingsPresetSpinner.setSelection(GameSettings.getPresetIndex(ctx))
        updateSeekBarsFromSettings()
        toggleSeekBarsEnabled(GameSettings.getPresetIndex(ctx) == 5)
        isUpdatingUi = false
    }

    private fun updateSeekBarsFromSettings() {
        val ctx = requireContext()
        binding.speedSeekBar.progress = GameSettings.getSpeed(ctx) - 1
        binding.maxBugsSeekBar.progress = GameSettings.getMaxBugs(ctx) - 1
        binding.bonusIntervalSeekBar.progress = GameSettings.getBonusInterval(ctx) - 1
        binding.roundDurationSeekBar.progress = GameSettings.getRoundDuration(ctx) - 1
    }

    private fun toggleSeekBarsEnabled(enabled: Boolean) {
        binding.speedSeekBar.isEnabled = enabled
        binding.maxBugsSeekBar.isEnabled = enabled
        binding.roundDurationSeekBar.isEnabled = enabled
        binding.bonusIntervalSeekBar.isEnabled = enabled
    }

    private fun setupSeekBars() {
        val ctx = requireContext()

        setupSeekBar(binding.speedSeekBar, binding.speedLabel, R.string.settings_speed_format) { p ->
            if (!isUpdatingUi && binding.speedSeekBar.isEnabled) {
                GameSettings.setSpeed(ctx, p + 1)
                checkCustom()
            }
            p + 1
        }
        setupSeekBar(binding.maxBugsSeekBar, binding.maxBugsLabel, R.string.settings_max_bugs_format) { p ->
            if (!isUpdatingUi && binding.maxBugsSeekBar.isEnabled) {
                GameSettings.setMaxBugs(ctx, p + 1)
                checkCustom()
            }
            p + 1
        }
        setupSeekBar(binding.roundDurationSeekBar, binding.roundDurationLabel, R.string.settings_round_duration_format) { p ->
            if (!isUpdatingUi && binding.roundDurationSeekBar.isEnabled) {
                GameSettings.setRoundDuration(ctx, p + 1)
                checkCustom()
            }
            p + 1
        }
        setupSeekBar(binding.bonusIntervalSeekBar, binding.bonusIntervalLabel, R.string.settings_bonus_interval_format) { p ->
            if (!isUpdatingUi && binding.bonusIntervalSeekBar.isEnabled) {
                GameSettings.setBonusInterval(ctx, p + 1)
                checkCustom()
            }
            p + 1
        }
    }

    private fun checkCustom() {
        if (GameSettings.getPresetIndex(requireContext()) != 5) {
            GameSettings.setCustomFlag(requireContext())
            isUpdatingUi = true
            binding.settingsPresetSpinner.setSelection(5)
            isUpdatingUi = false
        }
    }

    private fun setupSeekBar(seekBar: SeekBar, label: TextView, formatResId: Int, valueMapper: (Int) -> Int) {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                label.text = getString(formatResId, valueMapper(progress))
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
        label.text = getString(formatResId, valueMapper(seekBar.progress))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}