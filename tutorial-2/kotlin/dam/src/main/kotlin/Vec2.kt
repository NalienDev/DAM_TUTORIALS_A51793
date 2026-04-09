import kotlin.math.pow
import kotlin.math.sqrt

data class Vec2(val x: Double, val y: Double) : Comparable<Vec2>{

    // component1() and component2() are unnecessary if the class is a data class, if we wanted to keep it a normal class,
    // we would do:
    // operator fun component1(): Double = x
    // operator fun component2(): Double = y

    operator fun plus(other: Vec2): Vec2 =
        Vec2(this.x + other.x, this.y + other.y)

    operator fun minus(other: Vec2): Vec2 =
        Vec2(this.x - other.x, this.y - other.y)

    // Scalar multiplication (Vec2 * Double)
    operator fun times(scalar: Double): Vec2 =
        Vec2(this.x * scalar, this.y * scalar)

    // Scalar multiplication (Double * Vec2)
    operator fun Double.times(v: Vec2): Vec2 =
        Vec2(v.x * this, v.y * this)

    // Unary negation
    operator fun unaryMinus(): Vec2 =
        Vec2(-this.x, -this.y)

    override operator fun compareTo(other: Vec2): Int {
        val thisMag2 = x.pow(2) + y.pow(2) // Sqrt is not needed because we're only comparing magnitudes
        val otherMag2 = other.x.pow(2) + other.y.pow(2)
        return thisMag2.compareTo(otherMag2)
    }

    fun magnitude(): Double{
        return sqrt(x.pow(2) + y.pow(2))
    }
    fun dot(other: Vec2): Double{
        return (x*other.x) + (y*other.y)
    }
    fun normalized(): Vec2 {
        val length = sqrt(x.pow(2) + y.pow(2))

        if (length == 0.0) {
            throw IllegalStateException("Cannot normalize zero vector")
        }

        return Vec2(x / length, y / length)
    }

    operator fun get(index: Int): Double {
        return when (index) {
            0 -> x
            1 -> y
            else -> throw IndexOutOfBoundsException("Vec2 only has indices 0 and 1")
        }
    }

}
fun main () {
    val a = Vec2 (3.0 , 4.0)
    val b = Vec2 (1.0 , 2.0)
    println ("a = $a ") // a = Vec2 (x =3.0 , y =4.0)
    println ("b = $b ") // b = Vec2 (x =1.0 , y =2.0)
    println ("a + b = ${a + b}") // a + b = Vec2 (x =4.0 , y =6.0)
    println ("a - b = ${a - b}") // a - b = Vec2 (x =2.0 , y =2.0)
    println ("a * 2.0 = ${a * 2.0} ") // a * 2.0 = Vec2 (x =6.0 , y =8.0)
    println (" -a = ${-a}") // -a = Vec2 (x = -3.0 , y = -4.0)
    println ("|a| = ${a. magnitude () }") // |a| = 5.0
    println ("a dot b = ${a. dot (b)}") // a dot b = 11.0
    println (" norm (a) = ${a. normalized () }")
    // norm (a) = Vec2 (x =0.6 , y =0.8)
    println ("a [0] = ${a [0]} ") // a [0] = 3.0
    println ("a [1] = ${a [1]} ") // a [1] = 4.0
    println ("a > b = ${a > b}") // a > b = true
    println ("a < b = ${a < b}") // a < b = false
    val vectors = listOf ( Vec2 (1.0 , 0.0) , Vec2 (3.0 , 4.0) , Vec2 (0.0 , 2.0) )
    println (" Longest = ${ vectors . max () }") // Longest = Vec2 (x =3.0 , y =4.0)
    println (" Shortest = ${ vectors . min () }") // Shortest = Vec2 (x =1.0 , y =0.0)
}