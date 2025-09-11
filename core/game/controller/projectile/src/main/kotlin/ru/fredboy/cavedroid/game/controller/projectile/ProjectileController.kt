package ru.fredboy.cavedroid.game.controller.projectile

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Disposable
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.entity.drop.DropQueue
import ru.fredboy.cavedroid.entity.mob.abstraction.ProjectileAdapter
import ru.fredboy.cavedroid.entity.projectile.abstraction.ProjectileWorldAdapter
import ru.fredboy.cavedroid.entity.projectile.model.Projectile
import java.util.*
import javax.inject.Inject

@GameScope
class ProjectileController @Inject constructor(
    private val getItemByKeyUseCase: GetItemByKeyUseCase,
    private val projectileWorldAdapter: ProjectileWorldAdapter,
    private val dropQueue: DropQueue,
) : ProjectileAdapter,
    Disposable {

    constructor(
        initialProjectiles: Collection<Projectile>,
        getItemByKeyUseCase: GetItemByKeyUseCase,
        projectileWorldAdapter: ProjectileWorldAdapter,
        dropQueue: DropQueue,
    ) : this(getItemByKeyUseCase, projectileWorldAdapter, dropQueue) {
        _projectiles.addAll(initialProjectiles)
    }

    private val _projectiles = LinkedList<Projectile>()

    val projectiles: List<Projectile> get() = _projectiles

    override fun addProjectile(
        itemKey: String,
        damage: Int,
        dropOnGround: Boolean,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        velocity: Vector2,
    ) {
        addProjectile(
            projectile = Projectile(
                item = getItemByKeyUseCase[itemKey],
                damage = damage,
                dropOnGround = dropOnGround,
                width = width,
                height = height,
            ),
            x = x,
            y = y,
            velocity = velocity,
        )
    }

    fun addProjectile(projectile: Projectile, x: Float, y: Float, velocity: Vector2) {
        if (projectile.item.isNone()) {
            return
        }

        projectile.spawn(x, y, velocity, projectileWorldAdapter.getBox2dWorld())

        _projectiles.add(projectile)
    }

    fun update(delta: Float) {
        val iterator = _projectiles.iterator()

        while (iterator.hasNext()) {
            val projectile = iterator.next()

            if (projectile.isDead) {
                projectile.getDropItem()?.let { item ->
                    dropQueue.offerItem(projectile.position.x, projectile.position.y, item)
                }
                projectile.dispose()
                iterator.remove()
            } else {
                projectile.update(projectileWorldAdapter, delta)
            }
        }
    }

    override fun dispose() {
        _projectiles.forEach { it.dispose() }
        _projectiles.clear()
    }
}
