package ru.fredboy.cavedroid.game.controller.drop.usecase

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.drop.model.Drop
import javax.inject.Inject

@GameScope
class ForEachDropUseCase @Inject constructor(
    private val dropController: DropController
) {

    operator fun invoke(action: (Drop) -> Unit) {
        dropController.forEach(action)
    }

}