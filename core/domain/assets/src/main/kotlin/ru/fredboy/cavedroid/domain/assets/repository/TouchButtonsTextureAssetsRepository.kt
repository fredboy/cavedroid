package ru.fredboy.cavedroid.domain.assets.repository

import ru.fredboy.cavedroid.domain.assets.model.TouchButton

abstract class TouchButtonsTextureAssetsRepository : TextureAssetsRepository() {

    abstract fun getTouchButtons(): Map<String, TouchButton>
}
