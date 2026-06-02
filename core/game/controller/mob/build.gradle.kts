plugins {
    id("cavedroid.kotlin-library")
    id("cavedroid.dagger")
    id("cavedroid.automultibind")
    id("cavedroid.libgdx")
}

dependencies {
    implementation(projects.core.common)
    implementation(libs.kermit)
    implementation(libs.kotlinx.coroutines.core)

    implementation(projects.core.domain.assets)
    implementation(projects.core.domain.configuration)
    implementation(projects.core.domain.items)
    implementation(projects.core.domain.world)

    implementation(projects.core.entity.container)
    implementation(projects.core.entity.drop)
    implementation(projects.core.entity.mob)
    implementation(projects.core.entity.projectile)
}
