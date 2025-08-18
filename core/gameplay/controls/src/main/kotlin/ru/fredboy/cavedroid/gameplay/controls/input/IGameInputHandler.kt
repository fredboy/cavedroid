package ru.fredboy.cavedroid.gameplay.controls.input

import ru.fredboy.cavedroid.gameplay.controls.input.action.IGameInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.action.KeyboardInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.action.MouseInputAction

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
