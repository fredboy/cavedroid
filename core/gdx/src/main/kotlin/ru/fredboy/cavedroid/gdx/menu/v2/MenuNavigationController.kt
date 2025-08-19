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
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.ViewModelProvider
import ru.fredboy.cavedroid.gdx.menu.v2.stage.main.MainMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.stage.main.MainMenuViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.stage.main.mainMenuView
import ru.fredboy.cavedroid.gdx.menu.v2.stage.newgame.NewGameMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.stage.newgame.NewGameMenuViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.stage.newgame.newGameMenuView
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

    val navRootStage = NavRootStage(viewport, navBackStack) { navKey ->
        when (navKey) {
            is MainMenuNavKey -> {
                val viewModel = findViewModel<MainMenuViewModel>(navKey)
                mainMenuView(viewModel)
            }

            is NewGameMenuNavKey -> {
                val viewModel = findViewModel<NewGameMenuViewModel>(navKey)
                newGameMenuView(viewModel)
            }
        }
    }

    private inline fun <reified ViewModel : Any> findViewModel(navKey: NavKey): ViewModel {
        return viewModelProviders.first { it.viewModelClass == ViewModel::class }
            .get(navKey, navBackStack) as ViewModel
    }

    override fun dispose() {
        navRootStage.dispose()
        skin.dispose()
    }
}
