package ru.fredboy.cavedroid.data.assets.internal

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import ru.fredboy.cavedroid.domain.assets.repository.EnvironmentTextureRegionsRepositoryTexture
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class EnvironmentTextureRegionsRepositoryImplTexture @Inject constructor() : EnvironmentTextureRegionsRepositoryTexture() {

    private val texturesCache = mutableMapOf<String, Texture>()

    private var sunSprite: Sprite? = null

    private var moonPhases: Array<Sprite>? = null

    private var rainTexture: Texture? = null

    private var snowTexture: Texture? = null

    override fun getSunSprite(): Sprite {
        return requireNotNull(sunSprite)
    }

    override fun getMoonPhaseSprite(phase: Int): Sprite {
        require(phase in 0..MOON_PHASES)
        return requireNotNull(moonPhases)[phase]
    }

    override fun getRainTexture(): Texture = requireNotNull(rainTexture)

    override fun getSnowTexture(): Texture = requireNotNull(snowTexture)

    override fun getMoonPhasesCount(): Int {
        return MOON_PHASES
    }

    private fun loadScrollingTexture(name: String): Texture {
        return resolveTexture(name, "textures/environment", texturesCache).apply {
            setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)
        }
    }

    override fun initialize() {
        sunSprite = flippedSprite(resolveTexture("sun", "textures/environment", texturesCache))

        val moonTexture = resolveTexture("moon_phases", "textures/environment", texturesCache)
        moonPhases = Array(MOON_PHASES) { i ->
            flippedSprite(
                texture = moonTexture,
                x = (i % 4) * 96,
                y = i / 4 * 96,
                width = 96,
                height = 96,
            )
        }

        rainTexture = loadScrollingTexture("rain")
        snowTexture = loadScrollingTexture("snow")
    }

    override fun dispose() {
        super.dispose()
        texturesCache.clear()
        sunSprite = null
        moonPhases = null
        rainTexture = null
        snowTexture = null
    }

    companion object {
        private const val MOON_PHASES = 8
    }
}
