package ru.deadsoftware.cavedroid.game.render

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import ru.deadsoftware.cavedroid.game.GameScope

@Module
object RenderModule {

    @Binds
    @IntoSet
    @GameScope
    fun bindBackGroundBlocksRenderer(renderer: BackgroundBlocksRenderer): IGameRenderer = renderer

    @Binds
    @IntoSet
    @GameScope
    fun bindForegroundBlocksRenderer(renderer: ForegroundBlocksRenderer): IGameRenderer = renderer

    @Binds
    @IntoSet
    @GameScope
    fun bindMobsRenderer(renderer: MobsRenderer): IGameRenderer = renderer

    @Binds
    @IntoSet
    @GameScope
    fun bindDropsRenderer(renderer: DropsRenderer): IGameRenderer = renderer

    @Binds
    @IntoSet
    @GameScope
    fun bindHudRenderer(renderer: HudRenderer): IGameRenderer = renderer

    @Binds
    @IntoSet
    @GameScope
    fun bindWindowsRenderer(renderer: WindowsRenderer): IGameRenderer = renderer

    @Binds
    @IntoSet
    @GameScope
    fun bindTouchControlsRenderer(renderer: TouchControlsRenderer): IGameRenderer = renderer

    @Binds
    @IntoSet
    @GameScope
    fun bindDebugRenderer(renderer: DebugRenderer): IGameRenderer = renderer

//    @Provides
//    @GameScope
//    fun provideGameRenderers(renderers: Set<@JvmSuppressWildcards IGameRenderer>): List<IGameRenderer> {
//        return renderers.asSequence()
//            .sortedWith(Comparator.comparingInt(IGameRenderer::renderLayer))
//            .toList()
//    }

}
