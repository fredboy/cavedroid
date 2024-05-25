package ru.deadsoftware.cavedroid

import dagger.Component
import ru.deadsoftware.cavedroid.game.GameScreen
import ru.deadsoftware.cavedroid.menu.MenuScreen
import ru.deadsoftware.cavedroid.misc.utils.AssetLoader
import ru.deadsoftware.cavedroid.prefs.PreferencesStore
import javax.inject.Singleton

@Singleton
@Component(dependencies = [CaveGame::class, PreferencesStore::class])
interface MainComponent {

    val gameScreen: GameScreen

    val menuScreen: MenuScreen

    val mainConfig: MainConfig

    val assetLoader: AssetLoader

}