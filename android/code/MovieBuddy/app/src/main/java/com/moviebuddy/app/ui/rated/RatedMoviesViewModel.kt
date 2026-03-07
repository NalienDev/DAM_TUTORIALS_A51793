package com.moviebuddy.app.ui.rated

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moviebuddy.app.data.api.MovieDto
import com.moviebuddy.app.data.api.RetrofitClient
import com.moviebuddy.app.data.repository.RatedMovieRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class RatedMovieItem(
    val movie: MovieDto,
    val userRating: Float
)

sealed class RatedMoviesUiState {
    object Loading : RatedMoviesUiState()
    data class Success(val movies: List<RatedMovieItem>) : RatedMoviesUiState()
    data class Error(val message: String) : RatedMoviesUiState()
    object Empty : RatedMoviesUiState()
}

class RatedMoviesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RatedMovieRepository(application)
    private val api = RetrofitClient.apiService

    private val _uiState = MutableStateFlow<RatedMoviesUiState>(RatedMoviesUiState.Loading)
    val uiState: StateFlow<RatedMoviesUiState> = _uiState.asStateFlow()

    fun loadRatedMovies() {
        viewModelScope.launch {
            _uiState.value = RatedMoviesUiState.Loading
            
            val ratedIdsAndScores = repository.getRatedMovieIdsAndScores()
            if (ratedIdsAndScores.isEmpty()) {
                _uiState.value = RatedMoviesUiState.Empty
                return@launch
            }

            try {
                val results = mutableListOf<RatedMovieItem>()
                // Fetch each movie detail using TVMaze's generic lookup
                // In a production app with a DB, this would be a single local query.
                withContext(Dispatchers.IO) {
                    for ((id, rating) in ratedIdsAndScores) {
                        try {
                            // Note: We need a new endpoint for lookup by ID to make this strict.
                            // For now, we will add an endpoint extension in the API interface to fetch by ID.
                            val movieDetail = api.getShowById(id)
                            results.add(RatedMovieItem(movieDetail, rating))
                        } catch (e: Exception) {
                            // Skip failed fetches
                            e.printStackTrace()
                        }
                    }
                }
                _uiState.value = RatedMoviesUiState.Success(results)
            } catch (e: Exception) {
                 _uiState.value = RatedMoviesUiState.Error(e.message ?: "Failed to fetch rated movies")
            }
        }
    }
}
