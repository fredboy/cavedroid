package ru.fredboy.cavedroid.gdx.game

import co.touchlab.kermit.Logger
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.utils.Disposable
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.data.assets.music.BackgroundMusicAssets
import ru.fredboy.cavedroid.domain.assets.pack.AssetResolver
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import javax.inject.Inject

@GameScope
class BackgroundMusicController @Inject constructor(
    private val assetResolver: AssetResolver,
    private val applicationContextRepository: ApplicationContextRepository,
    private val backgroundMusicAssets: BackgroundMusicAssets,
) : Disposable {
    private val trackDurations get() = backgroundMusicAssets.trackDurations
    private val queue by lazy { ShuffledMusicQueue(trackDurations.keys.toList()) }
    private val failedTracks = mutableSetOf<String>()
    private var currentTrack: String? = null
    private var music: Music? = null
    private var trackDuration = 0f
    private var active = false
    private var playing = false
    private var completed = false
    private var disposed = false

    fun update() {
        if (disposed) return
        if (!active || !applicationContextRepository.isMusicEnabled()) {
            pausePlayback()
            return
        }
        if (completed) releaseMusic()
        if (music == null) loadNextTrack()
        val current = music ?: return
        try {
            if (!playing) {
                current.play()
                playing = true
            }
            val position = current.position
            val fadeIn = position / FADE_DURATION_SEC
            val fadeOut = (trackDuration - position) / FADE_DURATION_SEC
            current.volume = VOLUME * minOf(fadeIn, fadeOut).coerceIn(0f, 1f)
        } catch (exception: Exception) {
            failCurrentTrack(exception)
        }
    }

    private fun loadNextTrack() {
        // Failed tracks stay excluded for this session. Stop when none remain,
        // rather than retrying a broken playlist every frame.
        while (failedTracks.size < trackDurations.size) {
            val track = queue.next() ?: return
            if (track in failedTracks) continue
            currentTrack = track
            trackDuration = trackDurations.getValue(track)
            try {
                val next = Gdx.audio.newMusic(assetResolver.resolve("bgm/$track"))
                music = next
                next.volume = 0f
                next.isLooping = false
                next.setOnCompletionListener { finished ->
                    if (music === finished) completed = true
                }
                return
            } catch (exception: Exception) {
                failCurrentTrack(exception)
            }
        }
    }

    private fun failCurrentTrack(exception: Exception) {
        currentTrack?.let {
            failedTracks.add(it)
            queue.exclude(it)
        }
        logger.w(exception) { "Skipping failed BGM track: $currentTrack" }
        releaseMusic()
    }

    fun resume() {
        active = true
        update()
    }

    fun pause() {
        active = false
        pausePlayback()
    }

    private fun pausePlayback() {
        try {
            if (playing) music?.pause()
        } catch (exception: Exception) {
            failCurrentTrack(exception)
        }
        playing = false
    }

    private fun releaseMusic() {
        val previous = music
        music = null
        currentTrack = null
        playing = false
        completed = false
        if (previous != null) {
            runCatching { previous.setOnCompletionListener(null) }
                .onFailure { logger.w(it) { "Cannot detach BGM completion listener" } }
            runCatching { previous.dispose() }
                .onFailure { logger.w(it) { "Cannot dispose BGM track" } }
        }
    }

    override fun dispose() {
        disposed = true
        active = false
        releaseMusic()
    }

    companion object {
        private val logger = Logger.withTag("BackgroundMusicController")
        private const val VOLUME = 0.1f
        private const val FADE_DURATION_SEC = 3f
    }
}
