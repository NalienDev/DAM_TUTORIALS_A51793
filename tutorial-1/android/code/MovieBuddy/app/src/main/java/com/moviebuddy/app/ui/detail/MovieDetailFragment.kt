package com.moviebuddy.app.ui.detail

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.moviebuddy.app.databinding.FragmentMovieDetailBinding

class MovieDetailFragment : Fragment() {

    private var _binding: FragmentMovieDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MovieDetailViewModel by viewModels()
    private var movieId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        arguments?.let { bundle ->
            movieId = bundle.getString("movieId")
            binding.titleTextView.text = bundle.getString("title") ?: "Unknown Title"
            binding.genreTextView.text = bundle.getString("genre") ?: "Unknown Genre"
            binding.descriptionTextView.text = bundle.getString("description") ?: "No description available."
            
            val artworkUrl = bundle.getString("posterUrl")
            Glide.with(this)
                .load(artworkUrl?.replace("http://", "https://"))
                .into(binding.posterImageView)
        }

        setupRating()
    }
    
    private fun setupRating() {
        // Load saved rating
        val sharedPrefs = requireContext().getSharedPreferences("MovieRatings", Context.MODE_PRIVATE)
        val savedRating = sharedPrefs.getFloat("rating_${movieId}", 0f)
        binding.ratingBar.rating = savedRating

        binding.saveRatingButton.setOnClickListener {
            val newRating = binding.ratingBar.rating
            if (movieId != null) {
                sharedPrefs.edit().putFloat("rating_${movieId}", newRating).apply()
                viewModel.setRating(newRating)
                Toast.makeText(requireContext(), "Rating saved!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
