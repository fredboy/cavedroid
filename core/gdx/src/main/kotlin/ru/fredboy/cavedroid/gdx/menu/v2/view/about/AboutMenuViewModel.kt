package ru.fredboy.cavedroid.gdx.menu.v2.view.about

import com.badlogic.gdx.Gdx
import ru.fredboy.cavedroid.common.CaveDroidConstants
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModelDependencies

class AboutMenuViewModel(
    private val navBackStack: NavBackStack,
    baseViewModelDependencies: BaseViewModelDependencies,
) : BaseViewModel(baseViewModelDependencies) {

    fun onGithubClick() {
        Gdx.net.openURI(CaveDroidConstants.GITHUB_LINK)
    }

    fun onBackClick() {
        navBackStack.pop()
    }
}
