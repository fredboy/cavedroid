package ru.fredboy.cavedroid.gameplay.controls.action.useitem

import com.badlogic.gdx.math.Vector2
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.entity.projectile.model.Projectile
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.projectile.ProjectileController
import ru.fredboy.cavedroid.gameplay.controls.action.annotation.BindUseItemAction
import javax.inject.Inject

@GameScope
@BindUseItemAction(UseSnowBallAction.ACTION_KEY)
class UseSnowBallAction @Inject constructor(
    private val projectileController: ProjectileController,
    private val mobController: MobController,
) : IUseItemAction {

    override fun perform(item: Item.Usable, x: Int, y: Int): Boolean {
        projectileController.addProjectile(
            projectile = Projectile(
                item = item,
                damage = 1,
                width = 0.5f,
                height = 0.5f,
                dropOnGround = true,
            ),
            x = mobController.player.position.x + mobController.player.direction.basis,
            y = mobController.player.position.y - mobController.player.height / 4f,
            velocity = Vector2(
                x.toFloat() + 0.5f - mobController.player.position.x,
                y.toFloat() + 0.5f - mobController.player.position.y,
            ).nor().scl(200f),
        )

        mobController.player.decreaseCurrentItemCount()

        return true
    }

    companion object {
        const val ACTION_KEY = "use_snow_ball_action"
    }
}
