package ru.fredboy.cavedroid.domain.items.repository

import com.badlogic.gdx.utils.Disposable
import ru.fredboy.cavedroid.domain.items.model.mob.MobParams

interface MobParamsRepository : Disposable {

    fun initialize()

    fun getMobParamsByKey(key: String): MobParams?
}
