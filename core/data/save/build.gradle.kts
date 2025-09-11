plugins {
    kotlin("jvm")
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
    useModule(":core:domain:world")
    useModule(":core:domain:save")

    useModule(":core:entity:container")
    useModule(":core:entity:drop")
    useModule(":core:entity:mob")
    useModule(":core:entity:projectile")

    useModule(":core:game:controller:container")
    useModule(":core:game:controller:drop")
    useModule(":core:game:controller:mob")
    useModule(":core:game:controller:projectile")

    useModule(":core:game:world")
}
