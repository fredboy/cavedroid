package ru.fredboy.cavedroid.html.spike

import co.touchlab.kermit.Logger
import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.ai.btree.BehaviorTree
import com.badlogic.gdx.ai.btree.LeafTask
import com.badlogic.gdx.ai.btree.Task
import com.badlogic.gdx.ai.btree.branch.Sequence
import com.badlogic.gdx.utils.reflect.ClassReflection

object EcsAiSpike {

    private val logger = Logger.withTag("EcsAiSpike")

    fun run() {
        logger.i { "SPIKE start (Ashley + gdx-ai on TeaVM)" }
        ashleyCheck()
        reflectionCheck()
        behaviorTreeCheck()
        logger.i { "SPIKE done" }
    }

    private fun ashleyCheck() {
        try {
            val engine = Engine()
            engine.addSystem(MovementSystem())
            val entity = Entity().apply {
                add(PositionComponent())
                add(VelocityComponent().apply { x = 10f })
            }
            engine.addEntity(entity)
            engine.update(1f)

            val x = ComponentMapper.getFor(PositionComponent::class.java).get(entity).x
            if (x == 10f) {
                logger.i { "SPIKE [ashley] OK — system moved entity to x=$x" }
            } else {
                logger.w { "SPIKE [ashley] RAN but unexpected x=$x (expected 10)" }
            }
        } catch (t: Throwable) {
            logger.e(t) { "SPIKE [ashley] FAILED — ${t::class.simpleName}: ${t.message}" }
        }
    }

    private fun reflectionCheck() {
        try {
            val task = ClassReflection.newInstance(BarkTask::class.java)
            logger.i { "SPIKE [reflection] OK — newInstance produced ${task::class.simpleName}" }
        } catch (t: Throwable) {
            logger.e(t) { "SPIKE [reflection] FAILED — ${t::class.simpleName}: ${t.message}" }
        }
    }

    private fun behaviorTreeCheck() {
        try {
            val blackboard = Blackboard()
            val tree = BehaviorTree(Sequence(BarkTask(), BarkTask()), blackboard)
            tree.step()
            if (blackboard.barks == 2) {
                logger.i { "SPIKE [gdx-ai] OK — tree stepped, barks=${blackboard.barks}" }
            } else {
                logger.w { "SPIKE [gdx-ai] RAN but barks=${blackboard.barks} (expected 2)" }
            }
        } catch (t: Throwable) {
            logger.e(t) { "SPIKE [gdx-ai] FAILED — ${t::class.simpleName}: ${t.message}" }
        }
    }
}

private class PositionComponent : Component {
    var x = 0f
    var y = 0f
}

private class VelocityComponent : Component {
    var x = 0f
    var y = 0f
}

private class MovementSystem :
    IteratingSystem(Family.all(PositionComponent::class.java, VelocityComponent::class.java).get()) {

    private val position = ComponentMapper.getFor(PositionComponent::class.java)
    private val velocity = ComponentMapper.getFor(VelocityComponent::class.java)

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val p = position.get(entity)
        val v = velocity.get(entity)
        p.x += v.x * deltaTime
        p.y += v.y * deltaTime
    }
}

class Blackboard {
    var barks = 0
}

class BarkTask : LeafTask<Blackboard>() {
    override fun execute(): Status {
        `object`.barks++
        return Status.SUCCEEDED
    }

    override fun copyTo(task: Task<Blackboard>): Task<Blackboard> = task
}
