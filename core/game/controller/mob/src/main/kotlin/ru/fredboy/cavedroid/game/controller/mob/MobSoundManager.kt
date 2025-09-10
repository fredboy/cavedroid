package ru.fredboy.cavedroid.game.controller.mob

import com.badlogic.gdx.utils.Disposable
import ru.fredboy.cavedroid.common.api.SoundPlayer
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.repository.MobSoundAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.StepsSoundAssetsRepository
import ru.fredboy.cavedroid.entity.mob.model.Mob
import javax.inject.Inject

@GameScope
class MobSoundManager @Inject constructor(
    private val stepsSoundAssetsRepository: StepsSoundAssetsRepository,
    private val mobSoundAssetsRepository: MobSoundAssetsRepository,
    private val soundPlayer: SoundPlayer,
) : Disposable {

    private var _mobController: MobController? = null

    private val mobController get() = requireNotNull(_mobController)

    fun attachToMobController(mobController: MobController) {
        _mobController = mobController
    }

    fun makeSound(mob: Mob) {
        val soundType = mob.retrieveSound() ?: return

        val sound = when (soundType) {
            is Mob.SoundType.Stepping -> soundType.block.params.material?.name?.lowercase()
                ?.let { material -> stepsSoundAssetsRepository.getStepSound(material) }

            is Mob.SoundType.Idle -> mobSoundAssetsRepository.getIdleSound(mob.params.key)

            is Mob.SoundType.Hit -> mobSoundAssetsRepository.getHitSound(mob.params.key)

            is Mob.SoundType.Death -> mobSoundAssetsRepository.getDeathSound(mob.params.key)
        } ?: return

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
