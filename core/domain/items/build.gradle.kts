plugins {
    id("cavedroid.kotlin-library")
    id("cavedroid.dagger")
    id("cavedroid.libgdx")
}

dependencies {
    implementation(projects.core.common)
    implementation(libs.kermit)
    implementation(libs.kotlinx.coroutines.core)
}
