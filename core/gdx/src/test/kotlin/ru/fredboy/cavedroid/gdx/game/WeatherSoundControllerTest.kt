package ru.fredboy.cavedroid.gdx.game

import com.badlogic.gdx.audio.Sound
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.common.api.SoundPlayer
import ru.fredboy.cavedroid.domain.assets.model.RainSound
import ru.fredboy.cavedroid.domain.assets.model.ThunderTimeline
import ru.fredboy.cavedroid.domain.assets.repository.WeatherSoundAssetsRepository
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.game.world.WeatherLightning

class WeatherSoundControllerTest {
    private val world = mockk<GameWorld>(relaxed = true)
    private val mobs = mockk<MobController>(relaxed = true)
    private val assets = mockk<WeatherSoundAssetsRepository>()
    private val sound = mockk<Sound>(relaxed = true)
    private val player = mockk<SoundPlayer>(relaxed = true)
    private val settings = mockk<ApplicationContextRepository>()
    private val lightning = WeatherLightning()
    private val controller = WeatherSoundController(world, mobs, assets, player, settings, lightning)

    @BeforeEach
    fun setUp() {
        every { settings.isSoundEnabled() } returns true
        every { world.weatherIntensity } returns 1f
        every { world.biomeProximityFactor(any(), any(), any()) } returns 1f
        every { world.generatorConfig.seaLevel } returns 64
        every { mobs.player.middleMapY } returns 64
        every { assets.getRainSound() } returns RainSound(sound, ThunderTimeline(10.0, listOf(2.0)))
        every { player.playLoopSound(sound, any()) } returns 1L
    }

    @Test
    fun `flashes follow playback and repeat with the sound loop`() {
        controller.update(0f)
        controller.update(1f)
        assertEquals(0f, lightning.intensity)
        controller.update(1f)
        assertEquals(1f, lightning.intensity)
        controller.update(10f)
        assertEquals(1f, lightning.intensity)
        verify(exactly = 1) { player.playLoopSound(sound, any()) }
    }

    @Test
    fun `muted weather still flashes and unmuting resynchronizes to the restarted sound`() {
        every { settings.isSoundEnabled() } returns false
        controller.update(2f)
        assertEquals(1f, lightning.intensity)
        every { settings.isSoundEnabled() } returns true
        controller.update(0f)
        assertEquals(0f, lightning.intensity)
        controller.update(2f)
        assertEquals(1f, lightning.intensity)
    }

    @Test
    fun `leaving rain stops the sound and resets the flash`() {
        controller.update(0f)
        controller.update(2f)
        every { world.weatherIntensity } returns 0f
        controller.update(0.1f)
        assertEquals(0f, lightning.intensity)
        verify { player.stopLoopSound(sound, 1L) }
        every { world.weatherIntensity } returns 1f
        controller.update(1f)
        assertEquals(0f, lightning.intensity)
    }
}
