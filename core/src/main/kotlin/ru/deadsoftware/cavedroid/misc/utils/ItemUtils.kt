package ru.deadsoftware.cavedroid.misc.utils

import ru.deadsoftware.cavedroid.game.GameItemsHolder.Companion.FALLBACK_ITEM_KEY
import ru.deadsoftware.cavedroid.game.model.item.Item

fun Item.isFallback(): Boolean {
    return this.params.key == FALLBACK_ITEM_KEY
}
