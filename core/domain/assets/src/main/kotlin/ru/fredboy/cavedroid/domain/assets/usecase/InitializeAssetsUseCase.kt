package ru.fredboy.cavedroid.domain.assets.usecase

import dagger.Reusable
import ru.fredboy.cavedroid.common.performance.NoOpPerformanceMonitor
import ru.fredboy.cavedroid.common.performance.PerformanceMonitor
import ru.fredboy.cavedroid.common.performance.PerformanceOperation
import ru.fredboy.cavedroid.common.performance.operation
import ru.fredboy.cavedroid.domain.assets.GameAssetsHolder
import javax.inject.Inject

@Reusable
class InitializeAssetsUseCase @Inject constructor(
    private val performanceMonitor: PerformanceMonitor = NoOpPerformanceMonitor,
    private val gameAssetsHolder: GameAssetsHolder,
) {

    operator fun invoke() {
        performanceMonitor.operation(PerformanceOperation.ASSETS_LOAD) {
            gameAssetsHolder.initializeRepository()
        }
    }
}
