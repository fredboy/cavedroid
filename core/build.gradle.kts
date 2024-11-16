plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("kotlin")
    id("idea")
    id("org.jetbrains.kotlin.plugin.serialization") version Versions.kotlin
    id ("com.google.devtools.ksp") version Versions.ksp
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

sourceSets {
    buildDir = file("_build")
}

dependencies {
    useAutomultibind()
    useDagger()
    useLibgdx()
    useKotlinxSerializationJson()
    useKotlinxSerializationProtobuf()

    useCommonModule()

    // data
    useModule(":core:data:assets")
    useModule(":core:data:items")
    useModule(":core:data:save")

    // domain
    useModule(":core:domain:assets")
    useModule(":core:domain:items")
    useModule(":core:domain:world")
    useModule(":core:domain:save")

    //entity
    useModule(":core:entity:container")
    useModule(":core:entity:drop")
    useModule(":core:entity:mob")

    // controller
    useModule(":core:game:controller:drop")
    useModule(":core:game:controller:container")
    useModule(":core:game:controller:mob")

    // world
    useModule(":core:game:world")

    implementation(Dependencies.jetbrainsAnnotations)
}
