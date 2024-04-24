package ru.deadsoftware.cavedroid.game.input

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.input.action.KeyboardInputAction
import ru.deadsoftware.cavedroid.game.input.handler.keyboard.*

@Module
object KeyboardInputHandlersModule {
    
    @Binds
    @IntoSet
    @GameScope
    fun bindGoLeftKeyboardInputHandler(handler: GoLeftKeyboardInputHandler): IGameInputHandler<KeyboardInputAction> {
        return handler
    }

    @Binds
    @IntoSet
    @GameScope
    fun bindGoRightKeyboardInputHandler(handler: GoRightKeyboardInputHandler): IGameInputHandler<KeyboardInputAction> {
        return handler
    }

    @Binds
    @IntoSet
    @GameScope
    fun bindJumpKeyboardActionHandler(handler: JumpKeyboardInputHandler): IGameInputHandler<KeyboardInputAction> {
        return handler
    }

    @Binds
    @IntoSet
    @GameScope
    fun bindFlyUpKeyboardActionHandler(handler: FlyUpKeyboardInputHandler): IGameInputHandler<KeyboardInputAction> {
        return handler
    }

    @Binds
    @IntoSet
    @GameScope
    fun bindTurnOnFlyModeKeyboardActionHandler(handler: TurnOnFlyModeKeyboardInputHandler): IGameInputHandler<KeyboardInputAction> {
        return handler
    }

    @Binds
    @IntoSet
    @GameScope
    fun bindFlyDownKeyboardActionHandler(handler: FlyDownKeyboardInputHandler): IGameInputHandler<KeyboardInputAction> {
        return handler
    }

    @Binds
    @IntoSet
    @GameScope
    fun bindOpenInventoryKeyboardInputHandler(handler: OpenInventoryKeyboardInputHandler): IGameInputHandler<KeyboardInputAction> {
        return handler
    }

    @Binds
    @IntoSet
    @GameScope
    fun bindCloseGameWindowKeyboardInputHandler(handler: CloseGameWindowKeyboardInputHandler): IGameInputHandler<KeyboardInputAction> {
        return handler
    }

    @Binds
    @IntoSet
    @GameScope
    fun bindToggleDebugInfoKeyboardInputHandler(handler: ToggleDebugInfoKeyboardInputHandler): IGameInputHandler<KeyboardInputAction> {
        return handler
    }

    @Binds
    @IntoSet
    @GameScope
    fun bindToggleMinimapKeyboardInputHandler(handler: ToggleMinimapKeyboardInputHandler): IGameInputHandler<KeyboardInputAction> {
        return handler
    }

    @Binds
    @IntoSet
    @GameScope
    fun bindToggleGameModeKeyboardInputHandler(handler: ToggleGameModeKeyboardInputHandler): IGameInputHandler<KeyboardInputAction> {
        return handler
    }

    @Binds
    @IntoSet
    @GameScope
    fun bindPauseGameKeyboardInputHandler(handler: PauseGameKeyboardInputHandler): IGameInputHandler<KeyboardInputAction> {
        return handler
    }

    @Binds
    @IntoSet
    @GameScope
    fun bindToggleControlsModeKeyboardInputHandler(handler: ToggleControlsModeKeyboardInputHandler): IGameInputHandler<KeyboardInputAction> {
        return handler
    }

    @Binds
    @IntoSet
    @GameScope
    fun bindMoveCursorControlsModeKeyboardInputHandler(handler: MoveCursorControlsModeKeyboardInputHandler): IGameInputHandler<KeyboardInputAction> {
        return handler
    }

    @Binds
    @IntoSet
    @GameScope
    fun bindOpenCraftingKeyboardInputHandler(handler: OpenCraftingKeyboardInputHandler): IGameInputHandler<KeyboardInputAction> {
        return handler
    }

}