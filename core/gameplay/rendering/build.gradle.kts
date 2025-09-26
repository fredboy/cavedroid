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
    useDataModules()
    useDomainModules()
    useEntityModules()
    useGameModules()
}
