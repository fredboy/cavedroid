package ru.fredboy.cavedroid.gdx.menu.v2.navigation

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.scene2d.Scene2dDsl

class NavRootStage(
    viewport: Viewport,
    private val navBackStack: NavBackStack,
    private val resolver: @Scene2dDsl Stage.(NavKey) -> Unit,
) : Stage(viewport) {

    init {
        navBackStack.attachNavRootStage(this)
    }

    fun onStackChanged(topKey: NavKey) {
        clear()
        resolver(topKey)
    }
}
