package ru.fredboy.cavedroid

import android.app.Activity
import ru.fredboy.cavedroid.common.api.AdController
import ru.fredboy.cavedroid.common.api.NoOpAdController

fun createAdController(activity: Activity): AdController = NoOpAdController()
