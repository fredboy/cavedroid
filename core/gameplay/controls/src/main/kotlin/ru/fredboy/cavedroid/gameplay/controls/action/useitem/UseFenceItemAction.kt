package ru.fredboy.cavedroid.gameplay.controls.action.useitem

import ru.fredboy.cavedroid.common.api.OnboardingEvents
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.gameplay.controls.action.annotation.BindUseItemAction
import ru.fredboy.cavedroid.gameplay.controls.action.placeblock.PlaceBlockItemToBackgroundAction
import ru.fredboy.cavedroid.gameplay.controls.action.placeblock.PlaceBlockItemToForegroundAction
import javax.inject.Inject

@GameScope
@BindUseItemAction(UseFenceItemAction.ACTION_KEY)
class UseFenceItemAction @Inject constructor(
    private val getBlockByKey: GetBlockByKeyUseCase,
    private val placeForeground: PlaceBlockItemToForegroundAction,
    private val placeBackground: PlaceBlockItemToBackgroundAction,
    private val onboardingEvents: OnboardingEvents,
) : IUseItemAction {

    override fun perform(item: Item.Usable, x: Int, y: Int): Boolean {
        return placeForeground.place(item.asPlaceable(), x, y).also { placed ->
            if (placed) onboardingEvents.notifyPlace()
        }
    }

    override fun performBackground(item: Item.Usable, x: Int, y: Int): Boolean {
        return placeBackground.place(item.asPlaceable(), x, y)
    }

    private fun Item.Usable.asPlaceable(): Item.Block = Item.Block(params, getBlockByKey[params.key])

    companion object {
        const val ACTION_KEY = "use_fence_item"
    }
}
