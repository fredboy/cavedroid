package ru.fredboy.cavedroid.domain.world.model

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.Contact
import ru.fredboy.cavedroid.domain.items.model.block.Block

data class ChunkUserData(
    val boundX: IntRange,
    val boundY: IntRange,
    val blocks: Map<Pair<Int, Int>, Block>,
) {

    operator fun get(x: Int, y: Int): Block {
        require(x in boundX) { "x outside of chunk bounds" }
        require(y in boundY) { "y outside of chunk bounds" }

        return requireNotNull(blocks[x to y])
    }

    fun getContactedBlock(contact: Contact): Block? {
        val (x, y) = contact.worldManifold.points.first().let { vec ->
            MathUtils.round(vec.x) to MathUtils.round(vec.y)
        }

        if (x !in boundX || y !in boundY) {
            return null
        }

        require(contact.fixtureA.body.userData === this || contact.fixtureB.body.userData === this)

        return requireNotNull(blocks[x to y])
    }
}
