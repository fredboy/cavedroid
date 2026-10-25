package ru.fredboy.cavedroid.gameplay.controls.action.useitem

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.game.controller.explosion.ExplosionController
import ru.fredboy.cavedroid.game.controller.fire.FireController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.gameplay.controls.action.annotation.BindUseItemAction
import javax.inject.Inject

@GameScope
@BindUseItemAction(Item.FlintAndSteel.USE_ACTION_KEY)
class UseFlintAndSteelAction @Inject constructor(
    private val fireController: FireController,
    private val explosionController: ExplosionController,
    private val mobController: MobController,
) : IUseItemAction {

    override fun performBackground(item: Item.Usable, x: Int, y: Int): Boolean {
        val ignited = explosionController.prime(x, y, Layer.BACKGROUND) || fireController.addFire(x, y, Layer.BACKGROUND) != null
        if (ignited && !mobController.player.gameMode.isCreative()) {
            mobController.player.durateActiveDurable()
        }
        return ignited
    }

    override fun perform(item: Item.Usable, x: Int, y: Int): Boolean {
        val ignited = explosionController.prime(x, y) ||
            explosionController.prime(x, y, Layer.BACKGROUND) ||
            fireController.ignite(x, y) != null
        if (ignited && !mobController.player.gameMode.isCreative()) {
            mobController.player.durateActiveDurable()
        }
        return ignited
    }
}
