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

    useBaseModule()

    // data
    useModule(":core:data:assets")

    //domain
    useModule(":core:domain:assets")

    implementation(Dependencies.jetbrainsAnnotations)
}