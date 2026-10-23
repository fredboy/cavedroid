package ru.fredboy.cavedroid.gameplay.controls.input.handler.keyboard

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.performance.NoOpPerformanceMonitor
import ru.fredboy.cavedroid.common.performance.PerformanceMonitor
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.gameplay.controls.input.IKeyboardInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.action.KeyboardInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.action.keys.KeyboardInputActionKey
import ru.fredboy.cavedroid.gameplay.controls.input.annotation.BindKeyboardInputHandler
import javax.inject.Inject

@GameScope
@BindKeyboardInputHandler
class ToggleDebugInfoKeyboardInputHandler @Inject constructor(
    private val performanceMonitor: PerformanceMonitor = NoOpPerformanceMonitor,
    private val gameContextRepository: GameContextRepository,
) : IKeyboardInputHandler {

    override fun checkConditions(action: KeyboardInputAction): Boolean = action.actionKey is KeyboardInputActionKey.ShowDebug && action.isKeyDown

    override fun handle(action: KeyboardInputAction) {
        gameContextRepository.setShowInfo(!gameContextRepository.shouldShowInfo())
        performanceMonitor.setDebugOverlay(gameContextRepository.shouldShowInfo())
    }

    override fun reset() {
        // no-op
    }
}
