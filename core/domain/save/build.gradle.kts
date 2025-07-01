plugins {
    kotlin
    ksp
    ktlintGradle
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

dependencies {
    useCommonModule()
    useLibgdx()
    useDagger()

    useModule(":core:domain:items")
    useModule(":core:domain:assets")
    useModule(":core:domain:world")

    useModule(":core:entity:container")
    useModule(":core:entity:drop")
    useModule(":core:entity:mob")

    useModule(":core:game:controller:container")
    useModule(":core:game:controller:drop")
    useModule(":core:game:controller:mob")
    useModule(":core:game:world")
}
