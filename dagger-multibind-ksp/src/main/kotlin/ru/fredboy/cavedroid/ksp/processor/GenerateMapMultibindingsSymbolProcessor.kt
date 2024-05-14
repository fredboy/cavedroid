package ru.fredboy.cavedroid.ksp.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import ru.fredboy.cavedroid.ksp.annotations.GenerateMapMultibindingsModule

class GenerateMapMultibindingsSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    private fun generateModule(
        annotationName: String,
        interfaceName: ClassName,
        moduleName: ClassName,
        classes: List<KSClassDeclaration>
    ): FileSpec? {
        if (classes.isEmpty()) {
            return null
        }

        val bindings = classes.map { decl ->
            val stringKey = decl.annotations.first { declAnn ->
                    declAnn.shortName.getShortName() == annotationName
                }.arguments.firstOrNull { arg ->
                    arg.name!!.getShortName() == "stringKey"
                }?.value as? String ?: run {
                    logger.error("@${annotationName} must include stringKey parameter for key selection in generated module")
                    throw IllegalArgumentException()
            }

            val clazz = decl.toClassName()

            FunSpec.builder("bind${clazz.simpleName}")
                .addAnnotation(ClassName("dagger", "Binds"))
                .addAnnotation(ClassName("dagger.multibindings", "IntoMap"))
                .addAnnotation(
                    AnnotationSpec.builder(ClassName("dagger.multibindings", "StringKey"))
                        .addMember("\"$stringKey\"")
                        .build()
                )
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
            it.shortName.getShortName() == "GenerateMapMultibindingsModule"
        }.arguments.takeIf { it.size == 3 } ?: run {
            logger.error("GenerateMapMultibindingsModule should have 3 arguments")
            throw IllegalArgumentException()
        }

        val interfaceName = args.first { it.name?.getShortName() == "interfaceClass" }.value as KSType
        val modulePackage = args.first { it.name?.getShortName() == "modulePackage" }.value as String
        val moduleName = args.first { it.name?.getShortName() == "moduleName" }.value as String

        val moduleClassName = ClassName(modulePackage, moduleName)
        val elements = resolver.getSymbolsWithAnnotation(annotation.qualifiedName!!.asString())
            .filterIsInstance<KSClassDeclaration>()
            .toList()

        logger.info("Found elements: ${elements.joinToString()}")

        generateModule(
            annotationName = annotation.qualifiedName!!.getShortName(),
            interfaceName = interfaceName.toClassName(),
            moduleName = moduleClassName,
            classes = elements
        )?.writeTo(codeGenerator, Dependencies(true))
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val annotations = resolver.getAnnotatedClasses(GenerateMapMultibindingsModule::class.qualifiedName!!, logger)
        logger.info("Found annotations: ${annotations.joinToString { it.qualifiedName!!.asString() }}")
        annotations.forEach { processAnnotation(resolver, it) }
        return emptyList()
    }

}