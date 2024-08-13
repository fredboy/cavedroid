package ru.fredboy.cavedroid.game.controller.container.usecase

import ru.fredboy.cavedroid.game.controller.container.ContainerController
import ru.fredboy.cavedroid.game.controller.container.listener.ContainerRemovedListener
import javax.inject.Inject

class RemoveContainerRemovedListenerUseCase @Inject constructor(
    private val containerController: ContainerController,
) {

    operator fun invoke(containerRemovedListener: ContainerRemovedListener) {
        containerController.removeContainerRemovedListener(containerRemovedListener)
    }

}
