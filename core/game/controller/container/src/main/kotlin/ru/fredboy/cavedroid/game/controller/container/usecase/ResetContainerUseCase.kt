package ru.fredboy.cavedroid.game.controller.container.usecase

import ru.fredboy.cavedroid.game.controller.container.ContainerController
import javax.inject.Inject

class ResetContainerUseCase @Inject constructor(
    private val containerController: ContainerController,
) {

    operator fun invoke(x: Int, y: Int, z: Int) {
        containerController.resetContainer(x, y, z)
    }

}
