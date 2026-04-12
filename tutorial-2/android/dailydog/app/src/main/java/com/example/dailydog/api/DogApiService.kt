package com.example.dailydog.api

import com.example.dailydog.data.DogApiResponse
import retrofit2.http.GET

interface DogApiService {
    @GET("api/breeds/image/random/20")
    suspend fun getRandomDogs(): DogApiResponse
}
