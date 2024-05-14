package ru.deadsoftware.cavedroid.game.input

import ru.deadsoftware.cavedroid.game.input.action.IGameInputAction
import ru.deadsoftware.cavedroid.game.input.action.KeyboardInputAction
import ru.deadsoftware.cavedroid.game.input.action.MouseInputAction
import ru.fredboy.cavedroid.ksp.annotations.GenerateSetMultibindingsModule

@GenerateSetMultibindingsModule(
    interfaceClass = IKeyboardInputHandler::class,
    modulePackage = "ru.deadsoftware.cavedroid.game.input",
    moduleName = "KeyboardInputHandlersModule"
)
annotation class KeyboardInputHandler

@GenerateSetMultibindingsModule(
    interfaceClass = IMouseInputHandler::class,
    modulePackage = "ru.deadsoftware.cavedroid.game.input",
    moduleName = "MouseInputHandlersModule"
)
annotation class MouseInputHandler

interface IKeyboardInputHandler : IGameInputHandler<KeyboardInputAction>

interface IMouseInputHandler : IGameInputHandler<MouseInputAction>

interface IGameInputHandler<A : IGameInputAction> {

    /**
     * Implementation should check if conditions for handling an input are satisfied
     * For example - inventory input handler should return false if inventory is closed
     */
    fun checkConditions(action: A): Boolean

    /**
     * Handle given input action.
     * This will not be called if [checkConditions] returned false
     */
    fun handle(action: A)

}