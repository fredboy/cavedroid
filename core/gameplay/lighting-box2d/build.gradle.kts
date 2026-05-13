plugins {
    kotlin("jvm")
    ksp
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

dependencies {
    useLibgdx()
    useDagger()

    useCommonLibs()
    useModule(":core:domain:configuration")
    useModule(":core:domain:items")
    useModule(":core:domain:world")
    useModule(":core:entity:mob")
    useModule(":core:game:world")
}
