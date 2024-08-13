package ru.fredboy.cavedroid.common.utils

import com.badlogic.gdx.Graphics

val Graphics.ratio get() = width.toFloat() / height.toFloat()
