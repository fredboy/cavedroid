plugins {
    kotlin("jvm")
    ksp
    kotlinxSerialization
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

dependencies {
    useCommonLibs()
    useModule(":core:domain:assets")
    useModule(":core:domain:configuration")
    useLibgdx()
    useKotlinxSerializationJson()
    useDagger()
}
