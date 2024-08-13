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
    useModule(":core:domain:items")

    useModule(":core:game:controller:container")
    useModule(":core:game:controller:drop")
    useModule(":core:game:controller:mob")
}
