package ru.fredboy.cavedroid.game.controller.container.usecase

import ru.fredboy.cavedroid.game.controller.container.ContainerController
import javax.inject.Inject

class UpdateContainerControllerUseCase @Inject constructor(
    private val containerController: ContainerController,
) {

    operator fun invoke(delta: Float) {
        containerController.update(delta)
    }

}
