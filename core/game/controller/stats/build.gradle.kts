plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

dependencies {
    implementation(libs.bundles.gdx)
    implementation(libs.dagger.runtime)
    ksp(libs.dagger.compiler)

    implementation(project(":core:common"))
    implementation(libs.bundles.common)
    implementation(project(":core:domain:stats"))
    implementation(project(":core:domain:configuration"))
    implementation(project(":core:domain:items"))
    implementation(project(":core:domain:world"))
    implementation(project(":core:entity:container"))
    implementation(project(":core:entity:drop"))
    implementation(project(":core:entity:mob"))
    implementation(project(":core:entity:projectile"))
    implementation(project(":core:entity:vehicle"))
    implementation(project(":core:game:controller:mob"))
    implementation(project(":core:game:world"))
}
