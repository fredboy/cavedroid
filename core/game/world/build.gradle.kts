plugins {
    kotlin
    ksp
    ktlintGradle
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

dependencies {
    useLibgdx()
    useDagger()

    useCommonModule()
    useModule(":core:domain:items")
    useModule(":core:domain:world")
    useModule(":core:entity:container")
    useModule(":core:entity:drop")
    useModule(":core:entity:mob")
}
