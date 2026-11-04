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
    implementation(project(":core:domain:assets"))
    implementation(project(":core:domain:configuration"))
    implementation(project(":core:domain:items"))
    implementation(project(":core:domain:world"))
    implementation(project(":core:entity:vehicle"))
    implementation(project(":core:entity:mob"))
    implementation(project(":core:entity:drop"))
    implementation(project(":core:game:world"))
    implementation(project(":core:game:controller:mob"))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.junit.launcher)
    testImplementation(libs.mockk)
    // Box2D World loads its native library even when mocked for interaction tests.
    testRuntimeOnly(variantOf(libs.gdx.platform.box2d) { classifier("natives-desktop") })
    testRuntimeOnly(variantOf(libs.gdx.platform.core) { classifier("natives-desktop") })
}

tasks.test { useJUnitPlatform() }
