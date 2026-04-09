class Pipeline(private val list: MutableList<Pair<String, (List<String>) -> List<String>>>){

    fun addStage(name: String, transform: (List<String>) -> List<String>) {
        list.add(name to transform) // Creates a tuple with name and transform
    }
    fun execute(input: List<String>): List<String> {
        var result = input
        for (stage in list) {
            result = stage.second(result)
        }
        return result
    }
    fun describe(){
        var curIndex = 1
        println("Pipeline stages:")
        for (stage in list) {
            println("$curIndex. ${stage.first}")
            curIndex++
        }
    }
    fun compose(stageA: String, stageB: String) {
        val first = list.find { it.first == stageA }?.second
            ?: throw IllegalArgumentException("Stage $stageA not found")

        val second = list.find { it.first == stageB }?.second
            ?: throw IllegalArgumentException("Stage $stageB not found")

        val composed = first andThen second

        list.add("$stageA + $stageB" to composed)
    }

    fun fork(other: Pipeline, input: List<String>): Pair<List<String>, List<String>> {
        return this.execute(input) to other.execute(input)
    }

    private infix fun <T> ((T) -> T).andThen(next: (T) -> T): (T) -> T {
        return { x -> next(this(x)) }
    }
}

fun buildPipeline(block: Pipeline.() -> Unit): Pipeline {
    val pipeline = Pipeline(mutableListOf())
    pipeline.block()
    return pipeline
}

fun main() {
    val logs = listOf (
        " INFO : server started " ,
        " ERROR : disk full " ,
        " DEBUG : checking config " ,
        " ERROR : out of memory " ,
        " INFO : request received " ,
        " ERROR : connection timeout "
    )

    val pipeline = buildPipeline {
        addStage("Trim") { it.map { line -> line.trim() } }
        addStage("Filter errors") { it.filter { line -> line.contains("ERROR") } }
        addStage("Uppercase") { it.map { it.uppercase() } }
        addStage("Add index") { it.mapIndexed { i, v -> "${i + 1}. $v" } }
    }

    val pipeline2 = buildPipeline {
        addStage("Trim") { it.map { it.trim() } }
        addStage("Uppercase") { it.map { it.uppercase() } }
    }

    pipeline.compose("Trim", "Filter errors")

    pipeline.describe()

    val result = pipeline.execute(logs)

    println("Result :")
    result.forEach { println(it) }

    val forkResult = pipeline.fork(pipeline2, logs)

    println("LEFT:")
    forkResult.first.forEach { println(it) }

    println("RIGHT:")
    forkResult.second.forEach { println(it) }
}