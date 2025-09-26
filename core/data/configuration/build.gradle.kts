plugins {
    kotlin("jvm")
    ksp
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

dependencies {
    useDagger()
    useLibgdx()
    useCommonLibs()
    useModule(":core:domain:configuration")
}
