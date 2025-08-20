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
    useKotlinxCoroutines()

    useCommonModule()
    useDataModules()
    useDomainModules()
    useDomainSaveModule()
    useEntityModules()
    useGameModules()
    useGameplayModules()
}
