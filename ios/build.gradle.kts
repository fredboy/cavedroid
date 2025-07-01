plugins {
    kotlin
    id("robovm")
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

dependencies {
    useCommonModule()
    useZygoteModule()

    implementation(Dependencies.LibGDX.gdx)
    implementation(Dependencies.RoboVM.rt)
    implementation(Dependencies.RoboVM.cocoatouch)
    implementation(Dependencies.LibGDX.Ios.backend)
    implementation(Dependencies.LibGDX.Ios.natives)
}
