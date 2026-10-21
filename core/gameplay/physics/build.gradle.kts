plugins {
    kotlin("jvm")
    ksp
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

dependencies {
    useLibgdx()
    useDagger()
    useAutomultibind()

    useCommonLibs()
    useDomainModules()
    useDomainSaveModule()
    useEntityModules()
    useGameModules()

    testImplementation(Dependencies.Test.mockk)
    testRuntimeOnly(Dependencies.LibGDX.Box2d.Natives.desktop)
    testRuntimeOnly(Dependencies.LibGDX.Desktop.natives)
    testImplementation(Dependencies.Test.junitJupiter)
    testRuntimeOnly(Dependencies.Test.junitJupiterEngine)
    testRuntimeOnly(Dependencies.Test.junitPlatformLauncher)
    testImplementation(Dependencies.Kotlin.coroutinesTest)
}

tasks.test {
    useJUnitPlatform()
}
