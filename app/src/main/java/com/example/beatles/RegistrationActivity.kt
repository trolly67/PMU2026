package com.example.beatles

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.beatles.databinding.ActivityRegistrationBinding
import java.time.Instant
import java.time.ZoneId

class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCourseSpinner()
        setupDifficultySeekBar()

        binding.submitButton.setOnClickListener {
            when (val result = readProfileFromForm()) {
                is ProfileResult.Success -> showProfile(result.profile)
                is ProfileResult.Error -> binding.resultText.text = result.message
            }
        }
    }

    private fun setupCourseSpinner() {
        val courses = (1..6).map { it.toString() }
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            courses
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.courseSpinner.adapter = adapter
    }

    private fun setupDifficultySeekBar() {
        binding.difficultySeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?, progress: Int, fromUser: Boolean
                ) {
                    binding.difficultyLabel.text =
                        getString(R.string.difficulty_format, progress + 1)
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }
        )
    }

    private fun readProfileFromForm(): ProfileResult {
        val rawName = binding.fullNameInput.text.toString()
        val name = rawName.normalizedName()
        if (name.isEmpty()) return ProfileResult.Error(getString(R.string.error_empty_name))

        val gender = when (binding.genderGroup.checkedRadioButtonId) {
            R.id.maleRadio -> Gender.MALE
            R.id.femaleRadio -> Gender.FEMALE
            else -> return ProfileResult.Error(getString(R.string.error_no_gender))
        }

        val coursePosition = binding.courseSpinner.selectedItemPosition
        if (coursePosition < 0) return ProfileResult.Error(getString(R.string.error_no_course))
        val course = coursePosition + 1

        val difficulty = binding.difficultySeekBar.progress + 1

        val millis = binding.birthDateCalendar.date
        val birthDate = Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val zodiac = zodiacByDate(birthDate.dayOfMonth, birthDate.monthValue)

        return ProfileResult.Success(
            PlayerProfile(
                fullName = name,
                gender = gender,
                course = course,
                difficulty = difficulty,
                birthDate = birthDate,
                zodiac = zodiac
            )
        )
    }

    private fun showProfile(profile: PlayerProfile) {
        val genderText = when (profile.gender) {
            Gender.MALE -> getString(R.string.gender_male)
            Gender.FEMALE -> getString(R.string.gender_female)
        }
        binding.resultText.text = getString(
            R.string.result_format,
            profile.fullName,
            genderText,
            profile.course,
            profile.difficulty,
            profile.birthDate.toString(),
            profile.zodiac.name
        )
    }
}