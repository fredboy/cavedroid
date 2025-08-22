package ru.fredboy.cavedroid.gdx.menu.v2.navigation

import com.badlogic.gdx.utils.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

abstract class ViewModel : Disposable {

    private val job = SupervisorJob()

    private val dispatcher = Dispatchers.Default

    val viewModelScope = CoroutineScope(dispatcher + job)

    final override fun dispose() {
        job.cancel()
        onDispose()
    }

    open fun onDispose() {
    }
}
