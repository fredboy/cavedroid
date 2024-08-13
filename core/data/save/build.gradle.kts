plugins {
    kotlin
    ksp
    kotlinxSerialization
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

dependencies {
    useCommonModule()
    useLibgdx()
    useKotlinxSerializationProtobuf()
    useDagger()

    useModule(":core:domain:assets")
    useModule(":core:domain:items")
    useModule(":core:domain:save")

    useModule(":core:game:controller:container")
    useModule(":core:game:controller:drop")
    useModule(":core:game:controller:mob")
    useModule(":core:game:world")
}
