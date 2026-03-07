package dam.exer_vl

data class LibraryMember(
    val name: String,
    val membershipId: String,
    val borrowedBooks: MutableList<String> = mutableListOf()
)

class Library(private val name : String) {

    private val books: MutableList<Book> = mutableListOf()

    fun addBook(book: Book) {
        books.add(book)
        totalBooksAdded++
        println("Book '${book.title}' by ${book.author} has been added to the library.")
    }

    fun borrowBook(title: String, member: LibraryMember? = null) {
        val book = findBook(title) ?: return
        if (book.availableCopies == 0) {
            println("Sorry, no copies of '${book.title}' are available to borrow.")
            return
        }
        book.availableCopies--
        member?.borrowedBooks?.add(title)
        println("Successfully borrowed '${book.title}'. Copies remaining: ${book.availableCopies}")
    }

    fun returnBook(title: String, member: LibraryMember? = null) {
        val book = findBook(title) ?: return
        book.availableCopies++
        member?.borrowedBooks?.remove(title)
        println("Book '${book.title}' returned successfully. Copies available: ${book.availableCopies}")
    }

    fun searchByAuthor(author: String) {
        val booksByAuthor = books.filter { it.author == author }
        if (booksByAuthor.isEmpty()) {
            println("No books by '$author' found in $name.")
            return
        }
        println("Books by $author:")
        booksByAuthor.forEach { println("  - ${it.title} (${it.publicationYear}, ${it.availableCopies} copies available)") }
    }

    private fun findBook(title: String): Book? {
        val book = books.find { it.title == title }
        if (book == null) println("Book '$title' not found in $name.")
        return book
    }

    fun showBooks(){
        if (books.isNotEmpty()){
            println("\n--- Library Catalog ---")
            books.forEach {
                println(it)
                println("Storage: ${it.getStorageInfo()}")
            }
        } else {
            println("No books in $name.")
        }
    }
    companion object {
        private var totalBooksAdded: Int = 0

        private fun getTotalBooksCreated(): Int = totalBooksAdded

        @JvmStatic
        fun main(args: Array<String>) {
            val library = Library("Central Library")
            val digitalBook = DigitalBook(
                "Kotlin in Action",
                "Dmitry Jemerov",
                2017,
                5,
                4.5,
                Format.PDF
            )
            val physicalBook = PhysicalBook(
                "Clean Code",
                "Robert C. Martin",
                2008,
                3,
                650,
                true
            )
            val classicBook = PhysicalBook(
                "1984",
                "George Orwell",
                1949,
                2,
                400,
                false
            )
            library.addBook(digitalBook)
            library.addBook(physicalBook)
            library.addBook(classicBook)
            library.showBooks()

            println("\n--- Borrowing Books ---")
            val member = LibraryMember("Lucas", "51793")
            library.borrowBook("Clean Code", member)
            library.borrowBook("1984", member)
            library.borrowBook("1984", member)
            library.borrowBook("1984", member)
            println("\n--- Returning Books ---")
            library.returnBook("1984", member)
            println("\n--- Search by Author ---")
            library.searchByAuthor("George Orwell")

            println("\nTotal books added across all libraries: ${getTotalBooksCreated()}")
        }
    }

}