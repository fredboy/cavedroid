package ru.fredboy.cavedroid.gdx

import ru.fredboy.cavedroid.common.api.ApplicationController

interface CaveDroidApplicationDecorator : ApplicationController {

    val applicationComponent get() = getDelegate().applicationComponent

    fun getDelegate(): CaveDroidApplication
}
