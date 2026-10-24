package ru.fredboy.cavedroid.data.save.mapper

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.domain.items.model.mob.MobBehaviorType
import ru.fredboy.cavedroid.domain.items.model.mob.MobParams
import ru.fredboy.cavedroid.domain.items.repository.MobParamsRepository
import ru.fredboy.cavedroid.entity.mob.abstraction.MobPhysicsFactory
import ru.fredboy.cavedroid.entity.mob.model.CreeperMob

@OptIn(ExperimentalSerializationApi::class)
class CreeperSaveTest {
    private val params = MobParams("Creeper", "creeper", 0.625f, 1.625f, 2.5f, MobBehaviorType.CREEPER, emptyList(), 20, emptyList(), 15f, 0, false, 20, 0.5f, 0.08f)
    private val repository = mockk<MobParamsRepository> { every { getMobParamsByKey("creeper") } returns params }
    private val mapper = WalkingMobMapper(DirectionMapper(), repository, mockk())
    private val body = mockk<Body>(relaxed = true) {
        every { position } returns Vector2(-10f, 100f)
        every { linearVelocity } returns Vector2()
    }
    private val physics = mockk<MobPhysicsFactory> { every { createBody(any(), any(), any(), any(), any()) } returns body }

    @Test
    fun `creeper type fuse explicit ignition and daylight timer survive a save round trip`() {
        val creeper = CreeperMob(params)
        creeper.spawn(-10f, 100f, physics)
        creeper.fuse.elapsedSec = 0.75f
        creeper.fuse.forced = true
        creeper.daylightSec = 6f
        val encoded = ProtoBuf.encodeToByteArray(mapper.mapSaveData(creeper))
        val restored = mapper.mapPassiveMob(ProtoBuf.decodeFromByteArray<SaveDataDto.WalkingMobSaveDataDto>(encoded), physics)
        assertTrue(restored is CreeperMob)
        restored as CreeperMob
        assertEquals(0.75f, restored.fuse.elapsedSec)
        assertTrue(restored.fuse.forced)
        assertEquals(6f, restored.daylightSec)
    }

    @Test
    fun `omitted optional creeper fields load with inactive defaults`() {
        val creeper = CreeperMob(params)
        creeper.spawn(-10f, 100f, physics)
        val encoded = ProtoBuf.encodeToByteArray(mapper.mapSaveData(creeper))
        val restored = mapper.mapPassiveMob(ProtoBuf.decodeFromByteArray<SaveDataDto.WalkingMobSaveDataDto>(encoded), physics) as CreeperMob
        assertFalse(restored.fuse.isPrimed)
        assertEquals(0f, restored.daylightSec)
    }
}
