package ru.fredboy.cavedroid.gdx.base

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import ru.fredboy.cavedroid.common.utils.DEFAULT_VIEWPORT_WIDTH
import ru.fredboy.cavedroid.common.utils.MIN_VIEWPORT_HEIGHT
import ru.fredboy.cavedroid.common.utils.ratio
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import kotlin.math.max

abstract class BaseScreen(
    private val applicationContextRepository: ApplicationContextRepository,
) : Screen {

    protected open val scaleFactor = 1f

    override fun resize(width: Int, height: Int) {
        val scaledWidth = DEFAULT_VIEWPORT_WIDTH * scaleFactor
        applicationContextRepository.setWidth(scaledWidth)
        applicationContextRepository.setHeight(max(MIN_VIEWPORT_HEIGHT * scaleFactor, scaledWidth / Gdx.graphics.ratio))
    }
}
