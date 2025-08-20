package ru.fredboy.cavedroid.gdx.menu.v2.navigation

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.Viewport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ktx.scene2d.Scene2dDsl
import ru.fredboy.cavedroid.common.coroutines.GdxMainDispatcher

class NavRootStage(
    viewport: Viewport,
    private val navBackStack: NavBackStack,
    private val resolver: @Scene2dDsl suspend Stage.(NavKey, ViewModel?) -> ViewModel,
) : Stage(viewport) {

    private val job = SupervisorJob()

    private val stageScope = CoroutineScope(GdxMainDispatcher + job)

    val viewModels = mutableMapOf<NavKey, ViewModel>()

    init {
        navBackStack.attachNavRootStage(this)
    }

    fun clearViewModelFor(navKey: NavKey) {
        viewModels.remove(navKey)?.dispose()
    }

    fun onStackChanged(topKey: NavKey, poppedKey: NavKey? = null) {
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
