package ru.fredboy.cavedroid.gdx.menu.v2

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ScalingViewport
import ktx.scene2d.Scene2DSkin
import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavKey
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavRootStage
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.ViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.ViewModelProvider
import ru.fredboy.cavedroid.gdx.menu.v2.view.main.MainMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.main.MainMenuViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.main.mainMenuView
import ru.fredboy.cavedroid.gdx.menu.v2.view.newgame.NewGameMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.newgame.NewGameMenuViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.newgame.newGameMenuView
import ru.fredboy.cavedroid.gdx.menu.v2.view.settings.SettingsMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.settings.SettingsMenuViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.settings.settingsMenuView
import javax.inject.Inject

@MenuScope
class MenuNavigationController @Inject constructor(
    private val viewModelProviders: Set<@JvmSuppressWildcards ViewModelProvider<*>>,
    private val applicationContextRepository: ApplicationContextRepository,
) : Disposable {
    private val viewport = ScalingViewport(
        Scaling.stretch,
        applicationContextRepository.getWidth(),
        applicationContextRepository.getHeight(),
    )

    private val skin = Skin(Gdx.files.internal("skin/skin"))

    init {
        Scene2DSkin.defaultSkin = skin
    }

    private val navBackStack = NavBackStack(MainMenuNavKey)

    val navRootStage = NavRootStage(viewport, navBackStack) { navKey, cachedViewModel ->
        when (navKey) {
            is MainMenuNavKey -> {
                val viewModel = findViewModel<MainMenuViewModel>(navKey, cachedViewModel)
                mainMenuView(viewModel)
                viewModel
            }

            is NewGameMenuNavKey -> {
                val viewModel = findViewModel<NewGameMenuViewModel>(navKey, cachedViewModel)
                newGameMenuView(viewModel)
                viewModel
            }

            is SettingsMenuNavKey -> {
                val viewModel = findViewModel<SettingsMenuViewModel>(navKey, cachedViewModel)
                settingsMenuView(viewModel)
                viewModel
            }

            else -> throw IllegalStateException("Unknown key $navKey")
        }
    }

    private inline fun <reified V : Any> findViewModel(navKey: NavKey, cachedViewModel: ViewModel?): V {
        return (cachedViewModel as? V) ?: viewModelProviders.first { it.viewModelClass == V::class }
            .get(navKey, navBackStack) as V
    }

    override fun dispose() {
        navRootStage.dispose()
        skin.dispose()
    }
}
