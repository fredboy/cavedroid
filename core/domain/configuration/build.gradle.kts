plugins {
    id("cavedroid.kotlin-library")
    alias(libs.plugins.ksp)
    id("cavedroid.libgdx")
}

dependencies {
    implementation(projects.core.common)
    implementation(libs.kermit)
    implementation(libs.kotlinx.coroutines.core)
}
