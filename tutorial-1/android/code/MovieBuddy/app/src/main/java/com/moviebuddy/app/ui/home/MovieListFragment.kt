package com.moviebuddy.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.moviebuddy.app.R
import com.moviebuddy.app.databinding.FragmentMovieListBinding
import kotlinx.coroutines.launch

class MovieListFragment : Fragment() {

    private var _binding: FragmentMovieListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MovieListViewModel by viewModels()
    private lateinit var adapter: MovieAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_rated_movies -> {
                    findNavController().navigate(R.id.action_movieListFragment_to_ratedMoviesFragment)
                    true
                }
                else -> false
            }
        }

        setupRecyclerView()
        setupSearch()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = MovieAdapter { movie ->
            val bundle = Bundle().apply {
                putString("movieId", movie.trackId.toString())
                putString("title", movie.trackName)
                putString("description", movie.description)
                putString("posterUrl", movie.artworkUrl)
                putString("genre", movie.genre)
            }
            findNavController().navigate(R.id.action_movieListFragment_to_movieDetailFragment, bundle)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchEditText.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = v.text.toString().trim()
                if (query.isNotEmpty()) {
                    viewModel.loadMovies(query)
                }
                true
            } else {
                false
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is MovieListUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.recyclerView.visibility = View.GONE
                            binding.errorTextView.visibility = View.GONE
                        }
                        is MovieListUiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            binding.recyclerView.visibility = View.VISIBLE
                            binding.errorTextView.visibility = View.GONE
                            adapter.submitList(state.movies)

                            if(state.movies.isEmpty()) {
                                binding.errorTextView.text = getString(R.string.no_results)
                                binding.errorTextView.visibility = View.VISIBLE
                            }
                        }
                        is MovieListUiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.recyclerView.visibility = View.GONE
                            binding.errorTextView.visibility = View.VISIBLE
                            binding.errorTextView.text = state.message
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
