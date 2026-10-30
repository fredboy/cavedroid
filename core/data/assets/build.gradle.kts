plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

dependencies {
    implementation(project(":core:common"))
    implementation(libs.bundles.common)
    implementation(project(":core:domain:assets"))
    implementation(project(":core:domain:configuration"))
    implementation(libs.bundles.gdx)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.dagger.runtime)
    ksp(libs.dagger.compiler)
}
