package dam.exer_vl

class Library(name : String) {

    private val books: MutableList<Book> = mutableListOf()

    fun addBook(book: Book) {
        books.add(book) //TODO DAR PRINT AQUI Q FOI ADICIONADO OU NA CLASSE BOOK??
        println("Book ${book.title} by ${book.author} has been added to the library.")
    }

    fun borrowBook(title: String) {
        // Borrow book from library
    }

    fun returnBook(title: String) {
        // Return book to library
    }

    fun searchByAuthor(author: String) {
        // Search for books by author
    }

    fun showBooks(){
        // Show all the library's books
        if (books.isNotEmpty()){
            println("--- Library Catalog ---")
            books.forEach{
                println("Title: ${it.title}, Author: ${it.author}, Era: ${it.publicationYear}, Available: ${it.availableCopies} copies")
                if (it is DigitalBook){
                    println("Storage: Stored digitally: ${it.fileSize} MB, Format: ${it.format}")
                } else if (it is PhysicalBook){
                    print("Storage: Physical book: ${it.weight}g, Hardcover: ")
                    if (it.hasHardCover) println("Yes") else println("No")
                }
            }
        }
    }
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val library = Library("Central Library")
            val digitalBook = DigitalBook(
                "Kotlin in Action",
                "Dmitry Jemerov",
                2017,
                5,
                4.5,
                "PDF"
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
            /*
            println("\n--- Borrowing Books ---")
            library.borrowBook("Clean Code")
            library.borrowBook("1984")
            library.borrowBook("1984")
            library.borrowBook("1984") // Should fail - no copies left
            println("\n--- Returning Books ---")
            library.returnBook("1984")
            println("\n--- Search by Author ---")
            library.searchByAuthor("George Orwell")

            EXPECTED OUTPUT:
            Book 'Kotlin in Action' by Dmitry Jemerov has been added to the library.
            Book 'Clean Code' by Robert C. Martin has been added to the library.
            Book '1984' by George Orwell has been added to the library.
            --- Library Catalog ---
            Title: Kotlin in Action, Author: Dmitry Jemerov, Era: Contemporary, Available: 5 copies
            Storage: Stored digitally: 4.5 MB, Format: PDF
            Title: Clean Code, Author: Robert C. Martin, Era: Modern, Available: 3 copies
            Storage: Physical book: 650g, Hardcover: Yes
            Title: 1984, Author: George Orwell, Era: Classic, Available: 2 copies
            Storage: Physical book: 400g, Hardcover: No
            --- Borrowing Books ---
            Successfully borrowed 'Clean Code'. Copies remaining: 2
            Successfully borrowed '1984'. Copies remaining: 1
            Successfully borrowed '1984'. Copies remaining: 0
            Warning: Book is now out of stock!
            Sorry, -- Returning Books ---
            Book '1984' returned successfully. Copies available: 1
            --- Search by Author ---
            Books by George Orwell:
            - 1984 (Classic, 1 copy available)
            */
        }
    }

}