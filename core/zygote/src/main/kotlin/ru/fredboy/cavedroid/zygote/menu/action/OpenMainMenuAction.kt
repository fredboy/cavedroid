package ru.fredboy.cavedroid.zygote.menu.action

import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.domain.menu.repository.MenuButtonRepository
import ru.fredboy.cavedroid.zygote.menu.action.annotation.BindsMenuAction
import javax.inject.Inject

@MenuScope
@BindsMenuAction(OpenMainMenuAction.KEY)
class OpenMainMenuAction @Inject constructor(
    private val menuButtonRepository: MenuButtonRepository,
) : IMenuAction {

    override fun perform() {
        menuButtonRepository.setCurrentMenu("main")
    }

    companion object {
        const val KEY = "menu_main_action"
    }
}
