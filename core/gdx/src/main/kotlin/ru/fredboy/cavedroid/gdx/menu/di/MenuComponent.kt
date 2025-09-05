package ru.fredboy.cavedroid.gdx.menu.di

import dagger.BindsInstance
import dagger.Component
import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.gdx.di.ApplicationComponent
import ru.fredboy.cavedroid.gdx.menu.v2.MenuNavigationController
import ru.fredboy.cavedroid.gdx.menu.v2.generated.ViewModelProviderModule
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.RootNavKey

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

        @BindsInstance
        fun rootNavKey(impl: RootNavKey): Builder

        fun build(): MenuComponent
    }
}
