package dam.exer_2


fun main() {
    var option: String
    do {
        println("-------------Calculator-------------")
        println("0: Perform Arithmetic Operations")
        println("1: Boolean operations")
        println("2: Bitwise shift operations")
        println("3: Leave")
        println("Choose an option above:")
        option = readln()

        when (option) {
            "0" -> aritOperations()
            "1" -> boolOperations()
            "2" -> bitwiseShiftOperations()
            "3" -> print("Leaving Calculator...")
            else -> println("Invalid option: $option")
        }
    } while (option != "3")
}

fun aritOperations() {
    println("Arithmetic operations Selected")
    println("Insert the desired values and operations or type 'x' to go back to the main menu")
    println("example: 2*2")

    while (true) {
        print("Operation: ")
        val response = readln().trim()
        if (response.lowercase() == "x") break

        val operatorPair = response.findAnyOf(listOf("*", "+", "-", "/"))
        if (operatorPair == null) {
            println("Invalid operator. Use one of: + - * /")
            continue
        }

        val op = operatorPair.second
        val parts = response.split(op, limit = 2)

        if (parts.size < 2) {
            println("Missing second operand.")
            continue
        }

        val a = parts[0].trim().toIntOrNull()
        val b = parts[1].trim().toIntOrNull()

        if (a == null || b == null) {
            println("Invalid number(s). Please enter integers.")
            continue
        }

        if (op == "/" && b == 0) {
            println("Error: Division by zero.")
            continue
        }

        val result = when (op) {
            "+" -> a + b
            "-" -> a - b
            "*" -> a * b
            "/" -> a / b
            else -> { println("Invalid operator."); continue }
        }

        println("Result: $result")
    }
}

fun boolOperations() {
    println("Boolean operations Selected")
    println("Insert the desired values and operations or type 'x' to go back to the main menu")
    println("examples: 0||1  /  1&&0  /  !1")

    while (true) {
        print("Operation: ")
        val response = readln().trim()
        if (response.lowercase() == "x") break

        if (response.startsWith("!")) {
            val operand = response.drop(1).trim().toIntOrNull()
            if (operand == null) {
                println("Invalid value after '!'. Use 0 or higher.")
                continue
            }
            val a = operand != 0
            val result = !a
            println("Result: $result (${if (result) 1 else 0})")
            continue
        }

        val operatorPair = response.findAnyOf(listOf("||", "&&"))
        if (operatorPair == null) {
            println("Invalid operator. Use one of: || && !")
            continue
        }

        val op = operatorPair.second
        val parts = response.split(op, limit = 2)

        if (parts.size < 2) {
            println("Missing second operand.")
            continue
        }

        val a = parts[0].trim().toIntOrNull()
        val b = parts[1].trim().toIntOrNull()

        if (a == null || b == null) {
            println("Invalid value(s). Use 0 or 1.")
            continue
        }

        val result = when (op) {
            "||" -> (a != 0) || (b != 0)
            "&&" -> (a != 0) && (b != 0)
            else -> { println("Invalid operator."); continue }
        }

        println("Result: $result (${if (result) 1 else 0})")
    }
}

fun bitwiseShiftOperations() {
    println("Bitwise shift operations Selected")
    println("Insert the desired values and operations or type 'x' to go back to the main menu")
    println("examples: 4>>1  /  2<<3")

    while (true) {
        print("Operation: ")
        val response = readln().trim()
        if (response.lowercase() == "x") break

        val operatorPair = response.findAnyOf(listOf(">>", "<<"))
        if (operatorPair == null) {
            println("Invalid operator. Use >> or <<")
            continue
        }

        val op = operatorPair.second
        val parts = response.split(op, limit = 2)

        if (parts.size < 2) {
            println("Missing second operand.")
            continue
        }

        val a = parts[0].trim().toIntOrNull()
        val b = parts[1].trim().toIntOrNull()

        if (a == null || b == null) {
            println("Invalid number(s). Please enter integers.")
            continue
        }

        if (b < 0) {
            println("Shift amount cannot be negative.")
            continue
        }

        val result = when (op) {
            ">>" -> a shr b
            "<<" -> a shl b
            else -> { println("Invalid operator."); continue }
        }

        println("Result: $result")
    }
}
