package ru.fredboy.cavedroid.domain.assets.usecase

import dagger.Reusable
import ru.fredboy.cavedroid.domain.assets.GameAssetsHolder
import ru.fredboy.cavedroid.domain.assets.model.TouchButton
import javax.inject.Inject

@Reusable
class GetTouchButtonsUseCase @Inject constructor(
    private val gameAssetsHolder: GameAssetsHolder,
) {

    operator fun invoke(): Map<String, TouchButton> = gameAssetsHolder.getTouchButtons()
}
