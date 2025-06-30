package ru.fredboy.cavedroid.data.configuration.model

class ApplicationContext(
    internal val isDebug: Boolean,
    internal var isTouch: Boolean,
    internal var gameDirectory: String,
    internal var width: Float,
    internal var height: Float,
)
