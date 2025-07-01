package ru.fredboy.cavedroid.zygote.game.di

import dagger.BindsInstance
import dagger.Component
import ru.deadsoftware.cavedroid.generated.module.*
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.data.configuration.di.GameContextModule
import ru.fredboy.cavedroid.data.configuration.model.GameContext
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.game.controller.container.di.ControllerContainerModule
import ru.fredboy.cavedroid.game.controller.drop.di.ControllerDropModule
import ru.fredboy.cavedroid.game.controller.mob.di.MobControllerModule
import ru.fredboy.cavedroid.game.world.di.GameWorldModule
import ru.fredboy.cavedroid.zygote.di.ApplicationComponent
import ru.fredboy.cavedroid.zygote.game.GameProc

@GameScope
@Component(
    dependencies = [ ApplicationComponent::class ],
    modules = [
        GameModule::class,
        UseItemActionsModule::class,
        UpdateBlockActionsModule::class,
        PlaceBlockActionsModule::class,
        RenderModule::class,
        KeyboardInputHandlersModule::class,
        MouseInputHandlersModule::class,
        UseBlockActionsModule::class,
        GameWorldModule::class,
        ControllerContainerModule::class,
        ControllerDropModule::class,
        GameContextModule::class,
        MobControllerModule::class,
    ]
)
interface GameComponent {

    val gameProc: GameProc

    val gameContextRepository: GameContextRepository

    @Component.Builder
    interface Builder {

        fun applicationComponent(component: ApplicationComponent): Builder

        @BindsInstance
        fun gameContext(context: GameContext): Builder

        fun build(): GameComponent
    }
}