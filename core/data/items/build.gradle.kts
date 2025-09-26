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
    useKotlinxSerializationJson()
    useDagger()

    useModule(":core:domain:assets")
    useModule(":core:domain:items")
}
