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
    useModule(":core:domain:assets")
    useModule(":core:domain:items")
    useModule(":core:domain:world:")
    useEntityModules()
}
