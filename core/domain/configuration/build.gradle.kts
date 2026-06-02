plugins {
    id("cavedroid.kotlin-library")
    id("com.google.devtools.ksp")
    id("cavedroid.libgdx")
}

dependencies {
    implementation(projects.core.common)
    implementation(libs.kermit)
    implementation(libs.kotlinx.coroutines.core)
}
