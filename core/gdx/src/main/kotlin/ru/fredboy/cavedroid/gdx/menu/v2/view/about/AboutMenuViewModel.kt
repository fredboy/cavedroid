package ru.fredboy.cavedroid.gdx.menu.v2.view.about

import com.badlogic.gdx.Gdx
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.ViewModel

class AboutMenuViewModel(
    private val navBackStack: NavBackStack,
) : ViewModel() {

    fun onGithubClick() {
        Gdx.net.openURI("https://github.com/fredboy/cavedroid")
    }

    fun onBackClick() {
        navBackStack.pop()
    }
}
