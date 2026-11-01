package ru.fredboy.cavedroid.gameplay.rendering.utils

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.entity.vehicle.BoatTuning
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.vehicle.VehicleController
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
class PresentationPoses @Inject constructor(
    private val world: GameWorld,
    private val mobs: MobController,
    private val vehicles: VehicleController,
    private val gameWindowsManager: GameWindowsManager,
) {
    var snapshot = false
    private val scratch = Vector2()

    fun position(body: Body, out: Vector2): Vector2 = world.sampleRenderPosition(body, out, snapshot)

    fun heading(body: Body): Float = world.sampleRenderHeading(body, snapshot)

    fun playerPosition(out: Vector2): Vector2 {
        val boat = vehicles.mounted
        if (boat != null) return position(boat.body, out).add(0f, BoatTuning.SEAT_OFFSET_Y)
        return position(mobs.player.body, out)
    }

    fun aimPosition(out: Vector2, mouseWorldX: Float, mouseWorldY: Float): Vector2 {
        val player = mobs.player
        if (!player.holdAim) {
            if (gameWindowsManager.currentWindowType != GameWindowType.NONE) {
                return out.set(player.aimX, player.aimY)
            }

            playerPosition(out)
            // Gameplay aim is range-limited against the physics pose. Rebuild camera aim
            // from the mouse and the render pose so fixed physics steps cannot shake it.
            scratch.set(mouseWorldX - out.x, mouseWorldY - out.y).limit2(player.cursorRangeSquared)
            out.add(scratch)
            out.y = out.y.coerceIn(0f, world.height.toFloat())
            return out
        }
        playerPosition(out)
        return out.add(player.aimX - player.position.x, player.aimY - player.position.y)
    }

    fun cursorPosition(out: Vector2): Vector2 {
        val player = mobs.player
        if (!player.holdAim) return out.set(player.cursorX, player.cursorY)

        // Held cursors move with the player. Apply the same presentation offset as
        // the camera without changing the ray-cast cursor used for gameplay.
        playerPosition(out)
        return out.add(player.cursorX - player.position.x, player.cursorY - player.position.y)
    }

    fun bounds(body: Body, bounds: Rectangle): Rectangle {
        position(body, scratch)
        val physical = body.position
        bounds.x += scratch.x - physical.x
        bounds.y += scratch.y - physical.y
        return bounds
    }
}
