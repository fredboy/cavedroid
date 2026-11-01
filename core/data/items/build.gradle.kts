plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

dependencies {
    implementation(project(":core:common"))
    implementation(libs.bundles.common)
    implementation(libs.bundles.gdx)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.junit.launcher)
    testImplementation(libs.mockk)
    implementation(libs.dagger.runtime)
    ksp(libs.dagger.compiler)

    implementation(project(":core:domain:assets"))
    implementation(project(":core:domain:items"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.processTestResources {
    from(rootProject.file("assets")) {
        include(
            "json/game_items.json",
            "json/crafting.json",
            "json/mobs.json",
            "json/texture_regions.json",
            "i18n/CaveDroid_Items_*.properties",
            "textures/textures/items/*.png",
            "textures/textures/blocks/*.png",
        )
    }
}
