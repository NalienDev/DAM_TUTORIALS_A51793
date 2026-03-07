package com.moviebuddy.app.ui.rated

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.moviebuddy.app.databinding.ItemRatedMovieBinding

class RatedMovieAdapter(private val onMovieClick: (RatedMovieItem) -> Unit) :
    ListAdapter<RatedMovieItem, RatedMovieAdapter.RatedViewHolder>(RatedDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatedViewHolder {
        val binding = ItemRatedMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RatedViewHolder(binding, onMovieClick)
    }

    override fun onBindViewHolder(holder: RatedViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RatedViewHolder(
        private val binding: ItemRatedMovieBinding,
        private val onMovieClick: (RatedMovieItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RatedMovieItem) {
            binding.titleTextView.text = item.movie.trackName ?: "Unknown Title"
            binding.itemRatingBar.rating = item.userRating
            binding.ratingValueText.text = item.userRating.toString()

            Glide.with(binding.root.context)
                .load(item.movie.artworkUrl?.replace("http://", "https://"))
                .into(binding.posterImageView)

            binding.root.setOnClickListener {
                onMovieClick(item)
            }
        }
    }

    class RatedDiffCallback : DiffUtil.ItemCallback<RatedMovieItem>() {
        override fun areItemsTheSame(oldItem: RatedMovieItem, newItem: RatedMovieItem): Boolean {
            return oldItem.movie.trackId == newItem.movie.trackId
        }

        override fun areContentsTheSame(oldItem: RatedMovieItem, newItem: RatedMovieItem): Boolean {
            return oldItem == newItem
        }
    }
}
