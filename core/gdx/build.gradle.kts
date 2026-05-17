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

    useCommonLibs()
    useMvvmModule()
    useDataModules()
    useDomainModules()
    useDomainSaveModule()
    useDomainStatsModule()
    useDataStatsModule()
    useEntityModules()
    useGameModules()
    useGameplayModules()
}
