plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

dependencies {
    implementation(libs.bundles.gdx)
    implementation(libs.bundles.common)
    implementation(libs.dagger.runtime)
    ksp(libs.dagger.compiler)
    implementation(project(":core:common"))
    implementation(project(":core:domain:world"))
    implementation(project(":core:domain:items"))
    implementation(project(":core:domain:configuration"))
    implementation(project(":core:entity:mob"))
    implementation(project(":core:game:world"))
    implementation(project(":core:game:controller:mob"))
    implementation(project(":core:game:controller:vehicle"))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.junit.launcher)
    testImplementation(libs.mockk)
    testRuntimeOnly(variantOf(libs.gdx.platform.box2d) { classifier("natives-desktop") })
    testRuntimeOnly(variantOf(libs.gdx.platform.core) { classifier("natives-desktop") })
}

tasks.test { useJUnitPlatform() }
