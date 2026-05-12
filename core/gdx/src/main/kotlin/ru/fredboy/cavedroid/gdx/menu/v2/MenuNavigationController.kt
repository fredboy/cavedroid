package ru.fredboy.cavedroid.gdx.menu.v2

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ScalingViewport
import ktx.scene2d.Scene2DSkin
import ru.fredboy.cavedroid.common.coroutines.GdxMainDispatcher
import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.common.mvvm.NavBackStack
import ru.fredboy.cavedroid.common.mvvm.NavKey
import ru.fredboy.cavedroid.common.mvvm.NavRootStage
import ru.fredboy.cavedroid.common.mvvm.ViewModel
import ru.fredboy.cavedroid.common.mvvm.ViewModelProvider
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.gdx.menu.v2.view.about.AboutMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.about.AboutMenuViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.about.aboutMenuView
import ru.fredboy.cavedroid.gdx.menu.v2.view.attribution.AttributionMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.attribution.AttributionMenuViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.attribution.attributionMenuView
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.RootNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.death.DeathScreenNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.death.DeathScreenViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.death.deathScreenView
import ru.fredboy.cavedroid.gdx.menu.v2.view.deleteworld.DeleteWorldMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.deleteworld.DeleteWorldMenuViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.deleteworld.deleteWorldMenuView
import ru.fredboy.cavedroid.gdx.menu.v2.view.help.HelpMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.help.HelpMenuViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.help.helpMenuView
import ru.fredboy.cavedroid.gdx.menu.v2.view.language.LanguageMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.language.LanguageMenuViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.language.languageMenuView
import ru.fredboy.cavedroid.gdx.menu.v2.view.main.MainMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.main.MainMenuViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.main.mainMenuView
import ru.fredboy.cavedroid.gdx.menu.v2.view.newgame.NewGameMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.newgame.NewGameMenuViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.newgame.newGameMenuView
import ru.fredboy.cavedroid.gdx.menu.v2.view.notice.NoticeMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.notice.NoticeMenuViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.notice.noticeMenuView
import ru.fredboy.cavedroid.gdx.menu.v2.view.pause.PauseMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.pause.PauseMenuViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.pause.pauseMenuView
import ru.fredboy.cavedroid.gdx.menu.v2.view.settings.SettingsMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.settings.SettingsMenuViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.settings.settingsMenuView
import ru.fredboy.cavedroid.gdx.menu.v2.view.singleplayer.SinglePlayerMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.singleplayer.SinglePlayerMenuViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.singleplayer.singlePlayerMenuView
import javax.inject.Inject

@MenuScope
class MenuNavigationController @Inject constructor(
    private val viewModelProviders: Set<@JvmSuppressWildcards ViewModelProvider<*, *>>,
    private val applicationContextRepository: ApplicationContextRepository,
    private val rootNavKey: RootNavKey,
    skin: Skin,
) : Disposable {
    private val viewport = ScalingViewport(
        Scaling.stretch,
        applicationContextRepository.getWidth(),
        applicationContextRepository.getHeight(),
    )

    init {
        Scene2DSkin.defaultSkin = skin
    }

    private val navBackStack = NavBackStack(rootNavKey)

    val navRootStage = NavRootStage(viewport, navBackStack, GdxMainDispatcher) { navKey, cachedViewModel, render ->
        when (navKey) {
            is MainMenuNavKey -> {
                val viewModel = findViewModel<MainMenuNavKey, MainMenuViewModel>(navKey, cachedViewModel)
                render(viewModel) { mainMenuView(viewModel) }
            }

            is NewGameMenuNavKey -> {
                val viewModel = findViewModel<NewGameMenuNavKey, NewGameMenuViewModel>(navKey, cachedViewModel)
                render(viewModel) { newGameMenuView(viewModel) }
            }

            is SettingsMenuNavKey -> {
                val viewModel = findViewModel<SettingsMenuNavKey, SettingsMenuViewModel>(navKey, cachedViewModel)
                render(viewModel) { settingsMenuView(viewModel) }
            }

            is HelpMenuNavKey -> {
                val viewModel = findViewModel<HelpMenuNavKey, HelpMenuViewModel>(navKey, cachedViewModel)
                render(viewModel) { helpMenuView(viewModel) }
            }

            is AboutMenuNavKey -> {
                val viewModel = findViewModel<AboutMenuNavKey, AboutMenuViewModel>(navKey, cachedViewModel)
                render(viewModel) { aboutMenuView(viewModel) }
            }

            is AttributionMenuNavKey -> {
                val viewModel = findViewModel<AttributionMenuNavKey, AttributionMenuViewModel>(navKey, cachedViewModel)
                render(viewModel) { attributionMenuView(viewModel) }
            }

            is NoticeMenuNavKey -> {
                val viewModel = findViewModel<NoticeMenuNavKey, NoticeMenuViewModel>(navKey, cachedViewModel)
                render(viewModel) { noticeMenuView(viewModel) }
            }

            is SinglePlayerMenuNavKey -> {
                val viewModel =
                    findViewModel<SinglePlayerMenuNavKey, SinglePlayerMenuViewModel>(navKey, cachedViewModel)
                render(viewModel) { singlePlayerMenuView(viewModel) }
            }

            is DeleteWorldMenuNavKey -> {
                val viewModel = findViewModel<DeleteWorldMenuNavKey, DeleteWorldMenuViewModel>(navKey, cachedViewModel)
                render(viewModel) { deleteWorldMenuView(viewModel) }
            }

            is PauseMenuNavKey -> {
                val viewModel = findViewModel<PauseMenuNavKey, PauseMenuViewModel>(navKey, cachedViewModel)
                render(viewModel) { pauseMenuView(viewModel) }
            }

            is DeathScreenNavKey -> {
                val viewModel = findViewModel<DeathScreenNavKey, DeathScreenViewModel>(navKey, cachedViewModel)
                render(viewModel) { deathScreenView(viewModel) }
            }

            is LanguageMenuNavKey -> {
                val viewModel = findViewModel<LanguageMenuNavKey, LanguageMenuViewModel>(navKey, cachedViewModel)
                render(viewModel) { languageMenuView(viewModel) }
            }

            else -> throw IllegalStateException("Unknown key $navKey")
        }
    }

    fun reset() {
        navBackStack.reset()
    }

    private inline fun <reified K : NavKey, reified V : ViewModel> findViewModel(
        navKey: K,
        cachedViewModel: ViewModel?,
    ): V {
        val provider by lazy {
            @Suppress("UNCHECKED_CAST")
            viewModelProviders.first { it.viewModelClass == V::class } as ViewModelProvider<K, V>
        }
        return (cachedViewModel as? V) ?: provider.get(navKey, navBackStack)
    }

    override fun dispose() {
        navRootStage.dispose()
    }
}
