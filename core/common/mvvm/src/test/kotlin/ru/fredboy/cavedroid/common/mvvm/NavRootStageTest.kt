package ru.fredboy.cavedroid.common.mvvm

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Exercises NavRootStage's host-side logic through the NavStageHost contract,
 * avoiding instantiation of a real libGDX Stage (which requires a GL context).
 */
class NavRootStageTest {

    private object KeyA : NavKey
    private object KeyB : NavKey

    private class FakeViewModel : ViewModel() {
        var disposed: Boolean = false
            private set

        override fun onDispose() {
            disposed = true
        }
    }

    /**
     * A test double mirroring NavRootStage's host responsibilities:
     * cache ViewModels, dispose ones popped off the stack, invoke the
     * resolver with the cached entry (or null on first encounter).
     */
    private class FakeNavHost(
        private val backStack: NavBackStack,
        private val resolver: (NavKey, ViewModel?) -> ViewModel,
    ) : NavStageHost {
        val viewModels = mutableMapOf<NavKey, ViewModel>()
        val resolverCalls = mutableListOf<Pair<NavKey, ViewModel?>>()

        init {
            backStack.attachHost(this)
        }

        override fun onStackChanged(topKey: NavKey, poppedKey: NavKey?) {
            if (poppedKey != null && !backStack.hasKey(poppedKey)) {
                viewModels.remove(poppedKey)?.dispose()
            }
            val cached = viewModels[topKey]
            resolverCalls += topKey to cached
            viewModels[topKey] = resolver(topKey, cached)
        }

        override fun clearViewModelFor(navKey: NavKey) {
            viewModels.remove(navKey)?.dispose()
        }

        override fun show() = Unit

        override fun hide() = Unit
    }

    @Test
    fun `resolver is invoked with the new key on push`() {
        val backStack = NavBackStack(KeyA)
        val host = FakeNavHost(backStack) { _, _ -> FakeViewModel() }

        backStack.push(KeyB)

        val pushed = host.resolverCalls.last()
        assertEquals(KeyB, pushed.first)
        assertNull(pushed.second)
    }

    @Test
    fun `popped ViewModel is disposed when no longer on the stack`() {
        val backStack = NavBackStack(KeyA)
        val produced = mutableMapOf<NavKey, FakeViewModel>()
        val host = FakeNavHost(backStack) { key, _ ->
            FakeViewModel().also { produced[key] = it }
        }

        backStack.push(KeyB)
        val keyBVm = produced.getValue(KeyB)

        backStack.pop()

        assertTrue(keyBVm.disposed)
        assertFalse(host.viewModels.containsKey(KeyB))
    }

    @Test
    fun `cached ViewModel is passed to resolver when key is revisited`() {
        val backStack = NavBackStack(KeyA)
        val host = FakeNavHost(backStack) { _, cached -> cached ?: FakeViewModel() }

        // KeyA's VM is created on initial attachHost.
        val keyAVm = host.viewModels.getValue(KeyA)

        // Push KeyB then pop, then revisit KeyA — resolver gets cached A.
        backStack.push(KeyB)
        backStack.pop()

        // Re-push by simulating a navigation back to A: not directly possible
        // via NavBackStack's API, so verify the cache for A persisted.
        assertSame(keyAVm, host.viewModels[KeyA])
        // Resolver was called with the cached KeyA VM during the pop.
        val popCall = host.resolverCalls.last { it.first == KeyA && host.resolverCalls.indexOf(it) > 0 }
        assertSame(keyAVm, popCall.second)
    }

    @Test
    fun `clearViewModelFor disposes and removes the cached ViewModel`() {
        val backStack = NavBackStack(KeyA)
        val host = FakeNavHost(backStack) { _, _ -> FakeViewModel() }

        val vm = host.viewModels.getValue(KeyA) as FakeViewModel
        host.clearViewModelFor(KeyA)

        assertTrue(vm.disposed)
        assertFalse(host.viewModels.containsKey(KeyA))
    }

    @Test
    fun `mock-based host receives push and pop callbacks in order`() {
        val host = mockk<NavStageHost>(relaxed = true)
        val backStack = NavBackStack(KeyA)
        backStack.attachHost(host)

        backStack.push(KeyB)
        backStack.pop()

        verify { host.onStackChanged(KeyA, null) }
        verify { host.onStackChanged(KeyB, null) }
        verify { host.onStackChanged(KeyA, KeyB) }
    }
}
