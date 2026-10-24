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
import ru.fredboy.cavedroid.entity.mob.model.VillagerMob

@OptIn(ExperimentalSerializationApi::class)
class VillagerSaveTest {
    private val params = MobParams("Villager", "villager", 0.625f, 1.625f, 2.5f, MobBehaviorType.VILLAGER, emptyList(), 20, emptyList(), 15f, 0, false, 20, 0.5f, 0.08f)
    private val repository = mockk<MobParamsRepository> { every { getMobParamsByKey("villager") } returns params }
    private val mapper = WalkingMobMapper(DirectionMapper(), repository, mockk())
    private val body = mockk<Body>(relaxed = true) {
        every { position } returns Vector2(-10f, 100f)
        every { linearVelocity } returns Vector2()
    }
    private val physics = mockk<MobPhysicsFactory> { every { createBody(any(), any(), any(), any(), any()) } returns body }

    @Test
    fun `home sleeping pose and doors awaiting closure survive protobuf and mapper round trip`() {
        val villager = VillagerMob(params)
        villager.spawn(-10f, 100f, physics)
        villager.home = VillagerMob.BlockPosition(-13, 100)
        villager.isSleeping = true
        villager.openedDoors.add(VillagerMob.BlockPosition(-16, 100))
        val encoded = ProtoBuf.encodeToByteArray(mapper.mapSaveData(villager))
        val restored = mapper.mapPassiveMob(ProtoBuf.decodeFromByteArray<SaveDataDto.WalkingMobSaveDataDto>(encoded), physics) as VillagerMob
        assertEquals(villager.home, restored.home)
        assertTrue(restored.isSleeping)
        assertEquals(villager.openedDoors, restored.openedDoors)
    }

    @Test
    fun `old villagers without home fields remain valid`() {
        val villager = VillagerMob(params)
        villager.spawn(-10f, 100f, physics)
        val encoded = ProtoBuf.encodeToByteArray(mapper.mapSaveData(villager))
        val restored = mapper.mapPassiveMob(ProtoBuf.decodeFromByteArray<SaveDataDto.WalkingMobSaveDataDto>(encoded), physics) as VillagerMob
        assertEquals(null, restored.home)
        assertFalse(restored.isSleeping)
        assertTrue(restored.openedDoors.isEmpty())
    }
}
