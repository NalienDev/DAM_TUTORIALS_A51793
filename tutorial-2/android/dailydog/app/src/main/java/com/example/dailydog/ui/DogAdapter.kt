package com.example.dailydog.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dailydog.data.DogItem
import com.example.dailydog.databinding.ItemDogBinding

class DogAdapter(
    private val dogs: MutableList<DogItem>,
    private val onItemClick: (DogItem) -> Unit
) : RecyclerView.Adapter<DogAdapter.DogViewHolder>() {

    inner class DogViewHolder(private val binding: ItemDogBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(dog: DogItem) {
            binding.textViewBreed.text = dog.breed.capitalizeWords()
            Glide.with(binding.root.context)
                .load(dog.imageUrl)
                .centerCrop()
                .into(binding.imageViewDog)
                
            binding.root.setOnClickListener {
                onItemClick(dog)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
        val binding = ItemDogBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DogViewHolder(binding)
    }

    override fun getItemCount(): Int = dogs.size

    override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
        holder.bind(dogs[position])
    }

    fun updateData(newDogs: List<DogItem>) {
        dogs.clear()
        dogs.addAll(newDogs)
        notifyDataSetChanged()
    }
}

// Utility to capitalize breed names properly
fun String.capitalizeWords(): String = split("-").joinToString(" ") { 
    it.replaceFirstChar { char -> char.uppercaseChar() } 
}
