package ru.fredboy.cavedroid.game.controller.container.usecase

import ru.fredboy.cavedroid.game.controller.container.ContainerController
import javax.inject.Inject

class DestroyContainerUseCase @Inject constructor(
    private val containerController: ContainerController,
) {

    operator fun invoke(x: Int, y: Int, z: Int) {
        containerController.destroyContainer(x, y, z)
    }

}
