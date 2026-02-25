package dam.exer_2


fun main() {
    do {
        println("-------------Calculator-------------")
        println("0: Perform Arithmetic Operations")
        println("1: Boolean operations")
        println("2: Bitwise shift operations")
        println("3: Leave")
        println("Choose an option above:")
        val option = readln()

        when(option){
            "0" -> aritOperations()
            "1" -> boolOperations()
            "2" -> bitwiseShiftOperations()
            "3" -> print("Leaving Calculator...")
        }
    } while (option.equals(9))
}
fun aritOperations(){
    do{
        println("Arithmetic operations Selected")
        println("Insert the desired values and operations or type 'X' to go back to the main menu")
        println("example: 2 * 2")
        println("be sure to split each value with a space")
        print("Operation: ")
        val response = readln()
        val respList = response.split(" ")
        for (i in respList){
            when(i){
                /*
                "+" ->
                "-" ->
                "*" ->
                "/" ->

                 */
            }
        }

    } while (response == "x")
}

fun boolOperations(){
    print("Boolean operations Selected")
}
fun bitwiseShiftOperations(){
    print("Bitwise shift operations Selected")
}

