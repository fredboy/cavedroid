package ru.fredboy.cavedroid.gameplay.controls.action.useitem

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.common.model.GameMode
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.explosion.ExplosionController
import ru.fredboy.cavedroid.game.controller.explosion.ExplosionRules
import ru.fredboy.cavedroid.game.controller.fire.FireController
import ru.fredboy.cavedroid.game.controller.mob.MobController

class UseFlintAndSteelActionTest {
    private val fire = mockk<FireController>(relaxed = true)
    private val explosions = mockk<ExplosionController>(relaxed = true)
    private val player = mockk<Player>(relaxed = true)
    private val mobs = mockk<MobController> { every { this@mockk.player } returns this@UseFlintAndSteelActionTest.player }
    private val item = mockk<Item.FlintAndSteel>()
    private val action = UseFlintAndSteelAction(fire, explosions, mobs)

    @Test
    fun `successful TNT ignition wears flint and steel once in survival`() {
        every { player.gameMode } returns GameMode.SURVIVAL
        every { explosions.prime(1, 2, Layer.FOREGROUND, ExplosionRules.FUSE_SECONDS) } returns true
        assertTrue(action.perform(item, 1, 2))
        verify(exactly = 1) { player.durateActiveDurable() }
        verify(exactly = 0) { fire.ignite(any(), any()) }
    }

    @Test
    fun `creative ignition does not consume durability`() {
        every { player.gameMode } returns GameMode.CREATIVE
        every { explosions.prime(1, 2, Layer.FOREGROUND, ExplosionRules.FUSE_SECONDS) } returns true
        assertTrue(action.perform(item, 1, 2))
        verify(exactly = 0) { player.durateActiveDurable() }
    }

    @Test
    fun `failed ignition does not consume durability`() {
        every { fire.ignite(any(), any()) } returns null
        assertFalse(action.perform(item, 1, 2))
        verify(exactly = 0) { player.durateActiveDurable() }
    }

    @Test
    fun `background use primes only the background TNT`() {
        every { player.gameMode } returns GameMode.SURVIVAL
        every { explosions.prime(1, 2, Layer.BACKGROUND, ExplosionRules.FUSE_SECONDS) } returns true
        assertTrue(action.performBackground(item, 1, 2))
        verify(exactly = 0) { explosions.prime(1, 2, Layer.FOREGROUND, any()) }
        verify(exactly = 1) { player.durateActiveDurable() }
    }
}
