plugins {
    id("cavedroid.kotlin-library")
    id("cavedroid.libgdx")
}

dependencies {
    implementation(libs.ktx.scene2d)
    implementation(libs.ktx.actors)
    implementation(libs.kotlinx.coroutines.core)
}
