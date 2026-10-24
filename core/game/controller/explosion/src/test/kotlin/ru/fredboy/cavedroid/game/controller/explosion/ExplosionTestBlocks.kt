package ru.fredboy.cavedroid.game.controller.explosion

import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.block.BlockInsets
import ru.fredboy.cavedroid.domain.items.model.block.CommonBlockParams

/** Real block data avoids recording thousands of ray samples in mockk. */
internal object ExplosionTestBlocks {
    fun params(key: String, resistance: Float = 0f, hp: Int = 1, top: Float = 0f) = CommonBlockParams(
        key = key, collisionMargins = BlockInsets.Meters(0f, top, 0f, 0f),
        hitPoints = hp, dropInfo = emptyList(), dropsSingleItem = true,
        hasCollision = key !in setOf("none", "water", "lava"),
        isBackground = false, isTransparent = false,
        requiresBlock = false, allowAttachToNeighbour = false, animationInfo = null,
        texture = null, spriteMargins = BlockInsets.Pixels(0, 0, 0, 0), toolLevel = 0,
        toolType = null, damage = 0, tint = null, isFallable = false, castsShadows = true,
        lightInfo = null, replaceable = false, material = null, actionSoundKey = null,
        combustible = key == "tnt", requiresBackground = false, blastResistance = resistance,
    )

    val air = Block.None(params("none"))
    val tnt = Block.Normal(params("tnt"))
    val dirt = Block.Normal(params("dirt", 0.5f))
    val stone = Block.Normal(params("stone", 6f))
    val obsidian = Block.Normal(params("obsidian", 1200f))
    val bedrock = Block.Normal(params("bedrock", 3600000f, hp = -1))
    val water = Block.Water(params("water", 100f, hp = -1), 0, 1f)
}
