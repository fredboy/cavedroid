package ru.fredboy.cavedroid.domain.assets.repository

import com.badlogic.gdx.audio.Sound

abstract class MobSoundAssetsRepository : SoundAssetsRepository() {

    abstract fun getIdleSound(mobKey: String): Sound?

    abstract fun getHitSound(mobKey: String): Sound?

    abstract fun getDeathSound(mobKey: String): Sound?
}
