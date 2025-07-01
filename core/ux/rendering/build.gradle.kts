plugins {
    kotlin
    ksp
    ktlintGradle
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

dependencies {
    useLibgdx()
    useDagger()
    useAutomultibind()

    useCommonModule()
    useDataModules()
    useDomainModules()
    useEntityModules()
    useGameModules()
}
