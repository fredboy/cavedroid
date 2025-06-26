package ru.fredboy.cavedroid.ux.physics.action

import ru.fredboy.cavedroid.ux.physics.action.updateblock.IUpdateBlockAction
import ru.fredboy.cavedroid.ux.physics.action.updateblock.UpdateRequiresBlockAction

fun Map<String, IUpdateBlockAction>.getRequiresBlockAction(): IUpdateBlockAction {
    return requireNotNull(get(UpdateRequiresBlockAction.ACTION_KEY)) { "action requires_block not found" }
}
