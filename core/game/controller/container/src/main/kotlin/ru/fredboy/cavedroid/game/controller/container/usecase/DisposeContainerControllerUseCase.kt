package ru.fredboy.cavedroid.game.controller.container.usecase

import ru.fredboy.cavedroid.game.controller.container.ContainerController
import javax.inject.Inject

class DisposeContainerControllerUseCase @Inject constructor(
    private val containerController: ContainerController,
) {

    operator fun invoke() {
        containerController.dispose()
    }

}
