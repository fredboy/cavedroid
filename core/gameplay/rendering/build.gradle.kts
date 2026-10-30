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
    implementation(libs.automultibind.annotations)
    ksp(libs.automultibind.ksp)

    implementation(project(":core:common"))
    implementation(libs.bundles.common)
    implementation(project(":core:common"))
    implementation(project(":core:data:assets"))
    implementation(project(":core:data:configuration"))
    implementation(project(":core:data:items"))
    implementation(project(":core:data:save"))
    implementation(project(":core:domain:assets"))
    implementation(project(":core:domain:configuration"))
    implementation(project(":core:domain:items"))
    implementation(project(":core:domain:world"))
    implementation(project(":core:entity:container"))
    implementation(project(":core:entity:drop"))
    implementation(project(":core:entity:mob"))
    implementation(project(":core:entity:projectile"))
    implementation(project(":core:entity:vehicle"))
    implementation(project(":core:game:controller:container"))
    implementation(project(":core:game:controller:drop"))
    implementation(project(":core:game:controller:fire"))
    implementation(project(":core:game:controller:explosion"))
    implementation(project(":core:game:controller:mob"))
    implementation(project(":core:game:controller:projectile"))
    implementation(project(":core:game:controller:vehicle"))
    implementation(project(":core:game:controller:stats"))
    implementation(project(":core:game:window"))
    implementation(project(":core:game:world"))

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.junit.launcher)
    testImplementation(libs.mockk)
}

tasks.test {
    useJUnitPlatform()
}
