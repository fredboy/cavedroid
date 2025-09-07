package ru.fredboy.cavedroid.gdx.menu.v2.view.about

import com.badlogic.gdx.Gdx
import ru.fredboy.cavedroid.common.CaveDroidConstants
import ru.fredboy.cavedroid.domain.assets.repository.FontAssetsRepository
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModel

class AboutMenuViewModel(
    private val navBackStack: NavBackStack,
    fontAssetsRepository: FontAssetsRepository,
) : BaseViewModel(fontAssetsRepository) {

    fun onGithubClick() {
        Gdx.net.openURI(CaveDroidConstants.GITHUB_LINK)
    }

    fun onBackClick() {
        navBackStack.pop()
    }
}
