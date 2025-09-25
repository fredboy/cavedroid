package ru.fredboy.cavedroid.gdx

import com.badlogic.gdx.ApplicationListener

interface CaveDroidApplicationDecorator : ApplicationListener {

    val applicationComponent get() = getDelegate().applicationComponent

    fun getDelegate(): CaveDroidApplication
}
