package ru.fredboy.cavedroid.data.assets.internal

import com.badlogic.gdx.graphics.g2d.Sprite
import ru.fredboy.cavedroid.domain.assets.model.MobSprite
import ru.fredboy.cavedroid.domain.assets.repository.MobAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.TextureRegionsAssetsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class MobAssetsRepositoryImpl @Inject constructor(
    private val textureRegionsAssetsRepository: TextureRegionsAssetsRepository,
) : MobAssetsRepository() {

    private var playerSprite: MobSprite.Player? = null

    private var pigSprite: MobSprite.Pig? = null

    private var cowSprite: MobSprite.Cow? = null

    private var playerCursorSprite: Sprite? = null

    private fun loadPlayerSprite() {
        val (headTexture, bodyTexture, handTexture, legTexture) = List(4) { index ->
            loadTexture("$PLAYER_SPRITES_PATH/$index.png")
        }

        playerSprite = MobSprite.Player(
            head = flippedSprite(headTexture),
            hand = flippedSprite(handTexture),
            body = flippedSprite(bodyTexture),
            leg = flippedSprite(legTexture),
        )
    }

    private fun loadPigSprite() {
        val (bodyTexture, legTexture) = List(2) { index ->
            loadTexture("$PIG_SPRITES_PATH/$index.png")
        }

        pigSprite = MobSprite.Pig(
            headAndBody = flippedSprite(bodyTexture),
            leg = flippedSprite(legTexture),
        )
    }

    private fun loadCowSprites() {
        val (bodyTexture, legTexture) = List(2) { index ->
            loadTexture("$COW_SPRITES_PATH/$index.png")
        }

        cowSprite = MobSprite.Cow(
            headAndBody = flippedSprite(bodyTexture),
            leg = flippedSprite(legTexture),
        )
    }

    override fun getPlayerSprites(): MobSprite.Player = requireNotNull(playerSprite)

    override fun getPigSprites(): MobSprite.Pig = requireNotNull(pigSprite)

    override fun getCowSprites(): MobSprite.Cow = requireNotNull(cowSprite)

    override fun getPlayerCursorSprite(): Sprite {
        return requireNotNull(playerCursorSprite)
    }

    override fun initialize() {
        loadPlayerSprite()
        loadPigSprite()
        loadCowSprites()
        playerCursorSprite = textureRegionsAssetsRepository.getTextureRegionByName(CURSOR_KEY)?.let {
            Sprite(it).apply {
                setSize(1f, 1f)
            }
        }
    }

    override fun dispose() {
        super.dispose()
        playerSprite = null
        pigSprite = null
        playerCursorSprite = null
    }

    companion object {
        private const val PLAYER_SPRITES_PATH = "textures/mobs/char"
        private const val PIG_SPRITES_PATH = "textures/mobs/pig"
        private const val COW_SPRITES_PATH = "textures/mobs/cow"
        private const val CURSOR_KEY = "cursor"
    }
}
