plugins {
    kotlin("jvm")
    ksp
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

dependencies {
    useAutomultibind()
    useLibgdx()
    useLibKtx()
    useDagger()

    useCommonModule()
    useDataModules()
    useDomainModules()
    useDomainSaveModule()
    useEntityModules()
    useGameModules()
    useGameplayModules()
}
