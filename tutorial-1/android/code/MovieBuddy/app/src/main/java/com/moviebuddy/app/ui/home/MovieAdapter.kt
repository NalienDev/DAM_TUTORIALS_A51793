package com.moviebuddy.app.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.moviebuddy.app.data.api.MovieDto
import com.moviebuddy.app.databinding.ItemMovieBinding

class MovieAdapter(private val onMovieClick: (MovieDto) -> Unit) :
    ListAdapter<MovieDto, MovieAdapter.MovieViewHolder>(MovieDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(binding, onMovieClick)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MovieViewHolder(
        private val binding: ItemMovieBinding,
        private val onMovieClick: (MovieDto) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: MovieDto) {
            binding.titleTextView.text = movie.trackName ?: "Unknown Title"
            binding.genreTextView.text = movie.genre ?: "Unknown Genre"
            val rawDate = movie.releaseDate
            binding.dateTextView.text = if (rawDate != null && rawDate.length >= 10) rawDate.substring(0, 10) else "Unknown Date"

            Glide.with(binding.root.context)
                .load(movie.artworkUrl?.replace("http://", "https://"))
                .into(binding.posterImageView)

            binding.root.setOnClickListener {
                onMovieClick(movie)
            }
        }
    }

    class MovieDiffCallback : DiffUtil.ItemCallback<MovieDto>() {
        override fun areItemsTheSame(oldItem: MovieDto, newItem: MovieDto): Boolean {
            return oldItem.trackId == newItem.trackId
        }

        override fun areContentsTheSame(oldItem: MovieDto, newItem: MovieDto): Boolean {
            return oldItem == newItem
        }
    }
}
