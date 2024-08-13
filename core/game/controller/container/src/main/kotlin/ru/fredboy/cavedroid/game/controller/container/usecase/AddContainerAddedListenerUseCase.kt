package ru.fredboy.cavedroid.game.controller.container.usecase

import ru.fredboy.cavedroid.game.controller.container.ContainerController
import ru.fredboy.cavedroid.game.controller.container.listener.ContainerAddedListener
import javax.inject.Inject

class AddContainerAddedListenerUseCase @Inject constructor(
    private val containerController: ContainerController,
) {

    operator fun invoke(containerAddedListener: ContainerAddedListener) {
        containerController.addContainerAddedListener(containerAddedListener)
    }

}
