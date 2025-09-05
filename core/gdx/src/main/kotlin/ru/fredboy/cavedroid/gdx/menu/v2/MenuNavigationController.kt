package ru.fredboy.cavedroid.gdx.menu.v2

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
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
import ru.fredboy.cavedroid.gdx.menu.v2.view.about.AboutMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.about.AboutMenuViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.about.aboutMenuView
import ru.fredboy.cavedroid.gdx.menu.v2.view.attribution.AttributionMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.attribution.AttributionMenuViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.attribution.attributionMenuView
import ru.fredboy.cavedroid.gdx.menu.v2.view.deleteworld.DeleteWorldMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.deleteworld.DeleteWorldMenuViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.deleteworld.deleteWorldMenuView
import ru.fredboy.cavedroid.gdx.menu.v2.view.help.HelpMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.help.HelpMenuViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.help.helpMenuView
import ru.fredboy.cavedroid.gdx.menu.v2.view.main.MainMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.main.MainMenuViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.main.mainMenuView
import ru.fredboy.cavedroid.gdx.menu.v2.view.newgame.NewGameMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.newgame.NewGameMenuViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.newgame.newGameMenuView
import ru.fredboy.cavedroid.gdx.menu.v2.view.notice.NoticeMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.notice.NoticeMenuViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.notice.noticeMenuView
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
) : Disposable {
    private val viewport = ScalingViewport(
        Scaling.stretch,
        applicationContextRepository.getWidth(),
        applicationContextRepository.getHeight(),
    )

    private val skin = Skin(Gdx.files.internal("skin/skin.json"))
        .apply {
            atlas.textures.forEach { texture ->
                texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
            }

            getAll(BitmapFont::class.java)
                .forEach { entry ->
                    entry.value.data.setScale(1f)
                }
        }

    init {
        Scene2DSkin.defaultSkin = skin
    }

    private val navBackStack = NavBackStack(MainMenuNavKey)

    val navRootStage = NavRootStage(viewport, navBackStack) { navKey, cachedViewModel ->
        when (navKey) {
            is MainMenuNavKey -> {
                val viewModel = findViewModel<MainMenuNavKey, MainMenuViewModel>(navKey, cachedViewModel)
                mainMenuView(viewModel)
            }

            is NewGameMenuNavKey -> {
                val viewModel = findViewModel<NewGameMenuNavKey, NewGameMenuViewModel>(navKey, cachedViewModel)
                newGameMenuView(viewModel)
            }

            is SettingsMenuNavKey -> {
                val viewModel = findViewModel<SettingsMenuNavKey, SettingsMenuViewModel>(navKey, cachedViewModel)
                settingsMenuView(viewModel)
            }

            is HelpMenuNavKey -> {
                val viewModel = findViewModel<HelpMenuNavKey, HelpMenuViewModel>(navKey, cachedViewModel)
                helpMenuView(viewModel)
            }

            is AboutMenuNavKey -> {
                val viewModel = findViewModel<AboutMenuNavKey, AboutMenuViewModel>(navKey, cachedViewModel)
                aboutMenuView(viewModel)
            }

            is AttributionMenuNavKey -> {
                val viewModel = findViewModel<AttributionMenuNavKey, AttributionMenuViewModel>(navKey, cachedViewModel)
                attributionMenuView(viewModel)
            }

            is NoticeMenuNavKey -> {
                val viewModel = findViewModel<NoticeMenuNavKey, NoticeMenuViewModel>(navKey, cachedViewModel)
                noticeMenuView(viewModel)
            }

            is SinglePlayerMenuNavKey -> {
                val viewModel =
                    findViewModel<SinglePlayerMenuNavKey, SinglePlayerMenuViewModel>(navKey, cachedViewModel)
                singlePlayerMenuView(viewModel)
            }

            is DeleteWorldMenuNavKey -> {
                val viewModel = findViewModel<DeleteWorldMenuNavKey, DeleteWorldMenuViewModel>(navKey, cachedViewModel)
                deleteWorldMenuView(viewModel)
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
        val provider by lazy { viewModelProviders.first { it.viewModelClass == V::class } as ViewModelProvider<K, V> }
        return (cachedViewModel as? V) ?: provider.get(navKey, navBackStack)
    }

    override fun dispose() {
        navRootStage.dispose()
        skin.dispose()
    }
}
