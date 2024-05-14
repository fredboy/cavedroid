package ru.fredboy.cavedroid.ksp.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class GenerateSetMultibindingsModule(
    val interfaceClass: KClass<*>,
    val modulePackage: String,
    val moduleName: String,
)
