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
    useModule(":core:entity:vehicle")
    useModule(":core:entity:mob")
    useModule(":core:entity:drop")
    useModule(":core:game:world")
    useModule(":core:game:controller:mob")
    testImplementation(Dependencies.Test.junitJupiter)
    testRuntimeOnly(Dependencies.Test.junitJupiterEngine)
    testRuntimeOnly(Dependencies.Test.junitPlatformLauncher)
    testImplementation(Dependencies.Test.mockk)
}

tasks.test { useJUnitPlatform() }
