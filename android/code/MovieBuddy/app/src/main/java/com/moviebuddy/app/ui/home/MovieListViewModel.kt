package com.moviebuddy.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moviebuddy.app.data.api.MovieDto
import com.moviebuddy.app.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Kotlin `sealed class` restricts class hierarchies. 
// It's like a Super-Enum that can hold data securely. When we use it in a `when` statement, 
// the compiler knows all possible subclasses, so we don't need an `else` branch!
sealed class MovieListUiState {
    // `object` defines a Singleton (a class with exactly one instance). Used for valueless states.
    object Loading : MovieListUiState()
    data class Success(val movies: List<MovieDto>) : MovieListUiState()
    data class Error(val message: String) : MovieListUiState()
}

// Android `ViewModel` survives configuration changes like screen rotations. 
// Any data held here won't be lost when the activity restarts.
class MovieListViewModel : ViewModel() {
    private val repository = MovieRepository()

    // `MutableStateFlow` is Kotlin's modern reactive stream (replacing LiveData/RxJava).
    // We keep the Mutable version private (prefixed with `_`) so it cannot be changed outside the ViewModel.
    private val _uiState = MutableStateFlow<MovieListUiState>(MovieListUiState.Loading)
    
    // We expose a read-only (`StateFlow`) version to the View (Activity/Fragment).
    // This enforces the "Unidirectional Data Flow" architecture principle.
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
