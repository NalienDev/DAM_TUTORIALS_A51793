package dam.exer_3

import kotlin.math.round

fun main() {
    dropBall(100f).forEach { println("%.2f".format(it) + "m") }
}

fun dropBall(height: Float): List<Float> {
    return generateSequence(height) { it * 0.6f }
        .takeWhile { it > 1 }
        .toList()
}