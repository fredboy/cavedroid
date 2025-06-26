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

    useDataModules()
    useDomainModules()
    useEntityModules()
    useGameModules()
    useUxModules()

    implementation(Dependencies.jetbrainsAnnotations)
}
