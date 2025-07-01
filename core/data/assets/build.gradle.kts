plugins {
    kotlin
    ksp
    kotlinxSerialization
    ktlintGradle
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
