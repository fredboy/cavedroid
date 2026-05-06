package ru.fredboy.cavedroid.common.mvvm

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.Viewport
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ktx.scene2d.Scene2dDsl

class NavRootStage(
    viewport: Viewport,
    private val navBackStack: NavBackStack,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val resolver: @Scene2dDsl suspend Stage.(NavKey, ViewModel?) -> ViewModel,
) : Stage(viewport),
    NavStageHost {

    private val job = SupervisorJob()

    private val stageScope = CoroutineScope(dispatcher + job)

    val viewModels = mutableMapOf<NavKey, ViewModel>()

    init {
        navBackStack.attachHost(this)
    }

    override fun clearViewModelFor(navKey: NavKey) {
        viewModels.remove(navKey)?.dispose()
    }

    override fun onStackChanged(topKey: NavKey, poppedKey: NavKey?) {
        if (poppedKey != null && !navBackStack.hasKey(poppedKey)) {
            viewModels.remove(poppedKey)?.dispose()
        }

        clear()

        stageScope.launch {
            viewModels[topKey] = resolver(topKey, viewModels[topKey])
        }
    }

    override fun dispose() {
        super.dispose()
        job.cancel()
    }
}
