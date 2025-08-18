package ru.fredboy.cavedroid.data.items.repository

import com.badlogic.gdx.Gdx
import kotlinx.serialization.json.Json
import ru.fredboy.cavedroid.data.items.mapper.MobParamsMapper
import ru.fredboy.cavedroid.data.items.model.MobParamsDto
import ru.fredboy.cavedroid.domain.items.model.mob.MobParams
import ru.fredboy.cavedroid.domain.items.repository.MobParamsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MobParamsRepositoryImpl @Inject constructor(
    private val mobParamsMapper: MobParamsMapper,
) : MobParamsRepository {

    private val mobParamsMap = LinkedHashMap<String, MobParams>()

    private var _initialized: Boolean = false

    init {
        initialize()
    }

    override fun initialize() {
        if (_initialized) {
            Gdx.app.debug(TAG, "Attempted to init when already initialized")
            return
        }

        val jsonString = Gdx.files.internal("json/mobs.json").readString()
        val mobParams = JsonFormat.decodeFromString<Map<String, MobParamsDto>>(jsonString)

        mobParams.forEach { (key, mobParamsDto) ->
            val params = mobParamsMapper.map(key, mobParamsDto)
            mobParamsMap[key] = params
        }

        _initialized = true
    }

    override fun getMobParamsByKey(key: String): MobParams? {
        return mobParamsMap[key] ?: run {
            Gdx.app.error(TAG, "No such mob: '$key'")
            null
        }
    }

    override fun dispose() {
        mobParamsMap.clear()
        _initialized = false
    }

    companion object {
        private val TAG = MobParamsRepositoryImpl::class.java.simpleName

        private val JsonFormat = Json { ignoreUnknownKeys = true }
    }
}
