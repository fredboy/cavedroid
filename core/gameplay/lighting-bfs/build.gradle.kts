plugins {
    `java-library`
    kotlin("jvm")
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

dependencies {
    useLibgdx()

    useCommonLibs()
    useModule(":core:domain:items")
    useModule(":core:domain:world")
    useModule(":core:entity:mob")
    useApiModule(":core:domain:configuration")
    useApiModule(":core:game:world")

    testImplementation(Dependencies.Test.junitJupiter)
    testRuntimeOnly(Dependencies.Test.junitJupiterEngine)
    testRuntimeOnly(Dependencies.Test.junitPlatformLauncher)
}

tasks.test {
    useJUnitPlatform()
}
