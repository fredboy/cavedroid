plugins {
    kotlin("jvm")
    ksp
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

dependencies {
    useAutomultibind()
    useLibgdx()
    useDagger()

    useCommonLibs()
    useDomainModules()
    useEntityModules()
}
