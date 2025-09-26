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
    useModule(":core:entity:projectile")
    useModule(":core:entity:drop")
    useModule(":core:entity:mob")
}
