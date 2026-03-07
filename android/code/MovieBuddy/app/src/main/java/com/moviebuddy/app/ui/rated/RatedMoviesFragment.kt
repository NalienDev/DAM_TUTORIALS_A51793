package com.moviebuddy.app.ui.rated

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.moviebuddy.app.R
import com.moviebuddy.app.databinding.FragmentRatedMoviesBinding
import kotlinx.coroutines.launch

class RatedMoviesFragment : Fragment() {

    private var _binding: FragmentRatedMoviesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RatedMoviesViewModel by viewModels()
    private lateinit var adapter: RatedMovieAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRatedMoviesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setupRecyclerView()
        observeViewModel()
        
        // Load data on start
        viewModel.loadRatedMovies()
    }

    private fun setupRecyclerView() {
        adapter = RatedMovieAdapter { ratedItem ->
            val bundle = Bundle().apply {
                putString("movieId", ratedItem.movie.trackId.toString())
                putString("title", ratedItem.movie.trackName)
                putString("description", ratedItem.movie.description)
                putString("posterUrl", ratedItem.movie.artworkUrl)
                putString("genre", ratedItem.movie.genre)
            }
            findNavController().navigate(R.id.action_ratedMoviesFragment_to_movieDetailFragment, bundle)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is RatedMoviesUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.recyclerView.visibility = View.GONE
                            binding.statusTextView.visibility = View.GONE
                        }
                        is RatedMoviesUiState.Empty -> {
                            binding.progressBar.visibility = View.GONE
                            binding.recyclerView.visibility = View.GONE
                            binding.statusTextView.visibility = View.VISIBLE
                            binding.statusTextView.text = "No rated movies yet."
                        }
                        is RatedMoviesUiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            binding.recyclerView.visibility = View.VISIBLE
                            binding.statusTextView.visibility = View.GONE
                            adapter.submitList(state.movies)
                        }
                        is RatedMoviesUiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.recyclerView.visibility = View.GONE
                            binding.statusTextView.visibility = View.VISIBLE
                            binding.statusTextView.text = state.message
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
