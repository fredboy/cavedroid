plugins {
    kotlin
    ksp
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

dependencies {
    useAutomultibind()
    useLibgdx()
    useDagger()

    useCommonModule()
    useDataModules()
    useDomainModules()
    useDomainSaveModule()
    useEntityModules()
    useGameModules()
    useUxModules()

    useModule(":core:domain:menu")
}
