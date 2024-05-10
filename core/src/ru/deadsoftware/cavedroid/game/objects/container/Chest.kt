package ru.deadsoftware.cavedroid.game.objects.container

import ru.deadsoftware.cavedroid.game.GameItemsHolder

class Chest(gameItemsHolder: GameItemsHolder) : Container(SIZE, gameItemsHolder) {

    override fun update(gameItemsHolder: GameItemsHolder) {
        // no-op
    }

    companion object {
        private const val SIZE = 27
    }
}