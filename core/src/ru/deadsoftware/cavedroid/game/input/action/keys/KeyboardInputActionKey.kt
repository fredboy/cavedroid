package ru.deadsoftware.cavedroid.game.input.action.keys

sealed interface KeyboardInputActionKey {

    data object Left : KeyboardInputActionKey
    data object Right : KeyboardInputActionKey
    data object Down : KeyboardInputActionKey

    data object Jump : KeyboardInputActionKey

    data object Crouch : KeyboardInputActionKey

    data object SwitchControlsMode : KeyboardInputActionKey

    data object OpenInventory : KeyboardInputActionKey

    data object Pause : KeyboardInputActionKey

    data object ShowDebug : KeyboardInputActionKey
    data object SpawnPig : KeyboardInputActionKey
    data object SwitchGameMode : KeyboardInputActionKey
    data object ShowMap : KeyboardInputActionKey
}