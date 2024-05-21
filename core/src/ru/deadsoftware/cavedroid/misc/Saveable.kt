package ru.deadsoftware.cavedroid.misc

import ru.deadsoftware.cavedroid.game.model.dto.SaveDataDto

interface Saveable {
    fun getSaveData(): SaveDataDto
}
