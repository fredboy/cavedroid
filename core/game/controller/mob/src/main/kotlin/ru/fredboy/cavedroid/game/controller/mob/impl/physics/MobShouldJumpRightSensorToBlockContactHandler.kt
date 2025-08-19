package ru.fredboy.cavedroid.game.controller.mob.impl.physics

import com.badlogic.gdx.physics.box2d.Contact
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.abstraction.AbstractContactHandler
import ru.fredboy.cavedroid.domain.world.model.ContactSensorType
import ru.fredboy.cavedroid.entity.mob.model.Direction
import ru.fredboy.cavedroid.entity.mob.model.Mob
import ru.fredboy.cavedroid.entity.mob.model.Player
import javax.inject.Inject
import kotlin.reflect.KClass

@GameScope
@BindMobContactHandler
class MobShouldJumpRightSensorToBlockContactHandler @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
) : AbstractContactHandler<Mob, Block>() {

    override val sensorType: ContactSensorType
        get() = ContactSensorType.MOB_SHOULD_JUMP_RIGHT

    override val entityClassA: KClass<Mob>
        get() = Mob::class

    override val entityClassB: KClass<Block>
        get() = Block::class

    override fun Mob.handleBeginContact(contact: Contact, entityB: Block) {
        if (this is Player && !applicationContextRepository.isAutoJumpEnabled()) {
            return
        }

        autojumpCounters[Direction.RIGHT.index]++
    }

    override fun Mob.handleEndContact(contact: Contact, entityB: Block) {
        if (this is Player && !applicationContextRepository.isAutoJumpEnabled()) {
            return
        }

        autojumpCounters[Direction.RIGHT.index]--
    }
}
