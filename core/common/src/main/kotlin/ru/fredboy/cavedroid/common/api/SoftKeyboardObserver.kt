package ru.fredboy.cavedroid.common.api

import kotlinx.coroutines.flow.StateFlow

interface SoftKeyboardObserver {

    val isVisible: StateFlow<Boolean>
}
