plugins {
    id("cavedroid.kotlin-library")
    id("cavedroid.dagger")
    id("cavedroid.libgdx")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(projects.core.common)
    implementation(libs.kermit)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.protobuf)

    implementation(projects.core.domain.stats)
    implementation(projects.core.domain.configuration)
}
