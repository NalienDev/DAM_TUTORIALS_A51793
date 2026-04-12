package com.example.dailydog.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.dailydog.data.DogDatabase
import com.example.dailydog.data.DogItem
import com.example.dailydog.repository.DogRepository
import kotlinx.coroutines.launch

class DogViewModel(application: Application) : AndroidViewModel(application) {
    private val dogDao = DogDatabase.getDatabase(application).dogDao()
    private val repository = DogRepository(dogDao)

    private val _dogs = MutableLiveData<List<DogItem>>()
    val dogs: LiveData<List<DogItem>> get() = _dogs

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading
    
    private val _errorEvent = MutableLiveData<String>()
    val errorEvent: LiveData<String> get() = _errorEvent

    private var showingFavorites = false

    init {
        fetchDogs()
    }

    fun toggleFavorites(show: Boolean) {
        showingFavorites = show
        refresh()
    }

    fun refresh() {
        if (showingFavorites) fetchFavorites() else fetchDogs()
    }

    fun fetchFavorites() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val favoriteDogs = repository.getFavorites()
                _dogs.value = favoriteDogs
                if (favoriteDogs.isEmpty()) {
                    _errorEvent.value = "No favorites found!"
                }
            } catch (e: Exception) {
                _errorEvent.value = "Error fetching favorites"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchDogs() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentDogs = repository.fetchDogs()
                _dogs.value = currentDogs
                if (currentDogs.isEmpty()) {
                    _errorEvent.value = "No connection and no cached dogs found!"
                }
            } catch (e: Exception) {
                _errorEvent.value = "An unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
