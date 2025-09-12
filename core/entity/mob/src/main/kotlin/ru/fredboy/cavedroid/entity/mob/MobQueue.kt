package ru.fredboy.cavedroid.entity.mob

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.entity.mob.model.Mob
import java.util.*
import javax.inject.Inject

@GameScope
class MobQueue @Inject constructor() {

    val queue: Queue<QueuedMob> = LinkedList()

    fun offerMob(x: Float, y: Float, mob: Mob) {
        queue.offer(QueuedMob(x, y, mob))
    }

    data class QueuedMob(
        val x: Float,
        val y: Float,
        val mob: Mob,
    )
}
