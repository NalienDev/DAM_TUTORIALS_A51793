package processor

import annotations.Extract
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_23)
@SupportedAnnotationTypes("annotations.Extract")
class RegexProcessor : AbstractProcessor() {

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {

        val classMethodMap =
            mutableMapOf<TypeElement, MutableList<ExecutableElement>>()

        // Find all methods annotated with @Extract
        for (element in roundEnv.getElementsAnnotatedWith(Extract::class.java)) {
            if (element is ExecutableElement) {
                val enclosingClass = element.enclosingElement as TypeElement
                classMethodMap
                    .computeIfAbsent(enclosingClass) { mutableListOf() }
                    .add(element)
            }
        }

        // Generate extractor class for each class containing annotated methods
        for ((classElement, methods) in classMethodMap) {
            generateExtractorClass(classElement, methods)
        }

        return true
    }

    private fun generateExtractorClass(
        classElement: TypeElement,
        methods: List<ExecutableElement>
    ) {
        val packageName =
            processingEnv.elementUtils.getPackageOf(classElement).toString()

        val originalClassName = classElement.simpleName.toString()
        val extractorClassName = "${originalClassName}Extractor"

        // Build primary constructor with `input: String`
        val constructor = FunSpec.constructorBuilder()
            .addParameter("input", String::class)
            .build()

        // Build the class extending the abstract DataProcessor(input)
        val classBuilder = TypeSpec.classBuilder(extractorClassName)
            .addModifiers(KModifier.PUBLIC)
            .primaryConstructor(constructor)
            .superclass(ClassName(packageName, originalClassName))
            .addSuperclassConstructorParameter("input")

        // Generate an override for each @Extract-annotated abstract method
        for (method in methods) {
            val methodName = method.simpleName.toString()
            val regex = method.getAnnotation(Extract::class.java)?.regex ?: continue

            // Return type is String? — use nullable String
            val returnType = String::class.asTypeName().copy(nullable = true)

            val methodBuilder = FunSpec.builder(methodName)
                .addModifiers(KModifier.OVERRIDE)
                .returns(returnType)
                // val match = Regex("...").find(input)
                .addStatement("val match = %T(%S).find(input)", Regex::class, regex)
                // return match?.groupValues?.get(1)
                .addStatement("return match?.groupValues?.get(1)")

            classBuilder.addFunction(methodBuilder.build())
        }

        // Build the Kotlin file with explicit imports
        val file = FileSpec.builder(packageName, extractorClassName)
            .addImport("kotlin.text", "Regex")
            .addType(classBuilder.build())
            .build()

        // Write to kapt output directory
        try {
            val kaptKotlinGeneratedDir =
                processingEnv.options["kapt.kotlin.generated"]

            if (kaptKotlinGeneratedDir != null) {
                file.writeTo(File(kaptKotlinGeneratedDir))
            } else {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "kapt.kotlin.generated not found"
                )
            }
        } catch (e: Exception) {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.ERROR,
                "Error generating Kotlin file: ${e.message}"
            )
        }
    }
}