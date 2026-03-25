package com.moviebuddy.app.data.repository

import android.content.Context
import com.moviebuddy.app.data.api.MovieDto

class RatedMovieRepository(private val context: Context) {
    private val sharedPrefs = context.getSharedPreferences("MovieRatings", Context.MODE_PRIVATE)
    
    // In a real app we would store full movie objects in Room, but since we rely on SharedPreferences,
    // we will just store and retrieve a basic data mapping to show the rated IDs and scores.
    // For this simple implementation, we'll fetch details by querying the TVMaze API by the specific IDs.
    
    fun getRatedMovieIdsAndScores(): List<Pair<Long, Float>> {
        val ratings = mutableListOf<Pair<Long, Float>>()
        sharedPrefs.all.forEach { (key, value) ->
            if (key.startsWith("rating_") && value is Float && value > 0f) {
                val movieIdStr = key.removePrefix("rating_")
                movieIdStr.toLongOrNull()?.let { id ->
                    ratings.add(Pair(id, value))
                }
            }
        }
        return ratings.sortedByDescending { it.second }
    }
}
