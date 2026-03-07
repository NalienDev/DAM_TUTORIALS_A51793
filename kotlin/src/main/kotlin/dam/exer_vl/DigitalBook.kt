package dam.exer_vl

enum class Format { PDF, EPUB, MOBI }

class DigitalBook(title: String, author: String, year: Int, availableCopies: Int, private val fileSize: Double, private val format: Format
) : Book(title, author, year, availableCopies) {

    init {
        require(fileSize > 0) { "File size must be positive." }
    }

    override fun getStorageInfo(): String = "Stored digitally: $fileSize MB, Format: $format"
}