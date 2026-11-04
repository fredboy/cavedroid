plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

dependencies {
    implementation(libs.dagger.runtime)
    ksp(libs.dagger.compiler)
    implementation(libs.bundles.gdx)
    implementation(project(":core:common"))
    implementation(libs.bundles.common)
    implementation(project(":core:domain:configuration"))
}
