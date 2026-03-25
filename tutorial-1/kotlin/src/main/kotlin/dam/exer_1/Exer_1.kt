package dam.exer_1


fun main() {
    println("Hello World")
    val intArray = IntArray(50) {it * it}
    val arrayRangeMap = (0..49).map { it * it }
    val array = Array(50) {it * it}
    println(intArray.joinToString())
    println(arrayRangeMap.joinToString())
    print(array.joinToString())
}

