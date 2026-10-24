package ru.fredboy.cavedroid.gameplay.controls.action.useitem

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.game.controller.vehicle.VehicleController
import ru.fredboy.cavedroid.gameplay.controls.action.annotation.BindUseItemAction
import javax.inject.Inject

@GameScope
@BindUseItemAction("use_boat")
class UseBoatItemAction @Inject constructor(private val vehicles: VehicleController) : IUseItemAction {
    override fun perform(item: Item.Usable, x: Int, y: Int): Boolean = vehicles.place(x + 0.5f, y + 0.75f)
}
