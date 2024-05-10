package ru.deadsoftware.cavedroid.game.actions

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.actions.useblock.IUseBlockAction
import ru.deadsoftware.cavedroid.game.actions.useblock.UseCraftingTableAction
import ru.deadsoftware.cavedroid.game.actions.useblock.UseFurnaceAction

@Module
class UseBlockActionsModule {

    @Binds
    @IntoMap
    @StringKey(UseCraftingTableAction.KEY)
    @GameScope
    fun bindUseCraftingTableAction(action: UseCraftingTableAction): IUseBlockAction {
        return action
    }

    @Binds
    @IntoMap
    @StringKey(UseFurnaceAction.KEY)
    @GameScope
    fun bindUseFurnaceTableAction(action: UseFurnaceAction): IUseBlockAction {
        return action
    }
}
