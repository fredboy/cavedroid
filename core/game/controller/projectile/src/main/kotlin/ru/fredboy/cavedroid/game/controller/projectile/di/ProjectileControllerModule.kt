package ru.fredboy.cavedroid.game.controller.projectile.di

import dagger.Binds
import dagger.Module
import ru.fredboy.cavedroid.entity.mob.abstraction.ProjectileAdapter
import ru.fredboy.cavedroid.game.controller.projectile.ProjectileController

@Module
abstract class ProjectileControllerModule {

    @Binds
    abstract fun bindProjectileAdapter(impl: ProjectileController): ProjectileAdapter
}
