package ru.deadsoftware.cavedroid.game.input.handler.keyboard

import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.input.IKeyboardInputHandler
import ru.deadsoftware.cavedroid.game.input.action.KeyboardInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.KeyboardInputActionKey
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.misc.annotations.multibinding.BindKeyboardInputHandler
import javax.inject.Inject

@GameScope
@BindKeyboardInputHandler
class SelectHotbarSlotKeyboardInputHandler @Inject constructor(
    private val mobsController: MobsController,
) : IKeyboardInputHandler {

    override fun checkConditions(action: KeyboardInputAction): Boolean {
        return action.actionKey is KeyboardInputActionKey.SelectHotbarSlot &&
                action.isKeyDown
    }

    override fun handle(action: KeyboardInputAction) {
        mobsController.player.inventory.activeSlot = (action.actionKey as KeyboardInputActionKey.SelectHotbarSlot).slot
    }

}