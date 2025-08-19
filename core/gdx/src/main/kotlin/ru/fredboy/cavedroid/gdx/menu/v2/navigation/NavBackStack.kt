package ru.fredboy.cavedroid.gdx.menu.v2.navigation

import java.util.Stack

class NavBackStack(
    initial: NavKey,
) {
    private val stack = Stack<NavKey>().apply { push(initial) }

    private var _navRootStage: NavRootStage? = null

    fun push(key: NavKey) {
        stack.push(key).also {
            _navRootStage?.onStackChanged(key)
        }
    }

    fun pop() {
        if (stack.size == 1) {
            return
        }

        stack.pop()
        _navRootStage?.onStackChanged(stack.peek())
    }

    fun attachNavRootStage(navRootStage: NavRootStage) {
        _navRootStage = navRootStage
        navRootStage.onStackChanged(stack.peek())
    }
}
