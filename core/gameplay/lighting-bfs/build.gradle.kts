plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

dependencies {
    implementation(libs.bundles.gdx)

    implementation(project(":core:common"))
    implementation(libs.bundles.common)
    implementation(project(":core:domain:items"))
    implementation(project(":core:domain:world"))
    implementation(project(":core:entity:mob"))
    api(project(":core:domain:configuration"))
    api(project(":core:game:world"))

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.junit.launcher)
}

tasks.test {
    useJUnitPlatform()
}
