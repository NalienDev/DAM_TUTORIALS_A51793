package com.moviebuddy.app.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface iTunesApiService {
    @GET("search/shows")
    suspend fun searchMovies(
        @Query("q") query: String
    ): List<TvMazeResponse>

    @GET("shows/{id}")
    suspend fun getShowById(
        @retrofit2.http.Path("id") id: Long
    ): MovieDto
}
