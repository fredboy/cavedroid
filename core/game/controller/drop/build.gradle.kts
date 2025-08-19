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

    useCommonModule()
    useModule(":core:domain:items")
    useModule(":core:domain:world")
    useModule(":core:entity:drop")
    useModule(":core:entity:mob")
}
