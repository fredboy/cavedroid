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
    useModule(":core:domain:configuration")
    useModule(":core:domain:items")
    useModule(":core:domain:world")
    useDomainSaveModule()
    useModule(":core:game:world")

    testImplementation(Dependencies.Test.junitJupiter)
    testRuntimeOnly(Dependencies.Test.junitJupiterEngine)
    testRuntimeOnly(Dependencies.Test.junitPlatformLauncher)
    testImplementation(Dependencies.Test.mockk)
}

tasks.test { useJUnitPlatform() }
