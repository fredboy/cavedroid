plugins {
    `java-library`
    id("cavedroid.kotlin-library")
    id("cavedroid.libgdx")
}

dependencies {
    implementation(projects.core.common)
    implementation(libs.kermit)
    implementation(libs.kotlinx.coroutines.core)

    implementation(projects.core.domain.items)
    implementation(projects.core.domain.world)
    implementation(projects.core.entity.mob)
    api(projects.core.domain.configuration)
    api(projects.core.game.world)
}
