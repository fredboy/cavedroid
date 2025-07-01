package ru.fredboy.cavedroid.ux.controls.input.handler.keyboard

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.ux.controls.input.IKeyboardInputHandler
import ru.fredboy.cavedroid.ux.controls.input.action.KeyboardInputAction
import ru.fredboy.cavedroid.ux.controls.input.action.keys.KeyboardInputActionKey
import ru.fredboy.cavedroid.ux.controls.input.annotation.BindKeyboardInputHandler
import javax.inject.Inject

@GameScope
@BindKeyboardInputHandler
class SelectHotbarSlotKeyboardInputHandler @Inject constructor(
    private val mobController: MobController,
) : IKeyboardInputHandler {

    override fun checkConditions(action: KeyboardInputAction): Boolean = action.actionKey is KeyboardInputActionKey.SelectHotbarSlot &&
        action.isKeyDown

    override fun handle(action: KeyboardInputAction) {
        mobController.player.activeSlot = (action.actionKey as KeyboardInputActionKey.SelectHotbarSlot).slot
    }
}
