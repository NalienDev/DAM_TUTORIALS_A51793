package com.moviebuddy.app.data.api

import com.google.gson.annotations.SerializedName

// In Kotlin, a `data class` is a class whose main purpose is to hold data. 
// It automatically generates standard Java methods like getters, setters, equals(), hashCode(), 
// and a readable toString(). This replaces hundreds of lines of boilerplate Java code!
data class TvMazeResponse(
    // `val` creates an immutable property (with only a getter). 
    @SerializedName("show") val show: MovieDto
)

data class MovieDto(
    @SerializedName("id") val trackId: Long?,
    @SerializedName("name") val trackName: String?,
    @SerializedName("genres") val genres: List<String>?,
    @SerializedName("premiered") val releaseDate: String?,
    @SerializedName("summary") val rawDescription: String?,
    @SerializedName("image") val image: TvMazeImage?
) {
    // These properties define custom getters using `get()`. 
    // The `?.` is the "safe call operator". If the object is null, it immediately stops and returns null
    // rather than throwing a NullPointerException. This is one of Kotlin's strongest features over Java!
    val genre: String? get() = genres?.joinToString(", ")
    
    // The `?:` is the "Elvis Operator". It means: 
    // "Return the left side if it's not null; otherwise, return the right side". 
    val artworkUrl: String? get() = image?.original ?: image?.medium
    val description: String? get() = rawDescription?.replace(Regex("<.*?>"), "")
}

data class TvMazeImage(
    @SerializedName("medium") val medium: String?,
    @SerializedName("original") val original: String?
)
