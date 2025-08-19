package ru.fredboy.cavedroid.game.world.impl

import box2dLight.RayHandler
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.items.repository.MobParamsRepository
import ru.fredboy.cavedroid.domain.world.listener.OnBlockDestroyedListener
import ru.fredboy.cavedroid.domain.world.listener.OnBlockPlacedListener
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerWorldAdapter
import ru.fredboy.cavedroid.entity.container.model.ContainerCoordinates
import ru.fredboy.cavedroid.entity.drop.abstraction.DropWorldAdapter
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import ru.fredboy.cavedroid.game.world.GameWorld
import javax.inject.Inject
import kotlin.reflect.KClass

@GameScope
internal class WorldAdapterImpl @Inject constructor(
    private val gameWorld: GameWorld,
    private val itemsRepository: ItemsRepository,
    private val mobParamsRepository: MobParamsRepository,
) : DropWorldAdapter,
    ContainerWorldAdapter,
    MobWorldAdapter {

    override val height: Int
        get() = gameWorld.height

    override val width: Int
        get() = gameWorld.width

    override fun setForegroundBlock(x: Int, y: Int, block: Block) {
        gameWorld.setForeMap(x, y, block)
    }

    override fun setBackgroundBlock(x: Int, y: Int, block: Block) {
        gameWorld.setBackMap(x, y, block)
    }

    override fun getForegroundBlock(x: Int, y: Int): Block = gameWorld.getForeMap(x, y)

    override fun getBackgroundBlock(x: Int, y: Int): Block = gameWorld.getBackMap(x, y)

    override fun destroyForegroundBlock(x: Int, y: Int, shouldDrop: Boolean) {
        gameWorld.destroyForeMap(x, y, shouldDrop)
    }

    override fun destroyBackgroundBlock(x: Int, y: Int, shouldDrop: Boolean) {
        gameWorld.destroyBackMap(x, y, shouldDrop)
    }

    override fun findSpawnPoint(): Vector2 {
        var x = width / 2
        var y = 0

        while (y++ in 0..gameWorld.generatorConfig.seaLevel) {
            if (y == gameWorld.generatorConfig.seaLevel) {
                while (x++ in 0..<width) {
                    if (getForegroundBlock(x, y).params.hasCollision) {
                        break
                    } else if (x == width - 1) {
                        setBackgroundBlock(x, y, itemsRepository.getBlockByKey("grass"))
                        break
                    }
                }
                break
            }
            if (getForegroundBlock(width / 2, y).params.hasCollision) {
                break
            }
        }

        val charParams = mobParamsRepository.getMobParamsByKey("char")
        return Vector2(x + .5f - (charParams?.width ?: 1f) / 2, y - (charParams?.height ?: 1f))
    }

    override fun checkContainerAtCoordinates(
        coordinates: ContainerCoordinates,
        requiredType: KClass<out Block.Container>,
    ): Boolean {
        val block = when (coordinates.z) {
            Layer.FOREGROUND.z -> gameWorld.getForeMap(coordinates.x, coordinates.y)
            Layer.BACKGROUND.z -> gameWorld.getBackMap(coordinates.x, coordinates.y)
            else -> itemsRepository.fallbackBlock
        }

        return block.isContainer() && block::class == requiredType
    }

    override fun addOnBlockDestroyedListener(listener: OnBlockDestroyedListener) {
        gameWorld.addBlockDestroyedListener(listener)
    }

    override fun addOnBlockPlacedListener(listener: OnBlockPlacedListener) {
        gameWorld.addBlockPlacedListener(listener)
    }

    override fun removeOnBlockDestroyedListener(listener: OnBlockDestroyedListener) {
        gameWorld.removeBlockDestroyedListener(listener)
    }

    override fun removeOnBlockPlacedListener(listener: OnBlockPlacedListener) {
        gameWorld.removeBlockPlacedListener(listener)
    }

    override fun getBox2dWorld(): World {
        return gameWorld.world
    }

    override fun getMediumLiquid(hitbox: Rectangle): Block.Fluid? {
        val (startX, endX) = hitbox.x.toInt() to (hitbox.x + hitbox.width).toInt()
        val (startY, endY) = hitbox.y.toInt() to (hitbox.y + hitbox.height).toInt()

        var medium: Block.Fluid? = null
        for (x in startX..endX) {
            for (y in startY..endY) {
                val block = (getForegroundBlock(x, y) as? Block.Fluid)
                    ?.takeIf { it.getRectangle(x, y).overlaps(hitbox) }
                    ?: continue

                if (medium == null || medium.density < block.density) {
                    medium = block
                }
            }
        }
        return medium
    }

    override fun getRayHandler(): RayHandler {
        return gameWorld.rayHandler
    }
}
