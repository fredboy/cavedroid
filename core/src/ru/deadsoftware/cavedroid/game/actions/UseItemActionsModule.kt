package ru.deadsoftware.cavedroid.game.actions

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.actions.useitem.*

@Module
class UseItemActionsModule {

    @Binds
    @IntoMap
    @StringKey(UseWaterBucketAction.ACTION_KEY)
    @GameScope
    fun bindUseWaterBucketAction(action: UseWaterBucketAction): IUseItemAction {
        return action
    }

    @Binds
    @IntoMap
    @StringKey(UseLavaBucketAction.ACTION_KEY)
    @GameScope
    fun bindUseLavaBucketAction(action: UseLavaBucketAction): IUseItemAction {
        return action
    }

}
