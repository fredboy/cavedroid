package ru.deadsoftware.cavedroid.game.actions

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.actions.updateblock.*

@Module
class UpdateBlockActionsModule {

    @Binds
    @IntoMap
    @StringKey(UpdateSandAction.BLOCK_KEY)
    @GameScope
    fun bindUpdateSandAction(action: UpdateSandAction): IUpdateBlockAction {
        return action;
    }

    @Binds
    @IntoMap
    @StringKey(UpdateGravelAction.BLOCK_KEY)
    @GameScope
    fun bindUpdateGravelAction(action: UpdateGravelAction): IUpdateBlockAction {
        return action;
    }

    @Binds
    @IntoMap
    @StringKey(UpdateRequiresBlockAction.ACTION_KEY)
    @GameScope
    fun bindUpdateRequiresBlockAction(action: UpdateRequiresBlockAction): IUpdateBlockAction {
        return action;
    }

    @Binds
    @IntoMap
    @StringKey(UpdateGrassAction.BLOCK_KEY)
    @GameScope
    fun bindUpdateGrassAction(action: UpdateGrassAction): IUpdateBlockAction {
        return action;
    }
}