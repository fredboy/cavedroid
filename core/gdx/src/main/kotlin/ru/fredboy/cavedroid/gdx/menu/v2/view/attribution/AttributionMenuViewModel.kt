package ru.fredboy.cavedroid.gdx.menu.v2.view.attribution

import ru.fredboy.cavedroid.domain.assets.repository.FontAssetsRepository
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModel

class AttributionMenuViewModel(
    private val navBackStack: NavBackStack,
    fontAssetsRepository: FontAssetsRepository,
) : BaseViewModel(fontAssetsRepository) {

    fun onBackClick() {
        navBackStack.pop()
    }
}
