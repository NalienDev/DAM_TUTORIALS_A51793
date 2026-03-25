package com.moviebuddy.app.ui.detail

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MovieDetailViewModel : ViewModel() {
    private val _userRating = MutableStateFlow<Float>(0f)
    val userRating: StateFlow<Float> = _userRating.asStateFlow()

    fun setRating(rating: Float) {
        _userRating.value = rating
    }
}
