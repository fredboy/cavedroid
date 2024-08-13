plugins {
    kotlin
    ksp
    kotlinxSerialization
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

dependencies {
    useCommonModule()
    useModule(":core:domain:assets")
    useLibgdx()
    useKotlinxSerializationJson()
    useDagger()
}
