package ru.fredboy.cavedroid.ksp.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import ru.fredboy.cavedroid.ksp.annotations.GenerateSetMultibindingsModule

class GenerateSetMultibindingsSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    private fun generateModule(
        interfaceName: ClassName,
        moduleName: ClassName,
        classes: List<ClassName>
    ): FileSpec? {
        if (classes.isEmpty()) {
            return null
        }

        val bindings = classes.map { clazz ->
            FunSpec.builder("bind${clazz.simpleName}")
                .addAnnotation(ClassName("dagger", "Binds"))
                .addAnnotation(ClassName("dagger.multibindings", "IntoSet"))
                .addParameter(ParameterSpec("impl", clazz))
                .returns(interfaceName)
                .addCode("return impl")
                .build()
        }

        val moduleObject = TypeSpec.objectBuilder(moduleName)
            .addAnnotation(ClassName("dagger", "Module"))
            .addAnnotation(
                AnnotationSpec.builder(ClassName("javax.annotation.processing", "Generated"))
                    .addMember("value = [%S]", this::class.qualifiedName!!)
                    .build()
            )
            .addFunctions(bindings)
            .build()

        return FileSpec.builder(moduleName)
            .addType(moduleObject)
            .build()

    }

    private fun processAnnotation(resolver: Resolver, annotation: KSClassDeclaration) {
        val args = annotation.annotations.first {
            it.shortName.getShortName() == "GenerateSetMultibindingsModule"
        }.arguments.takeIf { it.size == 3 } ?: run {
            logger.error("GenerateSetMultibindingsModule should have 3 arguments")
            throw IllegalArgumentException()
        }

        val interfaceName = args.first { it.name?.getShortName() == "interfaceClass" }.value as KSType
        val modulePackage = args.first { it.name?.getShortName() == "modulePackage" }.value as String
        val moduleName = args.first { it.name?.getShortName() == "moduleName" }.value as String

        val moduleClassName = ClassName(modulePackage, moduleName)
        val elements = resolver.getSymbolsWithAnnotation(annotation.qualifiedName!!.asString())
            .filterIsInstance<KSClassDeclaration>()
            .map(KSClassDeclaration::toClassName)
            .toList()

        logger.info("Found elements: ${elements.joinToString()}")

        generateModule(
            interfaceName = interfaceName.toClassName(),
            moduleName = moduleClassName,
            classes = elements
        )?.writeTo(codeGenerator, Dependencies(true))
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val annotations = resolver.getAnnotatedClasses(GenerateSetMultibindingsModule::class.qualifiedName!!, logger)
        logger.info("Found annotations: ${annotations.joinToString { it.qualifiedName!!.asString() }}")
        annotations.forEach { processAnnotation(resolver, it) }
        return emptyList()
    }

}