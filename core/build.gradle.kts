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

dependencies {
    implementation(Dependencies.Automultibind.annotations)
    ksp(Dependencies.Automultibind.ksp)

    implementation(Dependencies.LibGDX.gdx)
    implementation(Dependencies.Dagger.dagger)

    implementation(Dependencies.jetbrainsAnnotations)
    implementation(Dependencies.Kotlin.stdlib)
    implementation(Dependencies.Kotlin.Serialization.json)
    implementation(Dependencies.Kotlin.Serialization.protobuf)

    ksp(Dependencies.Dagger.compiler)
}