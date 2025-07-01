package ru.fredboy.cavedroid.zygote.base

import com.badlogic.gdx.Screen
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository

abstract class BaseScreen(
    private val applicationContextRepository: ApplicationContextRepository,
) : Screen {

    override fun resize(width: Int, height: Int) {
        applicationContextRepository.setWidth(width.toFloat() * SCALE)
        applicationContextRepository.setHeight(height.toFloat() * SCALE)
    }

    companion object {
        private const val SCALE = .5f
    }
}