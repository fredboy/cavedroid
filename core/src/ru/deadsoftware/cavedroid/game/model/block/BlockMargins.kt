package ru.deadsoftware.cavedroid.game.model.block

data class BlockMargins(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
) {

    init {
        assert(left + right < 16)
        assert(top + bottom < 16)
    }

}
