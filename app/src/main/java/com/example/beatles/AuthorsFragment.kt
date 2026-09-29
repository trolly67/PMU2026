package com.example.beatles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.beatles.databinding.FragmentAuthorsBinding

class AuthorsFragment : Fragment() {

    private var _binding: FragmentAuthorsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthorsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val authors = listOf(
            Author("Кретов Евгений", R.drawable.author1),
            Author("Первойкин Максим", R.drawable.author2)
        )

        binding.authorsListView.adapter = AuthorAdapter(requireContext(), authors)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}