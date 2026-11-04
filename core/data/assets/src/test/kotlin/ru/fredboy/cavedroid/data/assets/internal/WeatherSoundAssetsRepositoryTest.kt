package ru.fredboy.cavedroid.data.assets.internal

import com.badlogic.gdx.Audio
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.files.FileHandle
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import ru.fredboy.cavedroid.domain.assets.pack.AssetResolver
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Path

class WeatherSoundAssetsRepositoryTest {
    @TempDir lateinit var temporary: Path
    private val previousAudio = Gdx.audio
    private val audio = mockk<Audio>()
    private val music = mockk<Music>(relaxed = true)
    private val repository = WeatherSoundAssetsRepositoryImpl()

    @AfterEach
    fun tearDown() {
        repository.dispose()
        Gdx.audio = previousAudio
    }

    @Test
    fun `weather streams the analyzed track and releases it with the assets`() {
        val root = FileHandle(temporary.toFile())
        root.child("sfx/weather/index.txt").writeString("rain\n", false)
        val file = root.child("sfx/weather/rain/0.wav")
        val wav = ByteBuffer.allocate(44 + 16000).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray())
            putInt(capacity() - 8)
            put("WAVEfmt ".toByteArray())
            putInt(16)
            putShort(1)
            putShort(1)
            putInt(8000)
            putInt(16000)
            putShort(2)
            putShort(16)
            put("data".toByteArray())
            putInt(16000)
        }.array()
        file.writeBytes(wav, false)
        repository.assetResolver = mockk<AssetResolver> {
            every { handle(any()) } answers { root.child(firstArg<String>()) }
            every { resolve(any()) } answers { FileHandle(firstArg<String>()) }
        }
        Gdx.audio = audio
        every { audio.newMusic(any()) } returns music

        repository.initialize()

        val rain = repository.getRainSound()!!
        assertSame(music, rain.music)
        assertEquals(1.0, rain.thunder!!.durationSeconds)
        verify(exactly = 1) { audio.newMusic(file) }
        verify(exactly = 0) { audio.newSound(any()) }
        repository.dispose()
        verify(exactly = 1) { music.dispose() }
    }
}
