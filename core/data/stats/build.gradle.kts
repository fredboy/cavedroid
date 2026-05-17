plugins {
    kotlin("jvm")
    ksp
    kotlinxSerialization
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

dependencies {
    useCommonLibs()
    useLibgdx()
    useKotlinxSerializationProtobuf()
    useDagger()

    useDomainStatsModule()
    useModule(":core:domain:configuration")
}
