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
    useDomainStatsModule()
    useModule(":core:domain:configuration")
    useModule(":core:domain:items")
    useModule(":core:domain:world")
    useEntityModules()
    useModule(":core:game:controller:mob")
    useModule(":core:game:world")
}
