plugins {
    kotlin("jvm")
    ksp
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

dependencies {
    useModule(":core:entity:vehicle")
    useModule(":core:game:controller:vehicle")
    useLibgdx()
    useDagger()
    useCommonLibs()
    useDomainModules()
    useDomainSaveModule()
    useModule(":core:entity:drop")
    useModule(":core:entity:mob")
    useModule(":core:game:world")
    useModule(":core:game:controller:fire")
    useModule(":core:game:controller:mob")

    testImplementation(Dependencies.Test.junitJupiter)
    testRuntimeOnly(Dependencies.Test.junitJupiterEngine)
    testRuntimeOnly(Dependencies.Test.junitPlatformLauncher)
    testImplementation(Dependencies.Test.mockk)
}

tasks.test { useJUnitPlatform() }
