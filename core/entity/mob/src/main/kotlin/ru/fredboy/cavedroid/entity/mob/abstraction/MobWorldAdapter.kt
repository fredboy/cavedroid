package ru.fredboy.cavedroid.entity.mob.abstraction

import com.badlogic.gdx.math.Vector2
import ru.fredboy.cavedroid.domain.items.model.block.Block

interface MobWorldAdapter {

    val height: Int

    val width: Int

    fun setForegroundBlock(x: Int, y: Int, block: Block)

    fun setBackgroundBlock(x: Int, y: Int, block: Block)

    fun getForegroundBlock(x: Int, y: Int): Block

    fun getBackgroundBlock(x: Int, y: Int): Block

    fun destroyForegroundBlock(x: Int, y: Int, shouldDrop: Boolean)

    fun destroyBackgroundBlock(x: Int, y: Int, shouldDrop: Boolean)

    fun findSpawnPoint(): Vector2

}