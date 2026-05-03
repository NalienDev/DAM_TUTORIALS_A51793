package com.example.dailydog.data

import com.google.gson.annotations.SerializedName

data class DogApiResponse(
    @SerializedName("message")
    val message: List<String>,
    @SerializedName("status")
    val status: String
)
