plugins {
    kotlin
    ksp
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

dependencies {
    useDagger()
    useLibgdx()
    useCommonModule()
    useModule(":core:domain:configuration")
}
