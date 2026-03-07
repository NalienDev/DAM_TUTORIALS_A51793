package dam.exer_vl

open class Book(
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

            if (field == 0) {
                println("Warning: Book is now out of stock!")
            }
        }

    init {
        println("Book $title by $author has been added to the library.")
    }
}