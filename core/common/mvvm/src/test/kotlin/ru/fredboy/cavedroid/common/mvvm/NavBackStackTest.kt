package ru.fredboy.cavedroid.common.mvvm

import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NavBackStackTest {

    private object KeyA : NavKey
    private object KeyB : NavKey
    private object KeyC : NavKey

    private fun fixture(): Pair<NavBackStack, NavStageHost> {
        val host = mockk<NavStageHost>(relaxed = true)
        val stack = NavBackStack(KeyA)
        stack.attachHost(host)
        return stack to host
    }

    @Test
    fun `attachHost notifies host with current top key`() {
        val host = mockk<NavStageHost>(relaxed = true)
        val stack = NavBackStack(KeyA)

        stack.attachHost(host)

        verify(exactly = 1) { host.onStackChanged(KeyA, null) }
    }

    @Test
    fun `push notifies host with the new top key`() {
        val (stack, host) = fixture()

        stack.push(KeyB)

        verifyOrder {
            host.onStackChanged(KeyA, null)
            host.onStackChanged(KeyB, null)
        }
    }

    @Test
    fun `pop reveals previous key and supplies popped key`() {
        val (stack, host) = fixture()
        stack.push(KeyB)

        stack.pop()

        verify(exactly = 1) { host.onStackChanged(KeyA, KeyB) }
    }

    @Test
    fun `pop on a single-item stack is a no-op`() {
        val (stack, host) = fixture()

        stack.pop()

        verify(exactly = 1) { host.onStackChanged(KeyA, null) }
        confirmVerified(host)
    }

    @Test
    fun `reset clears VMs for popped keys and notifies with root key`() {
        val (stack, host) = fixture()
        stack.push(KeyB)
        stack.push(KeyC)

        stack.reset()

        verify { host.clearViewModelFor(KeyC) }
        verify { host.clearViewModelFor(KeyB) }
        verify { host.onStackChanged(KeyA, null) }
    }

    @Test
    fun `hasKey reports membership`() {
        val (stack, _) = fixture()
        stack.push(KeyB)

        assertTrue(stack.hasKey(KeyA))
        assertTrue(stack.hasKey(KeyB))
        assertFalse(stack.hasKey(KeyC))
    }
}
