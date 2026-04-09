class Cache<K: Any, V: Any> {
    private val map = mutableMapOf<K, V>()

    fun put(key: K, value: V) {
        map[key] = value
    }
    fun get(key: K): V? = map[key]
    fun evict(key: K) {
        map.remove(key)
    }
    fun size() : Int{
        return map.size
    }
    fun getOrPut(key: K, default: () -> V): V {
        // If the value is already in the map ("?") return it. ".let" is to use that value in the lambda
        map[key]?.let { return it }

        val value = default()
        map[key] = value
        return value
    }

    fun transform(key: K, action: (V) -> V): Boolean {
        val current = map[key] ?: return false
        map[key] = action(current)
        return true
    }

    fun snapshot(): Map<K, V>{
        // return the map in immutable form (can't be modified and doesn't modify the original)
        return map.toMap()
    }

    fun filterValues(predicate: (V) -> Boolean): Map<K, V> {
        return map.filterValues(predicate).toMap()
    }
}
fun main(){
    val wordFrequencyCache = Cache<String, Int>()
    val idRegistryCache = Cache<Int, String>()

    wordFrequencyCache.put("scala", 1)
    wordFrequencyCache.put("haskell", 1)
    wordFrequencyCache.put("kotlin", 1)

    println("--- Word frequency cache ---")
    println("Size: ${wordFrequencyCache.size()}")
    println("Frequency of \"kotlin\": ${wordFrequencyCache.get("kotlin")}")
    println("getOrPut \"kotlin\": ${wordFrequencyCache.getOrPut("kotlin") { 1 }}")
    println("getOrPut \"java\": ${wordFrequencyCache.getOrPut("java") { 0 }}")
    println("Size after getOrPut: ${wordFrequencyCache.size()}")
    println("Transform \"kotlin\" (+1): ${wordFrequencyCache.transform("kotlin") { it + 1 }}") // add 1 to its original value
    println("Transform \"cobol\" (+1): ${wordFrequencyCache.transform("cobol") { it + 1 }}") // "cobol" is not a key in the map so no changes made
    println("Snapshot: ${wordFrequencyCache.snapshot()}")
    println("Words with count greater than zero: ${wordFrequencyCache.filterValues{it > 0} }")

    idRegistryCache.put(1, "Alice")
    idRegistryCache.put(2, "Bob")

    println("--- Id registry cache ---")
    println("Id 1 -> ${idRegistryCache.get(1)}")
    println("Id 2 -> ${idRegistryCache.get(2)}")
    idRegistryCache.evict(1)
    println("After evict id 1, size: ${idRegistryCache.size()}")
    println("Id 1 after evict -> ${idRegistryCache.get(1)}")


}