package ru.fredboy.cavedroid.gameplay.rendering.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BudgetedWorkQueueTest {
    @Test
    fun `visible edits take priority and incomplete work resumes next frame`() {
        var now = 0L
        val queue = BudgetedWorkQueue { now }
        val calls = mutableListOf<Int>()
        fun work(priority: Int, count: Int): BudgetedWorkQueue.Work = object : BudgetedWorkQueue.Work {
            override val priority = priority
            var remaining = count
            override fun step(deadline: Long): Boolean {
                calls.add(priority)
                now++
                return --remaining == 0
            }
        }
        queue.add(work(100, 1))
        queue.add(work(0, 3))
        assertEquals(0L, queue.run(2))
        assertEquals(listOf(0, 0), calls)
        assertEquals(2L, queue.run(2))
        assertEquals(listOf(0, 0, 0, 100), calls)
    }

    @Test
    fun `evicted jobs never execute and scheduling is idempotent`() {
        var calls = 0
        val queue = BudgetedWorkQueue()
        val job = object : BudgetedWorkQueue.Work {
            override val priority = 0
            override fun step(deadline: Long): Boolean {
                calls++
                return true
            }
        }
        queue.add(job)
        queue.add(job)
        queue.run(Long.MAX_VALUE)
        assertEquals(1, calls)
        queue.add(job)
        queue.remove(job)
        queue.run(Long.MAX_VALUE)
        assertEquals(1, calls)
    }
}
