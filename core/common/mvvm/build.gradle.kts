plugins {
    kotlin("jvm")
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

dependencies {
    useLibgdx()
    useLibKtx()
    useKotlinxCoroutines()

    testImplementation(Dependencies.Test.junitJupiter)
    testRuntimeOnly(Dependencies.Test.junitJupiterEngine)
    testRuntimeOnly(Dependencies.Test.junitPlatformLauncher)
    testImplementation(Dependencies.Test.mockk)
    testImplementation(Dependencies.Kotlin.coroutinesTest)
}

tasks.test {
    useJUnitPlatform()
}
