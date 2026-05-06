package ru.fredboy.cavedroid.common.mvvm

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ViewModelTest {

    private class TrackedViewModel : ViewModel() {
        var disposeCalls: Int = 0
            private set

        override fun onDispose() {
            disposeCalls++
        }
    }

    @Test
    fun `viewModelScope is active on construction`() {
        val vm = TrackedViewModel()

        assertTrue(vm.viewModelScope.coroutineContext[kotlinx.coroutines.Job]!!.isActive)
    }

    @Test
    fun `dispose cancels viewModelScope`() = runTest {
        val vm = TrackedViewModel()
        val launched = vm.viewModelScope.launch {
            while (true) {
                delay(1_000_000)
            }
        }

        vm.dispose()

        assertTrue(launched.isCancelled)
        assertFalse(vm.viewModelScope.coroutineContext[kotlinx.coroutines.Job]!!.isActive)
    }

    @Test
    fun `dispose calls onDispose exactly once per call`() {
        val vm = TrackedViewModel()

        vm.dispose()

        assertEquals(1, vm.disposeCalls)
    }
}
