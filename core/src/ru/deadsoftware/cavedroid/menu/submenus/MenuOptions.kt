package ru.deadsoftware.cavedroid.menu.submenus

import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.menu.MenuProc
import ru.deadsoftware.cavedroid.menu.objects.ButtonEventListener
import ru.deadsoftware.cavedroid.menu.objects.ButtonRenderer
import ru.deadsoftware.cavedroid.misc.utils.AssetLoader

class MenuOptions(
    width: Float,
    height: Float,
    buttonRenderer: ButtonRenderer,
    mainConfig: MainConfig,
    menuInput: MenuProc.Input,
    assetLoader: AssetLoader,
) : Menu(width, height, buttonRenderer, mainConfig, menuInput, assetLoader) {

    override fun getButtonEventListeners(): HashMap<String, ButtonEventListener> {
        val map = HashMap<String, ButtonEventListener>()
        map["dyncam"] = ButtonEventListener { mMenuInput.toggleDynamicCamera() }
        map["back"] = ButtonEventListener { mMenuInput.backClicked() }
        return map
    }

    override fun initButtons() {
        loadButtonsFromJson(mAssetLoader.getAssetHandle("json/menu_options_buttons.json"))
    }
}