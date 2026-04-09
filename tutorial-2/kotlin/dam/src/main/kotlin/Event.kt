sealed class Event{

    abstract val username: String
    abstract val timestamp : Long
    class Login(override val username: String, override val timestamp: Long) : Event()
    class Purchase(override val username: String, val amount: Double, override val timestamp:
    Long) : Event()
    class Logout(override val username: String, override val timestamp: Long) : Event()
}

fun List<Event>.filterByUser(username: String) : List<Event> {
    return filter { it.username == username }
}

fun List<Event>.totalSpent(username: String) : Float {
    return filterIsInstance<Event.Purchase>().filter { it.username == username }.sumOf { it.amount }.toFloat()
}

fun processEvents(events : List<Event>, handler: (Event) -> Unit) {
    events.forEach { handler(it) }
}

fun main(){
    val events = listOf (
        Event.Login ("alice" , 1_000 ) ,
        Event.Purchase ("alice" , 49.99 , 1_100 ) ,
        Event.Purchase ("bob" , 19.99 , 1_200 ) ,
        Event.Login ("bob" , 1_050 ) ,
        Event.Purchase ("alice" , 15.00 , 1_300 ) ,
        Event.Logout ("alice" , 1_400 ) ,
        Event.Logout ("bob" , 1_500 )
    )

    processEvents(events, handler = {
        when(it){
            is Event.Login -> println("[LOGIN] ${it.username} logged in at t=${it.timestamp}")
            is Event.Purchase -> println("[PURCHASE] ${it.username} spent ${it.amount} at t=${it.timestamp}")
            is Event.Logout -> println("[LOGOUT] ${it.username} logged out at t=${it.timestamp}")
        }
    })

    print("\n")


    println("Total spent by alice: $${events.totalSpent("alice")}")
    println("Total spent by bob: $${events.totalSpent("bob")}")

    print("\n")

    val username = "alice"
    println("Events for $username:")
    events.filterByUser(username).forEach {
        when(it){
            is Event.Login -> println("  Login (username=${it.username}, timestamp=${it.timestamp})")
            is Event.Purchase -> println("  Purchase (usename=${it.username}, amount=${it.amount}, timestamp=${it.timestamp})")
            is Event.Logout -> println("  Logout (username=${it.username}, timestamp=${it.timestamp})")
        }
    }

}