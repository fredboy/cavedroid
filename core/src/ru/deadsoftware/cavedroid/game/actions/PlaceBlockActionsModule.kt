package ru.deadsoftware.cavedroid.game.actions

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.actions.placeblock.IPlaceBlockAction
import ru.deadsoftware.cavedroid.game.actions.placeblock.PlaceBlockItemToBackgroundAction
import ru.deadsoftware.cavedroid.game.actions.placeblock.PlaceBlockItemToForegroundAction
import ru.deadsoftware.cavedroid.game.actions.placeblock.PlaceSlabAction

@Module
class PlaceBlockActionsModule {

    @Binds
    @IntoMap
    @StringKey(PlaceBlockItemToForegroundAction.ACTION_KEY)
    @GameScope
    fun bindPlaceBlockItemToForegroundAction(action: PlaceBlockItemToForegroundAction): IPlaceBlockAction {
        return action
    }

    @Binds
    @IntoMap
    @StringKey(PlaceBlockItemToBackgroundAction.ACTION_KEY)
    @GameScope
    fun bindPlaceBlockItemToBackgroundAction(action: PlaceBlockItemToBackgroundAction): IPlaceBlockAction {
        return action
    }

    @Binds
    @IntoMap
    @StringKey(PlaceSlabAction.ACTION_KEY)
    @GameScope
    fun bindPlaceSlabAction(action: PlaceSlabAction): IPlaceBlockAction {
        return action
    }

}