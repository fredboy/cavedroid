package ru.fredboy.cavedroid.data.assets.internal

import ru.fredboy.cavedroid.domain.assets.model.MobSprite
import ru.fredboy.cavedroid.domain.assets.repository.MobAssetsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class MobAssetsRepositoryImpl @Inject constructor(): MobAssetsRepository() {

    private var playerSprite: MobSprite.Player? = null

    private var pigSprite: MobSprite.Pig? = null

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

    override fun getPlayerSprites(): MobSprite.Player {
        return requireNotNull(playerSprite)
    }

    override fun getPigSprites(): MobSprite.Pig {
        return requireNotNull(pigSprite)
    }

    override fun initialize() {
        loadPlayerSprite()
        loadPigSprite()
    }

    override fun dispose() {
        super.dispose()
        playerSprite = null
        pigSprite = null
    }

    companion object {
        private const val PLAYER_SPRITES_PATH = "textures/mobs/char"
        private const val PIG_SPRITES_PATH = "textures/mobs/pig"
    }

}