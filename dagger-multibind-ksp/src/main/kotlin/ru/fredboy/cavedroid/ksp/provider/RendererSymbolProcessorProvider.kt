package ru.fredboy.cavedroid.ksp.provider

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import ru.fredboy.cavedroid.ksp.processor.RendererSymbolProcessor

internal class RendererSymbolProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return RendererSymbolProcessor(
            codeGenerator = environment.codeGenerator,
        )
    }

}