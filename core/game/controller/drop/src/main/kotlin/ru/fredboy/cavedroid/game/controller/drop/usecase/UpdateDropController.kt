package ru.fredboy.cavedroid.game.controller.drop.usecase

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.game.controller.drop.DropController
import javax.inject.Inject

@GameScope
class UpdateDropController @Inject constructor(
    private val dropController: DropController
) {

    operator fun invoke(delta: Float) {
        dropController.update(delta)
    }

}