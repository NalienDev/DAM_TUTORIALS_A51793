package app

fun main() {
    val myClass = MyClass()
    val wrappedMyClass = MyClassWrapper(myClass) // Use the wrapper class

    wrappedMyClass.sayHello()
    wrappedMyClass.compute()

    val input = "Name:John Address:123 Street"
    val extractor = DataProcessorExtractor(input)
    println("Name: ${extractor.getName()}")
    println("Address: ${extractor.getAddress()}")
}