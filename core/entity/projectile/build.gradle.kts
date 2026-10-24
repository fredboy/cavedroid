plugins {
    kotlin("jvm")
    ksp
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

dependencies {
    useModule(":core:entity:vehicle")
    useLibgdx()
    useDagger()

    useCommonLibs()
    useModule(":core:domain:items")
    useModule(":core:domain:world")
}
