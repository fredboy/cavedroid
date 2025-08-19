package ru.fredboy.cavedroid.gdx.menu.di

import dagger.Component
import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.data.menu.di.DataMenuModule
import ru.fredboy.cavedroid.domain.menu.repository.MenuButtonRepository
import ru.fredboy.cavedroid.gdx.di.ApplicationComponent
import ru.fredboy.cavedroid.gdx.menu.input.MenuInputProcessor
import ru.fredboy.cavedroid.gdx.menu.renderer.MenuRenderer
import ru.fredboy.cavedroid.gdx.menu.v2.MenuNavigationController
import ru.fredboy.cavedroid.gdx.menu.v2.generated.ViewModelProviderModule
import ru.fredboy.cavedroid.generated.module.MenuActionsModule
import ru.fredboy.cavedroid.generated.module.MenuBooleanOptionsModule
import ru.fredboy.cavedroid.generated.module.MenuNumericalOptionsModule

@MenuScope
@Component(
    dependencies = [ApplicationComponent::class],
    modules = [
        DataMenuModule::class,
        MenuBooleanOptionsModule::class,
        MenuActionsModule::class,
        MenuNumericalOptionsModule::class,
        ViewModelProviderModule::class,
    ],
)
interface MenuComponent {

    val menuRenderer: MenuRenderer

    val menuInputProcessor: MenuInputProcessor

    val menuButtonRepository: MenuButtonRepository

    val menuNavigationController: MenuNavigationController

    @Component.Builder
    interface Builder {

        fun applicationComponent(impl: ApplicationComponent): Builder

        fun build(): MenuComponent
    }
}
