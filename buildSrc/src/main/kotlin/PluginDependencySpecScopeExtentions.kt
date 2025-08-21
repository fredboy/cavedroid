import org.gradle.kotlin.dsl.PluginDependenciesSpecScope
import org.gradle.kotlin.dsl.version

val PluginDependenciesSpecScope.kotlin
    get() = id("kotlin")
val PluginDependenciesSpecScope.ksp
    get() = id("com.google.devtools.ksp") version Versions.ksp
val PluginDependenciesSpecScope.kotlinxSerialization
    get() = id("org.jetbrains.kotlin.plugin.serialization") version Versions.kotlin
val PluginDependenciesSpecScope.ktlintGradle
    get() = id("org.jlleitschuh.gradle.ktlint") version Versions.ktlintGradle

val PluginDependenciesSpecScope.construo
    get() = id("io.github.fourlastor.construo") version Versions.construo

