package com.moviebuddy.app.data.repository

import com.moviebuddy.app.data.api.MovieDto
import com.moviebuddy.app.data.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MovieRepository {
    private val api = RetrofitClient.apiService

    // The `suspend` keyword is crucial in modern Android. 
    // It tells the compiler this function is a "Coroutine" (a lightweight thread). 
    // It can be paused and resumed without blocking the UI.
    suspend fun searchMovies(query: String): Result<List<MovieDto>> {
        // `withContext(Dispatchers.IO)` moves this block of code off the Main (UI) thread 
        // and onto a background thread optimized for Input/Output (like network requests). 
        // This is how modern Kotlin replaces Java's old AsyncTasks or RxJava observables.
        return withContext(Dispatchers.IO) {
            try {
                val response = api.searchMovies(query = query)
                // Returning a built-in Kotlin `Result` object gracefully handles Success/Failure states.
                Result.success(response.map { it.show })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
