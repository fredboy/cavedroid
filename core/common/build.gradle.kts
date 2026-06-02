plugins {
    id("cavedroid.kotlin-library")
    id("cavedroid.dagger")
}

dependencies {
    implementation(libs.gdx)
    implementation(libs.gdx.box2d)
    implementation(libs.kotlinx.coroutines.core)
}
