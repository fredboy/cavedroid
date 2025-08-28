package ru.fredboy.cavedroid.gdx.game.di

import dagger.BindsInstance
import dagger.Component
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.data.configuration.di.GameContextModule
import ru.fredboy.cavedroid.data.configuration.model.GameContext
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.game.controller.container.di.ControllerContainerModule
import ru.fredboy.cavedroid.game.controller.drop.di.ControllerDropModule
import ru.fredboy.cavedroid.game.controller.mob.di.MobControllerModule
import ru.fredboy.cavedroid.game.world.di.GameWorldModule
import ru.fredboy.cavedroid.gameplay.physics.di.PhysicsModule
import ru.fredboy.cavedroid.gdx.di.ApplicationComponent
import ru.fredboy.cavedroid.gdx.game.GameProc
import ru.fredboy.cavedroid.gdx.game.GameSaveHelper
import ru.fredboy.cavedroid.generated.module.DropContactHandlerModule
import ru.fredboy.cavedroid.generated.module.HudRenderModule
import ru.fredboy.cavedroid.generated.module.KeyboardInputHandlersModule
import ru.fredboy.cavedroid.generated.module.MobContactHandlerModule
import ru.fredboy.cavedroid.generated.module.MouseInputHandlersModule
import ru.fredboy.cavedroid.generated.module.PlaceBlockActionsModule
import ru.fredboy.cavedroid.generated.module.UpdateBlockActionsModule
import ru.fredboy.cavedroid.generated.module.UseBlockActionsModule
import ru.fredboy.cavedroid.generated.module.UseItemActionsModule
import ru.fredboy.cavedroid.generated.module.UseMobActionsModule
import ru.fredboy.cavedroid.generated.module.WorldRenderModule

@GameScope
@Component(
    dependencies = [ApplicationComponent::class],
    modules = [
        GameModule::class,
        UseItemActionsModule::class,
        UpdateBlockActionsModule::class,
        PlaceBlockActionsModule::class,
        KeyboardInputHandlersModule::class,
        MouseInputHandlersModule::class,
        UseBlockActionsModule::class,
        GameWorldModule::class,
        ControllerContainerModule::class,
        ControllerDropModule::class,
        GameContextModule::class,
        MobControllerModule::class,
        WorldRenderModule::class,
        HudRenderModule::class,
        PhysicsModule::class,
        DropContactHandlerModule::class,
        MobContactHandlerModule::class,
        UseMobActionsModule::class,
    ],
)
interface GameComponent {

    val gameProc: GameProc

    val gameContextRepository: GameContextRepository

    val gameSaveHelper: GameSaveHelper

    @Component.Builder
    interface Builder {

        fun applicationComponent(component: ApplicationComponent): Builder

        @BindsInstance
        fun gameContext(context: GameContext): Builder

        fun build(): GameComponent
    }
}
