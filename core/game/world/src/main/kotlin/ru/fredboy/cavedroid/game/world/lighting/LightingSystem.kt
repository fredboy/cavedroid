package ru.fredboy.cavedroid.game.world.lighting

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.utils.Disposable
import ru.fredboy.cavedroid.domain.world.lighting.LightHandle
import ru.fredboy.cavedroid.entity.mob.model.Mob
import ru.fredboy.cavedroid.game.world.GameWorld

interface LightingSystem : Disposable {
    val chunkSize: Int
    fun attachToGameWorld(gameWorld: GameWorld)
    fun update(delta: Float)
    fun recalculate()
    fun render(camera: OrthographicCamera)
    fun isMobExposedToSun(mob: Mob): Boolean
    fun createPlayerSightLight(body: Body, x: Float, y: Float): LightHandle
    fun createFurnaceLight(x: Float, y: Float): LightHandle
    fun createFireLight(x: Float, y: Float): LightHandle
    fun refreshChunks(chunks: Iterable<Pair<Int, Int>>)
    fun getEffectiveBrightness(x: Int, y: Int, sunBrightness: Float): Float
}
