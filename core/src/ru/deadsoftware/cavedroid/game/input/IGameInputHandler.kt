package ru.deadsoftware.cavedroid.game.input

import ru.deadsoftware.cavedroid.game.input.action.IGameInputAction

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