package dam.exer_vl

abstract class Book(
    val title: String,
    val author: String,
    private val year: Int,
    availableCopies: Int
) {
    val publicationYear: String
        get() = when {
            year < 1980 -> "Classic"
            year in 1980..2010 -> "Modern"
            else -> "Contemporary"
        }

    var availableCopies: Int = availableCopies
        set(value) {
            if (value < 0) return
            field = value
            if (field == 0) println("Warning: '$title' is now out of stock!")
        }

    init {
        require(year > 0) { "Publication year must be positive." }
        require(availableCopies >= 0) { "Available copies cannot be negative." }
        println("Book $title by $author created.")
    }

    abstract fun getStorageInfo(): String

    override fun toString(): String =
        "Title: $title, Author: $author, Era: $publicationYear, Available: $availableCopies copies"
}