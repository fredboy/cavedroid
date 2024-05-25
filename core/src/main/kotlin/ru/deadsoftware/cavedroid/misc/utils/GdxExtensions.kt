package ru.deadsoftware.cavedroid.misc.utils

import com.badlogic.gdx.Graphics

val Graphics.ratio get() = width.toFloat() / height.toFloat()