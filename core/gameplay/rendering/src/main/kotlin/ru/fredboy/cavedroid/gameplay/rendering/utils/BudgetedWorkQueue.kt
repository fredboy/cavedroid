package ru.fredboy.cavedroid.gameplay.rendering.utils

/** Main-thread cooperative work. A work item must yield at its own bounded checkpoints. */
class BudgetedWorkQueue(private val clock: () -> Long = System::nanoTime) {
    interface Work {
        val priority: Int
        fun step(deadline: Long): Boolean
    }

    private val pending = linkedSetOf<Work>()
    private var active: Work? = null

    fun add(work: Work) {
        pending.add(work)
    }
    fun remove(work: Work) {
        pending.remove(work)
        if (active === work) active = null
    }
    fun clear() {
        pending.clear()
        active = null
    }

    fun run(budgetNanos: Long): Long {
        var completed = 0L
        val deadline = if (budgetNanos == Long.MAX_VALUE) Long.MAX_VALUE else clock() + budgetNanos
        while (pending.isNotEmpty()) {
            val next = pending.minBy { it.priority }
            val work = if (next.priority < 0) next else active ?: next
            if (work === next && next.priority >= 0) active = work
            if (work.step(deadline)) {
                pending.remove(work)
                if (active === work) active = null
                completed++
            }
            if (clock() >= deadline) break
        }
        return completed
    }
}
