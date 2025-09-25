package ru.fredboy.cavedroid.gdx.utils

import com.badlogic.gdx.ApplicationListener
import ru.fredboy.cavedroid.gdx.CaveDroidApplicationDecorator
import ru.fredboy.cavedroid.gdx.di.ApplicationComponent

val ApplicationListener.applicationComponent: ApplicationComponent
    get() = (this as CaveDroidApplicationDecorator).applicationComponent
