package com.example.beatles

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.beatles.data.AppDatabase
import com.example.beatles.data.PlayerEntity
import com.example.beatles.databinding.FragmentRegistrationBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar

class RegistrationFragment : Fragment() {

    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    private var selectedBirthDate: LocalDate? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = requireContext()

        setupCourseSpinner()

        binding.birthDateText.setOnClickListener {
            showDatePicker()
        }

        val currentId = GameSettings.getCurrentPlayerId(ctx)
        if (currentId > 0) {
            loadPlayerProfile(currentId)
        } else {
            setupLoggedOutUI()
        }

        binding.submitButton.setOnClickListener {
            when (val result = readProfileFromForm()) {
                is ProfileResult.Success -> {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val db = AppDatabase.get(requireContext())
                        val player = PlayerEntity(
                            name = result.profile.fullName,
                            gender = result.profile.gender.name,
                            course = result.profile.course,
                            difficulty = result.profile.difficulty,
                            birthDateEpochDay = result.profile.birthDate.toEpochDay(),
                            zodiac = result.profile.zodiac.name
                        )
                        val insertedId = db.playerDao().insert(player)
                        val finalId = if (insertedId > 0) insertedId
                        else db.playerDao().findByName(player.name)?.id ?: -1L

                        GameSettings.setCurrentPlayerId(requireContext(), finalId)

                        withContext(Dispatchers.Main) {
                            loadPlayerProfile(finalId)
                        }
                    }
                }
                is ProfileResult.Error -> binding.resultText.text = result.message
            }
        }
    }

    private fun loadPlayerProfile(playerId: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            val player = AppDatabase.get(requireContext()).playerDao().getById(playerId)
            withContext(Dispatchers.Main) {
                if (player != null) {
                    val profile = PlayerProfile(
                        fullName = player.name,
                        gender = Gender.valueOf(player.gender),
                        course = player.course,
                        difficulty = player.difficulty,
                        birthDate = LocalDate.ofEpochDay(player.birthDateEpochDay),
                        zodiac = ZodiacSign.valueOf(player.zodiac)
                    )
                    showProfile(profile)
                    setupLoggedInUI(playerId)
                } else {
                    GameSettings.setCurrentPlayerId(requireContext(), -1L)
                    setupLoggedOutUI()
                }
            }
        }
    }

    private fun setupLoggedInUI(playerId: Long) {
        binding.formContainer.visibility = View.GONE
        binding.profileContainer.visibility = View.VISIBLE

        // Меняем заголовок
        binding.titleText.text = getString(R.string.profile_title)

        binding.playButton.isEnabled = true
        binding.playButton.text = getString(R.string.button_play)
        binding.logoutButton.visibility = View.VISIBLE

        binding.playButton.setOnClickListener {
            val intent = Intent(requireContext(), GameActivity::class.java)
            intent.putExtra("player_id", playerId)
            startActivity(intent)
        }

        binding.logoutButton.setOnClickListener {
            GameSettings.setCurrentPlayerId(requireContext(), -1L)
            requireActivity().recreate()
        }
    }

    private fun setupLoggedOutUI() {
        binding.formContainer.visibility = View.VISIBLE
        binding.profileContainer.visibility = View.GONE

        // Возвращаем заголовок регистрации
        binding.titleText.text = getString(R.string.registration_title)

        binding.playButton.isEnabled = false
        binding.playButton.text = getString(R.string.button_play_disabled)
        binding.logoutButton.visibility = View.GONE

        binding.resultText.text = ""
        binding.zodiacImage.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showDatePicker() {
        val today = Calendar.getInstance()
        val dialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedBirthDate = LocalDate.of(year, month + 1, dayOfMonth)
                binding.birthDateText.text = selectedBirthDate.toString()
            },
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        )
        dialog.datePicker.maxDate = System.currentTimeMillis()
        dialog.datePicker.minDate = LocalDate.of(1900, 1, 1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        dialog.show()
    }

    private fun setupCourseSpinner() {
        val courses = (1..6).map { it.toString() }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            courses
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.courseSpinner.adapter = adapter
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

        val difficulty = GameSettings.getPresetIndex(requireContext())

        val birthDate = selectedBirthDate
            ?: return ProfileResult.Error(getString(R.string.error_no_birth_date))

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

    private fun zodiacImageRes(zodiac: ZodiacSign): Int = when (zodiac) {
        ZodiacSign.ARIES -> R.drawable.zodiac_aries
        ZodiacSign.TAURUS -> R.drawable.zodiac_taurus
        ZodiacSign.GEMINI -> R.drawable.zodiac_gemini
        ZodiacSign.CANCER -> R.drawable.zodiac_cancer
        ZodiacSign.LEO -> R.drawable.zodiac_leo
        ZodiacSign.VIRGO -> R.drawable.zodiac_virgo
        ZodiacSign.LIBRA -> R.drawable.zodiac_libra
        ZodiacSign.SCORPIO -> R.drawable.zodiac_scorpio
        ZodiacSign.SAGITTARIUS -> R.drawable.zodiac_sagittarius
        ZodiacSign.CAPRICORN -> R.drawable.zodiac_capricorn
        ZodiacSign.AQUARIUS -> R.drawable.zodiac_aquarius
        ZodiacSign.PISCES -> R.drawable.zodiac_pisces
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

        binding.zodiacImage.setImageResource(zodiacImageRes(profile.zodiac))
        binding.zodiacImage.visibility = View.VISIBLE
    }
}