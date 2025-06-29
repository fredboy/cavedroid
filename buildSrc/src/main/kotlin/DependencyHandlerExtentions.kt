import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.project

private fun DependencyHandler.implementation(dependency: String) =
    add("implementation", dependency)

private fun DependencyHandler.ksp(dependency: String) =
    add("ksp", dependency)

fun DependencyHandler.useModule(moduleName: String) {
    add("implementation", project(moduleName))
}

fun DependencyHandler.useCommonModule() {
    useModule(":core:common")
}

fun DependencyHandler.useDataModules() {
    useModule(":core:data:assets")
    useModule(":core:data:configuration")
    useModule(":core:data:items")
    useModule(":core:data:menu")
    useModule(":core:data:save")
}

fun DependencyHandler.useDomainModules() {
    useModule(":core:domain:assets")
    useModule(":core:domain:configuration")
    useModule(":core:domain:items")
    useModule(":core:domain:save")
    useModule(":core:domain:menu")
    useModule(":core:domain:world")
}

fun DependencyHandler.useEntityModules() {
    useModule(":core:entity:container")
    useModule(":core:entity:drop")
    useModule(":core:entity:mob")
}

fun DependencyHandler.useGameModules() {
    useModule(":core:game:controller:container")
    useModule(":core:game:controller:drop")
    useModule(":core:game:controller:mob")
    useModule(":core:game:window")
    useModule(":core:game:world")
}

fun DependencyHandler.useUxModules() {
    useModule(":core:ux:controls")
    useModule(":core:ux:physics")
    useModule(":core:ux:rendering")
}

fun DependencyHandler.useZygoteModule() {
    useModule(":core:zygote")
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
