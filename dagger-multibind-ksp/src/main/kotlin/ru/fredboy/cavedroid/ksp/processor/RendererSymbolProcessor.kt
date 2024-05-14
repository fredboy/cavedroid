package ru.fredboy.cavedroid.ksp.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import ru.fredboy.cavedroid.ksp.annotations.render.Renderer
import java.lang.reflect.Type

internal class RendererSymbolProcessor(
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {

    private fun generateModule(renderers: List<ClassName>): FileSpec? {
        if (renderers.isEmpty()) {
            return null
        }

        val bindings = renderers.map { renderer ->
            FunSpec.builder("bind${renderer.simpleName}")
                .addAnnotation(ClassName("dagger", "Binds"))
                .addAnnotation(ClassName("dagger", "IntoSet"))
                .addAnnotations(renderer.annotations)
                .addParameter(ParameterSpec("renderer", renderer))
                .returns(IGameRendererType)
                .beginControlFlow("return renderer")
                .build()
        }

        val moduleObject = TypeSpec.objectBuilder(MODULE_NAME)
            .addAnnotation(ClassName("dagger", "Module"))
            .addFunctions(bindings)
            .build()

        return FileSpec.builder(MODULE_PACKAGE, MODULE_NAME)
//            .addImport("dagger", "Binds", "Module", "IntoSet")
//            .addImport("ru.deadsoftware.cavedroid.game", "GameScope")
            .addType(moduleObject)
            .build()

    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        generateModule(
            resolver.getAnnotatedClasses(MODULE_PACKAGE, Renderer::class)
                .map(KSClassDeclaration::toClassName)
                .toList()
        )?.writeTo(codeGenerator, Dependencies(true))

        return emptyList()
    }

    private object IGameRendererType : Type {
        override fun getTypeName(): String = "IGameRenderer"
    }

    companion object {
        private const val MODULE_PACKAGE = "ru.deadsoftware.cavedroid.game.render"
        private const val MODULE_NAME = "RenderModule"
    }
}