package ru.fredboy.cavedroid.gdx.menu.v2.view.assetpacks

import ru.fredboy.cavedroid.common.api.AssetPackPlatform
import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.common.mvvm.NavBackStack
import ru.fredboy.cavedroid.common.mvvm.ViewModelProvider
import ru.fredboy.cavedroid.domain.assets.pack.AssetPackRepository
import ru.fredboy.cavedroid.gdx.menu.v2.di.BindViewModelProvider
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModelDependencies
import javax.inject.Inject

@MenuScope
@BindViewModelProvider
class AssetPacksViewModelProvider @Inject constructor(
    private val repository: AssetPackRepository,
    private val platform: AssetPackPlatform,
    private val dependencies: BaseViewModelDependencies,
) : ViewModelProvider<AssetPacksNavKey, AssetPacksViewModel> {
    override val viewModelClass = AssetPacksViewModel::class
    override fun get(navKey: AssetPacksNavKey, navBackStack: NavBackStack) = AssetPacksViewModel(repository, platform, navBackStack, dependencies)
}
