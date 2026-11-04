package ru.fredboy.cavedroid.gdx.game

import com.badlogic.gdx.audio.Music
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
    private val music = mockk<Music>(relaxed = true)
    private val settings = mockk<ApplicationContextRepository>()
    private val lightning = WeatherLightning()
    private val controller = WeatherSoundController(world, mobs, assets, settings, lightning)

    @BeforeEach
    fun setUp() {
        every { settings.isSoundEnabled() } returns true
        every { world.weatherIntensity } returns 1f
        every { world.biomeProximityFactor(any(), any(), any()) } returns 1f
        every { world.generatorConfig.seaLevel } returns 64
        every { mobs.player.middleMapY } returns 64
        every { assets.getRainSound() } returns RainSound(music, ThunderTimeline(10.0, listOf(2.0)))
    }

    @Test
    fun `flashes follow decoder position rather than simulation time and repeat with the loop`() {
        controller.update(0f)
        every { music.position } returns 1f
        controller.update(2f)
        assertEquals(0f, lightning.intensity)
        every { music.position } returns 2f
        controller.update(0.01f)
        assertEquals(1f, lightning.intensity)
        every { music.position } returns 9f
        controller.update(0.01f)
        every { music.position } returns 2f
        controller.update(0.01f)
        assertEquals(1f, lightning.intensity)
        verify(exactly = 1) { music.play() }
        verify { music.isLooping = true }
        verify { music.volume = 0.5f }
    }

    @Test
    fun `muted weather still flashes and unmuting follows the restarted stream`() {
        every { settings.isSoundEnabled() } returns false
        controller.update(2f)
        assertEquals(1f, lightning.intensity)
        every { settings.isSoundEnabled() } returns true
        controller.update(0f)
        assertEquals(0f, lightning.intensity)
        every { music.position } returns 2f
        controller.update(0.01f)
        assertEquals(1f, lightning.intensity)
        every { settings.isSoundEnabled() } returns false
        controller.update(0f)
        verify { music.stop() }
    }

    @Test
    fun `leaving rain stops the stream and resets the flash`() {
        controller.update(0f)
        every { music.position } returns 2f
        controller.update(2f)
        every { world.weatherIntensity } returns 0f
        controller.update(0.1f)
        assertEquals(0f, lightning.intensity)
        verify { music.stop() }
        every { music.position } returns 0f
        every { world.weatherIntensity } returns 1f
        controller.update(1f)
        assertEquals(0f, lightning.intensity)
        verify(exactly = 2) { music.play() }
    }

    @Test
    fun `pause preserves stream position and resumes on the next weather update`() {
        controller.update(0f)
        every { music.position } returns 1f
        controller.update(1f)
        controller.pause()
        verify { music.pause() }
        controller.update(0f)
        assertEquals(0f, lightning.intensity)
        every { music.position } returns 2f
        controller.update(0.01f)
        assertEquals(1f, lightning.intensity)
        verify(exactly = 2) { music.play() }
        verify(exactly = 0) { music.stop() }
    }

    @Test
    fun `a late thunder flash fades across the stream loop boundary`() {
        every { assets.getRainSound() } returns RainSound(music, ThunderTimeline(10.0, listOf(9.9)))
        controller.update(0f)
        assertEquals(0f, lightning.intensity)
        every { music.position } returns 9.9f
        controller.update(0.01f)
        every { music.position } returns 0.01f
        controller.update(0.01f)
        assertEquals(0.15f, lightning.intensity)
    }
}
