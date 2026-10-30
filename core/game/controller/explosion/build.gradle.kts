plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

dependencies {
    implementation(project(":core:entity:vehicle"))
    implementation(project(":core:game:controller:vehicle"))
    implementation(libs.bundles.gdx)
    implementation(libs.dagger.runtime)
    ksp(libs.dagger.compiler)
    implementation(project(":core:common"))
    implementation(libs.bundles.common)
    implementation(project(":core:domain:assets"))
    implementation(project(":core:domain:configuration"))
    implementation(project(":core:domain:items"))
    implementation(project(":core:domain:world"))
    implementation(project(":core:domain:save"))
    implementation(project(":core:entity:drop"))
    implementation(project(":core:entity:mob"))
    implementation(project(":core:game:world"))
    implementation(project(":core:game:controller:fire"))
    implementation(project(":core:game:controller:mob"))

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.junit.launcher)
    testImplementation(libs.mockk)
}

tasks.test { useJUnitPlatform() }
