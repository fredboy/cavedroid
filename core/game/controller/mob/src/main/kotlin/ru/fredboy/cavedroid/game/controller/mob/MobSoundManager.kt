package ru.fredboy.cavedroid.game.controller.mob

import com.badlogic.gdx.utils.Disposable
import ru.fredboy.cavedroid.common.api.SoundPlayer
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.repository.StepsSoundAssetsRepository
import ru.fredboy.cavedroid.entity.mob.model.Mob
import javax.inject.Inject

@GameScope
class MobSoundManager @Inject constructor(
    private val stepsSoundAssetsRepository: StepsSoundAssetsRepository,
    private val soundPlayer: SoundPlayer,
) : Disposable {

    private var _mobController: MobController? = null

    private val mobController get() = requireNotNull(_mobController)

    fun attachToMobController(mobController: MobController) {
        _mobController = mobController
    }

    fun makeStepSound(mob: Mob) {
        val stepping = mob.retrieveStepping() ?: return
        val material = stepping.block.params.material?.name?.lowercase() ?: return
        val sound = stepsSoundAssetsRepository.getStepSound(material) ?: return

        soundPlayer.playSoundAtPosition(
            sound = sound,
            soundX = mob.position.x,
            soundY = mob.position.y,
            playerX = mobController.player.position.x,
            playerY = mobController.player.position.y,
        )
    }

    override fun dispose() {
        _mobController = null
    }
}
