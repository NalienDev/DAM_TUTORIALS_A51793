package com.moviebuddy.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moviebuddy.app.data.api.MovieDto
import com.moviebuddy.app.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MovieListUiState {
    object Loading : MovieListUiState()
    data class Success(val movies: List<MovieDto>) : MovieListUiState()
    data class Error(val message: String) : MovieListUiState()
}

class MovieListViewModel : ViewModel() {
    private val repository = MovieRepository()

    private val _uiState = MutableStateFlow<MovieListUiState>(MovieListUiState.Loading)
    val uiState: StateFlow<MovieListUiState> = _uiState.asStateFlow()

    init {
        loadMovies("movie")
    }

    fun loadMovies(query: String) {
        viewModelScope.launch {
            _uiState.value = MovieListUiState.Loading
            val result = repository.searchMovies(query)
            result.onSuccess { movies ->
                _uiState.value = MovieListUiState.Success(movies)
            }.onFailure { error ->
                _uiState.value = MovieListUiState.Error(error.localizedMessage ?: "Unknown error")
            }
        }
    }
}
