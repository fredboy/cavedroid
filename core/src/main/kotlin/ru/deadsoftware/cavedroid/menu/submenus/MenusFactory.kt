package ru.deadsoftware.cavedroid.menu.submenus

import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.menu.MenuProc
import ru.deadsoftware.cavedroid.menu.MenuScope
import ru.deadsoftware.cavedroid.menu.objects.ButtonRenderer
import ru.deadsoftware.cavedroid.misc.utils.AssetLoader
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import javax.inject.Inject

@MenuScope
class MenusFactory @Inject constructor(
    private val mainConfig: MainConfig,
    private val assetLoader: AssetLoader,
    private val getTextureRegionByName: GetTextureRegionByNameUseCase,
) {

    fun getMainMenu(
        width: Float,
        height: Float,
        buttonRenderer: ButtonRenderer,
        menuInput: MenuProc.Input,
    ): MenuMain {
        return MenuMain(width, height, buttonRenderer, mainConfig, menuInput, assetLoader, getTextureRegionByName)
    }

    fun getMenuNewGame(
        width: Float,
        height: Float,
        buttonRenderer: ButtonRenderer,
        menuInput: MenuProc.Input,
    ): MenuNewGame {
        return MenuNewGame(width, height, buttonRenderer, mainConfig, menuInput, assetLoader, getTextureRegionByName)
    }

    fun getMenuOptions(
        width: Float,
        height: Float,
        buttonRenderer: ButtonRenderer,
        menuInput: MenuProc.Input,
    ): MenuOptions {
        return MenuOptions(width, height, buttonRenderer, mainConfig, menuInput, assetLoader, getTextureRegionByName)
    }

}