package dam.exer_vl

class PhysicalBook(title: String, author: String, year: Int, availableCopies: Int, private val weight: Int, private val hasHardCover: Boolean = true
) : Book(title, author, year, availableCopies) {

    init {
        require(weight > 0) { "Weight must be positive." }
    }

    override fun getStorageInfo(): String =
        "Physical book: ${weight}g, Hardcover: ${if (hasHardCover) "Yes" else "No"}"
}