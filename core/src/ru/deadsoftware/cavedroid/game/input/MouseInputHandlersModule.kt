package ru.deadsoftware.cavedroid.game.input

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.input.action.MouseInputAction
import ru.deadsoftware.cavedroid.game.input.handler.mouse.*

@Module
object MouseInputHandlersModule {

    @Binds
    @IntoSet
    @GameScope
    fun bindCursorMouseInputHandler(handler: CursorMouseInputHandler): IGameInputHandler<MouseInputAction> {
        return handler
    }

    @Binds
    @IntoSet
    @GameScope
    fun bindHoldHotbarMouseInputHandler(handler: HotbarMouseInputHandler): IGameInputHandler<MouseInputAction> {
        return handler
    }

    @Binds
    @IntoSet
    @GameScope
    fun bindCloseGameWindowMouseActionHandler(handler: CloseGameWindowMouseInputHandler): IGameInputHandler<MouseInputAction> {
        return handler
    }

    @Binds
    @IntoSet
    @GameScope
    fun bindCreativeInventoryScrollMouseInputHandler(handler: CreativeInventoryScrollMouseInputHandler): IGameInputHandler<MouseInputAction> {
        return handler
    }

    @Binds
    @IntoSet
    @GameScope
    fun bindSelectCreativeInventoryItemMouseActionHandler(handler: SelectCreativeInventoryItemMouseInputHandler): IGameInputHandler<MouseInputAction> {
        return handler
    }

    @Binds
    @IntoSet
    @GameScope
    fun bindAttackMouseInputHandler(handler: AttackMouseInputHandler): IGameInputHandler<MouseInputAction> {
        return handler
    }

    @Binds
    @IntoSet
    @GameScope
    fun bindUseItemMouseInputActionHandler(handler: UseItemMouseInputHandler): IGameInputHandler<MouseInputAction> {
        return handler
    }

    @Binds
    @IntoSet
    @GameScope
    fun bindSelectSurvivalInventoryItemMouseInputHandler(handler: SelectSurvivalInventoryItemMouseInputHandler): IGameInputHandler<MouseInputAction> {
        return handler
    }
}