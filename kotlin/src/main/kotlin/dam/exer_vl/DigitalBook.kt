package dam.exer_vl

class DigitalBook//TODO porque double
    (title: String, author: String, year: Int, availableCopies: Int, val fileSize: Double, val format: String) :
    Book(title, author, year, availableCopies) {

}