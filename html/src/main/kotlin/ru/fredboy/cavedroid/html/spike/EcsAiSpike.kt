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

/**
 * S0 spike (issue #131 / #147) — proves whether **Ashley ECS** and **gdx-ai**
 * survive TeaVM in the browser. Both rely on libGDX reflection
 * ([ClassReflection]), which TeaVM only supports with explicit metadata, so the
 * point of this spike is to find out what works and what needs reflection
 * config BEFORE we commit to the ECS-first rewrite (ADR 0002 / 0009).
 *
 * Throwaway: called once from [ru.fredboy.cavedroid.html.WebLauncher]. Each
 * check is isolated in try/catch and only logs — it never blocks the app.
 * Run `./gradlew html:runWeb` and read the browser console for `SPIKE` lines.
 *
 * If `:html` fails to COMPILE after adding the ashley/gdx-ai deps, that itself
 * is a gate result (a class in the reachable graph is TeaVM-incompatible).
 */
object EcsAiSpike {

    private val logger = Logger.withTag("EcsAiSpike")

    fun run() {
        logger.i { "SPIKE start (Ashley + gdx-ai on TeaVM)" }
        ashleyCheck()
        reflectionCheck()
        behaviorTreeCheck()
        logger.i { "SPIKE done" }
    }

    /** Ashley: Engine + ComponentMapper + Family + IteratingSystem. */
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

    /**
     * The actual TeaVM gate: libGDX [ClassReflection.newInstance] is how both
     * Ashley (component lookups) and gdx-ai's behavior-tree parser instantiate
     * types by class. If this throws on web, we need reflection metadata.
     */
    private fun reflectionCheck() {
        try {
            val task = ClassReflection.newInstance(BarkTask::class.java)
            logger.i { "SPIKE [reflection] OK — newInstance produced ${task::class.simpleName}" }
        } catch (t: Throwable) {
            logger.e(t) { "SPIKE [reflection] FAILED — ${t::class.simpleName}: ${t.message}" }
        }
    }

    /** gdx-ai: build a BehaviorTree programmatically and step it. */
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

/** Blackboard for the gdx-ai behavior tree. */
class Blackboard {
    var barks = 0
}

/**
 * Top-level (reflection-friendly) leaf task with a public no-arg constructor —
 * exactly the shape gdx-ai's `.tree` parser instantiates via reflection.
 */
class BarkTask : LeafTask<Blackboard>() {
    override fun execute(): Status {
        `object`.barks++
        return Status.SUCCEEDED
    }

    override fun copyTo(task: Task<Blackboard>): Task<Blackboard> = task
}
