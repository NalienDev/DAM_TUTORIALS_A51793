package dam.exer_vl

class PhysicalBook(title: String, author: String, year: Int, availableCopies: Int, val weight: Int, val hasHardCover: Boolean = true) :
    Book(title, author, year, availableCopies) {

}