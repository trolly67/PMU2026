package com.example.beatles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.beatles.data.AppDatabase
import com.example.beatles.databinding.FragmentRecordsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RecordsFragment : Fragment() {

    private var _binding: FragmentRecordsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: RecordsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RecordsAdapter()
        binding.recordsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recordsRecycler.adapter = adapter

        // Настраиваем фильтр
        val filterOptions = listOf(getString(R.string.filter_all), "1", "2", "3", "4", "5")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, filterOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.filterSpinner.adapter = spinnerAdapter

        // Следим за выбором фильтра
        binding.filterSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val difficulty = if (position == 0) null else position
                loadRecords(difficulty)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun loadRecords(difficulty: Int?) {
        val dao = AppDatabase.get(requireContext()).gameSessionDao()
        val flow = if (difficulty == null) {
            dao.observeAllRecords()
        } else {
            dao.observeRecordsByDifficulty(difficulty)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            flow.collectLatest { records ->
                adapter.submitList(records)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}