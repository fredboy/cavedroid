package ru.fredboy.cavedroid.common.mvvm

import java.util.Stack

class NavBackStack(
    initial: NavKey,
) {
    private val stack = Stack<NavKey>().apply { push(initial) }

    private var host: NavStageHost? = null

    fun push(key: NavKey) {
        stack.push(key).also {
            host?.onStackChanged(key)
        }
    }

    fun pop() {
        if (stack.size == 1) {
            return
        }

        val popped = stack.pop()
        host?.onStackChanged(stack.peek(), popped)
    }

    fun reset() {
        while (stack.size > 1) {
            host?.clearViewModelFor(stack.pop())
        }

        host?.onStackChanged(stack.peek())
    }

    fun hasKey(key: NavKey): Boolean {
        return key in stack
    }

    internal fun attachHost(host: NavStageHost) {
        this.host = host
    }
}
