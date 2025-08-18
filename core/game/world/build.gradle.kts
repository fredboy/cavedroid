plugins {
    kotlin
    ksp
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

dependencies {
    useLibgdx()
    useDagger()

    useCommonModule()
    useDomainModules()
    useModule(":core:entity:container")
    useModule(":core:entity:drop")
    useModule(":core:entity:mob")
}
