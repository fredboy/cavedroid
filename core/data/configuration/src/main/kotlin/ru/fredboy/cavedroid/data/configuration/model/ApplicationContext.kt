package ru.fredboy.cavedroid.data.configuration.model

class ApplicationContext(
    internal val isDebug: Boolean,
    internal var isTouch: Boolean,
    internal var isFullscreen: Boolean,
    internal var useDynamicCamera: Boolean,
    internal var gameDirectory: String,
    internal var width: Float,
    internal var height: Float,
    internal var screenScale: Int,
    internal var isAutoJumpEnabled: Boolean,
)
