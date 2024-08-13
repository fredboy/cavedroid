plugins {
    kotlin
    ksp
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

dependencies {
    useCommonModule()
    useLibgdx()
    useDagger()
}
