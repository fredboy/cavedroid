package ru.fredboy.cavedroid.gameplay.rendering.renderer.world

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.cycledInsideWorld
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.game.controller.vehicle.VehicleController
import ru.fredboy.cavedroid.game.world.GameWorld
import javax.inject.Inject
import kotlin.math.sin

@GameScope
class BoatRenderer @Inject constructor(
    private val vehicles: VehicleController,
    private val world: GameWorld,
    private val textures: GetTextureRegionByNameUseCase,
) {
    private val side by lazy { Sprite(requireNotNull(textures["oak_boat_side"])) }
    private val rim by lazy { Sprite(requireNotNull(textures["oak_boat_rim"])) }
    private val oar by lazy { Sprite(requireNotNull(textures["oak_boat_oar"])) }

    private val blade by lazy { Sprite(requireNotNull(textures["oak_boat_blade"])) }

    fun draw(batch: SpriteBatch, viewport: Rectangle, front: Boolean) {
        vehicles.vehicles.forEach { boat ->
            val bounds = if (world.isInfinite) boat.hitbox else boat.hitbox.cycledInsideWorld(viewport, world.width.toFloat())
            bounds ?: return@forEach
            if (!bounds.overlaps(viewport)) return@forEach
            val hull = if (front) side else rim
            hull.setFlip(boat.facing < 0, hull.isFlipY)
            hull.setBounds(bounds.x, bounds.y + if (front) 0.08f else -0.06f, bounds.width, if (front) 0.42f else 0.14f)
            hull.setColor(if (front) 1f else 0.7f, if (front) 1f else 0.7f, if (front) 1f else 0.7f, 1f)
            hull.draw(batch)
            val stroke = 25f + sin(boat.oarPhase) * 35f
            drawOar(
                batch,
                bounds.x + bounds.width / 2f,
                bounds.y + if (front) 0.2f else -0.05f,
                if (boat.facing > 0) stroke else 180f - stroke,
                front,
            )
        }
    }

    private fun drawOar(batch: SpriteBatch, x: Float, y: Float, angle: Float, front: Boolean) {
        val shade = if (front) 1f else 0.65f
        oar.setBounds(x - 0.1f, y - 0.05f, 0.9f, 0.1f)
        oar.setOrigin(0.1f, 0.05f)
        oar.rotation = angle
        oar.setColor(shade, shade, shade, 1f)
        oar.draw(batch)
        blade.setBounds(x + 0.65f, y - 0.125f, 0.3f, 0.25f)
        blade.setOrigin(-0.65f, 0.125f)
        blade.rotation = angle
        blade.setColor(shade, shade, shade, 1f)
        blade.draw(batch)
    }
}
