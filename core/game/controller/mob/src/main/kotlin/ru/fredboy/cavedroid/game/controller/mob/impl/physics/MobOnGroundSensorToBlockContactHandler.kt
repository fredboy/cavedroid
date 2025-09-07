package ru.fredboy.cavedroid.game.controller.mob.impl.physics

import com.badlogic.gdx.physics.box2d.Contact
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.abstraction.AbstractContactHandler
import ru.fredboy.cavedroid.domain.world.model.ContactSensorType
import ru.fredboy.cavedroid.entity.mob.model.Mob
import javax.inject.Inject
import kotlin.reflect.KClass

@GameScope
@BindMobContactHandler
class MobOnGroundSensorToBlockContactHandler @Inject constructor() : AbstractContactHandler<Mob, Block>() {

    override val sensorType: ContactSensorType?
        get() = ContactSensorType.MOB_ON_GROUND

    override val entityClassA: KClass<Mob>
        get() = Mob::class

    override val entityClassB: KClass<Block>
        get() = Block::class

    override fun Mob.handleBeginContact(contact: Contact, entityB: Block) {
        footContactCounter++
        isFlyMode = false
        descend = false
        controlVector.y = 0f
    }

    override fun Mob.handleEndContact(contact: Contact, entityB: Block) {
        footContactCounter--
    }
}
