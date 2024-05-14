package ru.fredboy.cavedroid.ksp.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.validate
import kotlin.reflect.KClass

internal fun Resolver.getAnnotatedClasses(
    packageName: String,
    annotationClass: KClass<*>
): Sequence<KSClassDeclaration> {
    return getSymbolsWithAnnotation(annotationClass.qualifiedName.orEmpty())
        .filterIsInstance<KSClassDeclaration>()
        .filter { it.packageName.getShortName() == packageName }
        .filter(KSNode::validate)
}