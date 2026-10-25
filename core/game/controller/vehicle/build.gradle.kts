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
    // Box2D World loads its native library even when mocked for interaction tests.
    testRuntimeOnly(Dependencies.LibGDX.Box2d.Natives.desktop)
    testRuntimeOnly(Dependencies.LibGDX.Desktop.natives)
}

tasks.test { useJUnitPlatform() }
