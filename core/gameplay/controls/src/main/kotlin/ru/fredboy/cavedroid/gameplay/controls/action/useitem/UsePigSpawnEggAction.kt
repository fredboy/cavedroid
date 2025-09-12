package ru.fredboy.cavedroid.gameplay.controls.action.useitem

import com.badlogic.gdx.Gdx
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.entity.mob.abstraction.MobFactory
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.gameplay.controls.action.annotation.BindUseItemAction
import javax.inject.Inject

@GameScope
@BindUseItemAction(UsePigSpawnEggAction.ACTION_KEY)
class UsePigSpawnEggAction @Inject constructor(
    private val mobController: MobController,
    private val mobFactory: MobFactory,
) : IUseItemAction {

    override fun perform(item: Item.Usable, x: Int, y: Int): Boolean {
        val mobKey = item.mobKey ?: run {
            Gdx.app.error(ACTION_KEY, "No mob key")
            return false
        }

        mobFactory.create(mobController.player.cursorX, mobController.player.cursorY, mobKey)
        mobController.player.decreaseCurrentItemCount()

        return true
    }

    companion object {
        const val ACTION_KEY = "use_spawn_egg"
    }
}
