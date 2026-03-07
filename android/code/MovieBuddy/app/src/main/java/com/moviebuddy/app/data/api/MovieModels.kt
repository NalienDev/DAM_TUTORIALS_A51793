package com.moviebuddy.app.data.api

import com.google.gson.annotations.SerializedName

data class TvMazeResponse(
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
    val genre: String? get() = genres?.joinToString(", ")
    val artworkUrl: String? get() = image?.original ?: image?.medium
    val description: String? get() = rawDescription?.replace(Regex("<.*?>"), "")
}

data class TvMazeImage(
    @SerializedName("medium") val medium: String?,
    @SerializedName("original") val original: String?
)
