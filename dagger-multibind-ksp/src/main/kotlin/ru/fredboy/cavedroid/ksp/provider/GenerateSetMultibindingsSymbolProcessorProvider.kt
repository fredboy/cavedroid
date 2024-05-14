package ru.fredboy.cavedroid.ksp.provider

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import ru.fredboy.cavedroid.ksp.processor.GenerateSetMultibindingsSymbolProcessor

internal class GenerateSetMultibindingsSymbolProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return GenerateSetMultibindingsSymbolProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
        )
    }

}