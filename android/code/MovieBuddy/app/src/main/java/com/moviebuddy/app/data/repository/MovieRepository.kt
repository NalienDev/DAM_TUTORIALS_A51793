package com.moviebuddy.app.data.repository

import com.moviebuddy.app.data.api.MovieDto
import com.moviebuddy.app.data.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MovieRepository {
    private val api = RetrofitClient.apiService

    suspend fun searchMovies(query: String): Result<List<MovieDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.searchMovies(query = query)
                Result.success(response.map { it.show })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
