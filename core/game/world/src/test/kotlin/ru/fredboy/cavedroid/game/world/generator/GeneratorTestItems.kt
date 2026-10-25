package ru.fredboy.cavedroid.game.world.generator

import io.mockk.mockk
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.block.BlockInsets
import ru.fredboy.cavedroid.domain.items.model.block.CommonBlockParams
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository

/** Real block data without textures; avoids recording millions of mocked terrain reads. */
internal class GeneratorTestItems : ItemsRepository by mockk<ItemsRepository>() {
    private val blocks = mutableMapOf<String, Block>()
    override val fallbackBlock = Block.None(params("none"))

    override fun getBlockByKey(key: String): Block = blocks.getOrPut(key) {
        val params = params(key)
        when {
            key == "none" -> fallbackBlock
            key == "water" -> Block.Water(params, state = 0, density = 1f)
            key == "lava" -> Block.Lava(params, state = 0, density = 1f)
            key.startsWith("chest_") -> Block.Chest(params, key.removePrefix("chest_"))
            key == "spawner" -> Block.Spawner(params, "spider")
            key.startsWith("spawner_") -> Block.Spawner(params, key.removePrefix("spawner_"))
            else -> Block.Normal(params)
        }
    }

    private fun params(key: String) = CommonBlockParams(
        key = key, collisionMargins = BlockInsets.Meters(0f, 0f, 0f, 0f),
        hitPoints = 1, dropInfo = emptyList(), dropsSingleItem = true,
        hasCollision = key !in NON_SOLID && !key.startsWith("chest_"),
        isBackground = key.startsWith("chest_"), isTransparent = false,
        requiresBlock = false, allowAttachToNeighbour = false, animationInfo = null,
        texture = null, spriteMargins = BlockInsets.Pixels(0, 0, 0, 0), toolLevel = 0,
        toolType = null, damage = 0, tint = null, isFallable = false, castsShadows = true,
        lightInfo = null, replaceable = false, material = null, actionSoundKey = null,
        combustible = false, requiresBackground = false,
    )

    companion object {
        private val NON_SOLID = setOf(
            "none", "water", "lava", "web", "snow", "sugar_cane", "torch", "ladder",
            "dandelion", "rose", "tallgrass", "deadbush", "mushroom_brown", "mushroom_red",
        )
    }
}
