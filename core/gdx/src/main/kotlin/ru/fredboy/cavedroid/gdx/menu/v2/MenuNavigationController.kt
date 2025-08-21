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

            is HelpMenuNavKey -> {
                val viewModel = findViewModel<HelpMenuViewModel>(navKey, cachedViewModel)
                helpMenuView(viewModel)
                viewModel
            }

            is AboutMenuNavKey -> {
                val viewModel = findViewModel<AboutMenuViewModel>(navKey, cachedViewModel)
                aboutMenuView(viewModel)
                viewModel
            }

            is AttributionMenuNavKey -> {
                val viewModel = findViewModel<AttributionMenuViewModel>(navKey, cachedViewModel)
                attributionMenuView(viewModel)
                viewModel
            }

            is NoticeMenuNavKey -> {
                val viewModel = findViewModel<NoticeMenuViewModel>(navKey, cachedViewModel)
                noticeMenuView(viewModel)
                viewModel
            }

            else -> throw IllegalStateException("Unknown key $navKey")
        }
    }

    fun reset() {
        navBackStack.reset()
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
