package ru.fredboy.cavedroid.common.mvvm

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.Viewport
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ktx.scene2d.Scene2dDsl

typealias RenderFun = (viewModel: ViewModel, renderFun: @Scene2dDsl suspend (ViewModel) -> Unit) -> ViewModel

class NavRootStage(
    viewport: Viewport,
    private val navBackStack: NavBackStack,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val resolver: Stage.(NavKey, ViewModel?, render: RenderFun) -> ViewModel,
) : Stage(viewport),
    NavStageHost {

    private val job = SupervisorJob()

    private val stageScope = CoroutineScope(dispatcher + job)

    val viewModels = mutableMapOf<NavKey, ViewModel>()

    private var lastTopKey: NavKey? = null

    init {
        navBackStack.attachHost(this)
    }

    override fun clearViewModelFor(navKey: NavKey) {
        viewModels.remove(navKey)?.dispose()
    }

    override fun onStackChanged(topKey: NavKey, poppedKey: NavKey?) {
        hideLastTopKey()

        if (poppedKey != null && !navBackStack.hasKey(poppedKey)) {
            viewModels.remove(poppedKey)?.dispose()
        }

        clear()

        stageScope.launch {
            val viewModel = resolver(topKey, viewModels[topKey], ::renderFun)
            viewModel.onShow()
            viewModels[topKey] = viewModel
        }

        lastTopKey = topKey
    }

    private fun hideLastTopKey() {
        val key = lastTopKey ?: return
        val viewModel = viewModels[key] ?: return

        viewModel.onHide()
    }

    private fun renderFun(viewModel: ViewModel, renderFun: @Scene2dDsl suspend (ViewModel) -> Unit): ViewModel {
        stageScope.launch { renderFun(viewModel) }
        return viewModel
    }

    override fun show() {
        lastTopKey?.let { key ->
            viewModels[key]?.onShow()
        }
    }

    override fun hide() {
        hideLastTopKey()
    }

    override fun dispose() {
        hideLastTopKey()
        super.dispose()
        job.cancel()
    }
}
