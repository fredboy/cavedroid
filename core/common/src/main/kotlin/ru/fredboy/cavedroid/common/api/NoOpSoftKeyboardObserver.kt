package ru.fredboy.cavedroid.common.api

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object NoOpSoftKeyboardObserver : SoftKeyboardObserver {
    override val isVisible: StateFlow<Boolean> = MutableStateFlow(false)
}
