package com.example.dailydog.repository

import com.example.dailydog.api.RetrofitInstance
import com.example.dailydog.data.DogDao
import com.example.dailydog.data.DogItem

class DogRepository(private val dogDao: DogDao) {

    suspend fun fetchDogs(): List<DogItem> {
        return try {
            val response = RetrofitInstance.api.getRandomDogs()
            if (response.status == "success") {
                val mappedDogs = response.message.map { url ->
                    DogItem(
                        imageUrl = url,
                        breed = extractBreedFromUrl(url)
                    )
                }
                dogDao.insertCacheDogs(mappedDogs)
                mappedDogs
            } else {
                dogDao.getCacheDogs()
            }
        } catch (e: Exception) {
            dogDao.getCacheDogs() // Offline fallback
        }
    }

    suspend fun getFavorites(): List<DogItem> {
        return dogDao.getFavorites()
    }

    private fun extractBreedFromUrl(url: String): String {
        return try {
            val parts = url.split("/")
            parts[parts.size - 2]
        } catch (e: Exception) {
            "Unknown Breed"
        }
    }
}
