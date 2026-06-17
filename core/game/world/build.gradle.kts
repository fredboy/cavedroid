plugins {
    kotlin("jvm")
    ksp
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

dependencies {
    useLibgdx()
    useDagger()

    useCommonLibs()
    useDomainModules()
    useEntityModules()

    testImplementation(Dependencies.Test.junitJupiter)
    testRuntimeOnly(Dependencies.Test.junitJupiterEngine)
    testRuntimeOnly(Dependencies.Test.junitPlatformLauncher)
    testImplementation(Dependencies.Test.mockk)
}

tasks.test {
    useJUnitPlatform()
}
