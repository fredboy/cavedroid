package ru.fredboy.cavedroid.gdx.menu.di

import dagger.Component
import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.gdx.di.ApplicationComponent
import ru.fredboy.cavedroid.gdx.menu.v2.MenuNavigationController
import ru.fredboy.cavedroid.gdx.menu.v2.generated.ViewModelProviderModule

@MenuScope
@Component(
    dependencies = [ApplicationComponent::class],
    modules = [
        ViewModelProviderModule::class,
    ],
)
interface MenuComponent {

    val menuNavigationController: MenuNavigationController

    @Component.Builder
    interface Builder {

        fun applicationComponent(impl: ApplicationComponent): Builder

        fun build(): MenuComponent
    }
}
