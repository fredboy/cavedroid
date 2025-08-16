package ru.fredboy.cavedroid.gameplay.physics.action

import ru.fredboy.cavedroid.gameplay.physics.action.updateblock.IUpdateBlockAction
import ru.fredboy.cavedroid.gameplay.physics.action.updateblock.UpdateRequiresBlockAction

fun Map<String, IUpdateBlockAction>.getRequiresBlockAction(): IUpdateBlockAction {
    return requireNotNull(get(UpdateRequiresBlockAction.ACTION_KEY)) { "action requires_block not found" }
}
