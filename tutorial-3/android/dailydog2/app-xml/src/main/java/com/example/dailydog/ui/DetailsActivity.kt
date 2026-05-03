package com.example.dailydog.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.dailydog.data.DogDatabase
import com.example.dailydog.data.DogItem
import com.example.dailydog.databinding.ActivityDetailsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding
    private var isFavorite = false
    private lateinit var imageUrl: String
    private lateinit var breed: String
    private val dogDao by lazy { DogDatabase.getDatabase(this).dogDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageUrl = intent.getStringExtra("EXTRA_IMAGE_URL") ?: ""
        breed = intent.getStringExtra("EXTRA_BREED") ?: "Unknown"

        binding.tvDetailBreed.text = breed
        Glide.with(this).load(imageUrl).into(binding.imgFullScreenDog)

        checkFavoriteStatus()

        binding.fabFavorite.setOnClickListener {
            toggleFavorite()
        }
    }

    private fun checkFavoriteStatus() {
        lifecycleScope.launch {
            val dogInDb = withContext(Dispatchers.IO) { dogDao.getDogByUrl(imageUrl) }
            isFavorite = dogInDb?.isFavorite == true
            updateFabIcon()
        }
    }

    private fun toggleFavorite() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                if (isFavorite) {
                    dogDao.removeFavorite(imageUrl)
                    isFavorite = false
                } else {
                    val dog = DogItem(imageUrl, breed)
                    dogDao.addFavoriteFIFO(dog)
                    isFavorite = true
                }
            }
            updateFabIcon()
            val msg = if (isFavorite) "Added to Favorites!" else "Removed from Favorites."
            Toast.makeText(this@DetailsActivity, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateFabIcon() {
        val iconRes = if (isFavorite) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off
        binding.fabFavorite.setImageResource(iconRes)
    }
}
