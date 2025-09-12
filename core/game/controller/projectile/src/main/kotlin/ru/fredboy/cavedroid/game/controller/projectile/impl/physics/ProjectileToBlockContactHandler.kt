package ru.fredboy.cavedroid.game.controller.projectile.impl.physics

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.Contact
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.abstraction.AbstractContactHandler
import ru.fredboy.cavedroid.domain.world.model.ContactSensorType
import ru.fredboy.cavedroid.entity.mob.abstraction.MobFactory
import ru.fredboy.cavedroid.entity.projectile.model.Projectile
import javax.inject.Inject
import kotlin.reflect.KClass

@GameScope
@BindProjectileContactHandler
class ProjectileToBlockContactHandler @Inject constructor(
    private val mobFactory: MobFactory,
) : AbstractContactHandler<Projectile, Block>() {

    override val sensorType: ContactSensorType? get() = null

    override val entityClassA: KClass<Projectile>
        get() = Projectile::class

    override val entityClassB: KClass<Block>
        get() = Block::class

    override fun Projectile.handleBeginContact(contact: Contact, entityB: Block) {
        if (isOnGround) {
            return
        }

        isOnGround = true

        if (item.params.key == "egg" && MathUtils.randomBoolean(0.125f)) {
            val spawnPoint = velocity.get().cpy().nor().scl(-1f)
            mobFactory.create(
                x = position.x + spawnPoint.x,
                y = position.y + spawnPoint.y,
                mobKey = "chicken",
            )
        }
    }

    override fun Projectile.handleEndContact(contact: Contact, entityB: Block) = Unit
}
