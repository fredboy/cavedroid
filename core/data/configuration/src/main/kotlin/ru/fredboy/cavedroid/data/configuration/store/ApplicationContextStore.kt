package ru.fredboy.cavedroid.data.configuration.store

import ru.fredboy.cavedroid.data.configuration.model.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplicationContextStore @Inject constructor(
    private val applicationContext: ApplicationContext,
) {

    private val lock = Any()

    val isDebug: Boolean
        get() = synchronized(lock) { applicationContext.isDebug }

    var isTouch: Boolean
        get() = synchronized(lock) { applicationContext.isTouch }
        set(value) = synchronized(lock) { applicationContext.isTouch = value }

    var gameDirectory: String
        get() = synchronized(lock) { applicationContext.gameDirectory }
        set(value) = synchronized(lock) { applicationContext.gameDirectory = value }

    var width: Float
        get() = synchronized(lock) { applicationContext.width }
        set(value) = synchronized(lock) { applicationContext.width = value }

    var height: Float
        get() = synchronized(lock) { applicationContext.height }
        set(value) = synchronized(lock) { applicationContext.height = value }

}