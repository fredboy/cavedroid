package ru.fredboy.cavedroid.gdx.game

import com.badlogic.gdx.Audio
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.files.FileHandle
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.data.assets.music.BackgroundMusicAssets
import ru.fredboy.cavedroid.domain.assets.pack.AssetResolver
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository

class BackgroundMusicControllerTest {
    private val previousAudio = Gdx.audio
    private val audio = mockk<Audio>()
    private val resolver = mockk<AssetResolver>()
    private val settings = mockk<ApplicationContextRepository>()
    private val loaded = mutableListOf<Music>()
    private val completions = mutableListOf<Music.OnCompletionListener>()
    private val assets = mockk<BackgroundMusicAssets>()
    private val controller = BackgroundMusicController(resolver, settings, assets)

    @BeforeEach
    fun setUp() {
        Gdx.audio = audio
        every { settings.isMusicEnabled() } returns true
        every { assets.trackDurations } returns mapOf("0.wav" to 60f, "1.wav" to 60f, "2.wav" to 60f)
        every { resolver.resolve(any()) } answers { FileHandle(firstArg<String>()) }
        every { audio.newMusic(any()) } answers {
            mockk<Music>(relaxed = true).also { music ->
                every { music.setOnCompletionListener(any()) } answers {
                    firstArg<Music.OnCompletionListener?>()?.let(completions::add)
                }
                loaded.add(music)
            }
        }
    }

    @AfterEach
    fun tearDown() {
        controller.dispose()
        Gdx.audio = previousAudio
    }

    @Test
    fun `completion disposes the previous track and starts the next track silently`() {
        controller.resume()
        val first = loaded.single()
        verify { first.volume = 0f }
        verify { first.isLooping = false }
        verify(exactly = 1) { first.play() }
        completions.single().onCompletion(first)
        controller.update()
        verify(exactly = 1) { first.dispose() }
        verify(exactly = 1) { loaded.last().play() }
        verify { loaded.last().volume = 0f }
        verify(exactly = 2) { audio.newMusic(any()) }
    }

    @Test
    fun `music fades in over three seconds and fades out during the last three seconds`() {
        controller.resume()
        val music = loaded.single()
        every { music.position } returns 1.5f
        controller.update()
        verify { music.volume = 0.3f }
        every { music.position } returns 3f
        controller.update()
        verify { music.volume = 0.6f }
        every { music.position } returns 30f
        controller.update()
        verify(exactly = 2) { music.volume = 0.6f }
        every { music.position } returns 58.5f
        controller.update()
        verify(exactly = 2) { music.volume = 0.3f }
        every { music.position } returns 60f
        controller.update()
        verify(exactly = 3) { music.volume = 0f }
        verify(exactly = 1) { audio.newMusic(any()) }
    }

    @Test
    fun `pause preserves fade progress until playback resumes`() {
        controller.resume()
        val music = loaded.single()
        every { music.position } returns 1.5f
        controller.update()
        controller.pause()
        repeat(10) { controller.update() }
        verify(exactly = 1) { music.volume = 0.3f }
        controller.resume()
        verify(exactly = 2) { music.volume = 0.3f }
        verify(exactly = 1) { audio.newMusic(any()) }
    }

    @Test
    fun `pausing and disabling music preserve the current track`() {
        every { settings.isMusicEnabled() } returns false
        controller.resume()
        verify(exactly = 0) { audio.newMusic(any()) }
        every { settings.isMusicEnabled() } returns true
        controller.update()
        val music = loaded.single()
        controller.pause()
        controller.update()
        verify(exactly = 1) { music.pause() }
        controller.resume()
        every { settings.isMusicEnabled() } returns false
        controller.update()
        verify(exactly = 2) { music.pause() }
        every { settings.isMusicEnabled() } returns true
        controller.update()
        verify(exactly = 3) { music.play() }
        verify(exactly = 1) { audio.newMusic(any()) }
    }

    @Test
    fun `late completion after disposal cannot restart playback`() {
        controller.resume()
        val music = loaded.single()
        val completion = completions.single()
        controller.dispose()
        completion.onCompletion(music)
        controller.resume()
        controller.update()
        verify(exactly = 1) { music.dispose() }
        verify(exactly = 1) { audio.newMusic(any()) }
    }

    @Test
    fun `an empty playlist never loads music`() {
        every { assets.trackDurations } returns emptyMap()
        controller.resume()
        repeat(10) { controller.update() }
        verify(exactly = 0) { audio.newMusic(any()) }
    }

    @Test
    fun `decoder failures skip each broken track once and stop retrying when all fail`() {
        every { audio.newMusic(any()) } throws IllegalArgumentException("Broken audio")
        controller.resume()
        repeat(10) { controller.update() }
        controller.pause()
        controller.resume()
        verify(exactly = 3) { audio.newMusic(any()) }
    }

    @Test
    fun `a broken track does not stop healthy tracks or get retried in later cycles`() {
        every { assets.trackDurations } returns mapOf("bad.wav" to 60f, "good.wav" to 60f)
        every { audio.newMusic(match { it.name() == "bad.wav" }) } throws IllegalArgumentException("Broken audio")
        controller.resume()
        repeat(5) {
            completions.last().onCompletion(loaded.last())
            controller.update()
        }
        verify(exactly = 1) { audio.newMusic(match { it.name() == "bad.wav" }) }
        verify(exactly = 6) { audio.newMusic(match { it.name() == "good.wav" }) }
    }

    @Test
    fun `play failures release the track without crashing or repeatedly loading it`() {
        every { assets.trackDurations } returns mapOf("bad.wav" to 60f)
        val broken = mockk<Music>(relaxed = true)
        every { broken.play() } throws IllegalStateException("Cannot play")
        every { audio.newMusic(any()) } returns broken
        controller.resume()
        repeat(10) { controller.update() }
        verify(exactly = 1) { audio.newMusic(any()) }
        verify(exactly = 1) { broken.dispose() }
    }

    @Test
    fun `disposal failure does not prevent the next track from starting`() {
        controller.resume()
        val music = loaded.single()
        every { music.dispose() } throws IllegalStateException("Cannot dispose")
        completions.single().onCompletion(music)
        controller.update()
        verify(exactly = 2) { audio.newMusic(any()) }
        verify(exactly = 1) { loaded.last().play() }
    }
}
