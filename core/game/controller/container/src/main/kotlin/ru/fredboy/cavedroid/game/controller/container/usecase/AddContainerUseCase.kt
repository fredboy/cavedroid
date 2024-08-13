package ru.fredboy.cavedroid.game.controller.container.usecase

import ru.fredboy.cavedroid.game.controller.container.ContainerController
import ru.fredboy.cavedroid.game.controller.container.model.Container
import javax.inject.Inject

class AddContainerUseCase @Inject constructor(
    private val containerController: ContainerController,
) {

    operator fun invoke(x: Int, y: Int, z: Int, container: Container) {
        containerController.addContainer(x, y, z, container)
    }

}
