package ru.fredboy.cavedroid.gdx.base

import com.badlogic.gdx.Screen
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository

abstract class BaseScreen(
    private val applicationContextRepository: ApplicationContextRepository,
) : Screen {

    override fun resize(width: Int, height: Int) {
        applicationContextRepository.setWidth(width.toFloat() / applicationContextRepository.getScreenScale())
        applicationContextRepository.setHeight(height.toFloat() / applicationContextRepository.getScreenScale())
    }
}
