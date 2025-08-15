package ru.fredboy.cavedroid.game.controller.drop.impl.physics

import com.badlogic.gdx.physics.box2d.Contact
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.abstraction.AbstractContactHandler
import ru.fredboy.cavedroid.domain.world.model.ContactSensorType
import ru.fredboy.cavedroid.entity.drop.model.Drop
import javax.inject.Inject
import kotlin.reflect.KClass

@GameScope
@BindDropContactHandler
class DropOnGroundSensorToBlockContactHandler @Inject constructor() : AbstractContactHandler<Drop, Block>() {

    override val sensorType: ContactSensorType?
        get() = ContactSensorType.DROP_ON_GROUND

    override val entityClassA: KClass<Drop>
        get() = Drop::class

    override val entityClassB: KClass<Block>
        get() = Block::class

    override fun Drop.handleBeginContact(contact: Contact, entityB: Block) {
        isBobbing = true
    }

    override fun Drop.handleEndContact(contact: Contact, entityB: Block) {
        isBobbing = false
    }
}
