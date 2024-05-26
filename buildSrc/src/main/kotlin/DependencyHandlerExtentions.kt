import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.project

private fun DependencyHandler.implementation(dependency: String) =
    add("implementation", dependency)

private fun DependencyHandler.ksp(dependency: String) =
    add("ksp", dependency)

fun DependencyHandler.useModule(moduleName: String) {
    add("implementation", project(moduleName))
}

fun DependencyHandler.useBaseModule() {
    useModule(":core:base")
}

fun DependencyHandler.useAutomultibind() {
    implementation(Dependencies.Automultibind.annotations)
    ksp(Dependencies.Automultibind.ksp)
}

fun DependencyHandler.useLibgdx() {
    implementation(Dependencies.LibGDX.gdx)
}

fun DependencyHandler.useDagger() {
    implementation(Dependencies.Dagger.dagger)
    ksp(Dependencies.Dagger.compiler)
}

fun DependencyHandler.useKotlinxSerializationJson() {
    implementation(Dependencies.Kotlin.Serialization.json)
}

fun DependencyHandler.useKotlinxSerializationProtobuf() {
    implementation(Dependencies.Kotlin.Serialization.protobuf)
}
