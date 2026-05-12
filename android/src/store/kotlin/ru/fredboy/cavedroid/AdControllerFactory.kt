package ru.fredboy.cavedroid

import android.app.Activity
import ru.fredboy.cavedroid.common.api.AdController

fun createAdController(activity: Activity): AdController = YandexAdController(activity)
