package ru.fredboy.cavedroid.gdx.menu.v2.view.common

import ru.fredboy.cavedroid.common.api.SoundPlayer
import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.domain.assets.repository.FontTextureAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.UiSoundAssetsRepository
import javax.inject.Inject

@MenuScope
class BaseViewModelDependencies @Inject constructor(
    val fontAssetsRepository: FontTextureAssetsRepository,
    val uiSoundAssetsRepository: UiSoundAssetsRepository,
    val soundPlayer: SoundPlayer,
)
