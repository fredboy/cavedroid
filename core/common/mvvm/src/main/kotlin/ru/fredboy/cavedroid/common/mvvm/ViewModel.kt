package ru.fredboy.cavedroid.common.mvvm

import com.badlogic.gdx.utils.Disposable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

abstract class ViewModel(dispatcher: CoroutineDispatcher) : Disposable {

    private val job = SupervisorJob()

    val viewModelScope = CoroutineScope(dispatcher + job)

    final override fun dispose() {
        job.cancel()
        onDispose()
    }

    open fun onShow() {
    }

    open fun onHide() {
    }

    open fun onDispose() {
    }
}
