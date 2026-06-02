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
    implementation(projects.core.domain.save)
    implementation(projects.core.domain.stats)

    implementation(projects.core.entity.container)
    implementation(projects.core.entity.drop)
    implementation(projects.core.entity.mob)
    implementation(projects.core.entity.projectile)

    implementation(projects.core.game.controller.container)
    implementation(projects.core.game.controller.drop)
    implementation(projects.core.game.controller.fire)
    implementation(projects.core.game.controller.mob)
    implementation(projects.core.game.controller.projectile)
    implementation(projects.core.game.controller.stats)
    implementation(projects.core.game.window)
    implementation(projects.core.game.world)
}
