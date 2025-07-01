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
    useEntityModules()
    useDomainModules()
}
