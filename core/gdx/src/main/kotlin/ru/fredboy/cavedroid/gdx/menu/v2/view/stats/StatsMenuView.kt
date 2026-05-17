package ru.fredboy.cavedroid.gdx.menu.v2.view.stats

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable
import com.badlogic.gdx.utils.Align
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actors
import ktx.scene2d.label
import ktx.scene2d.scrollPane
import ktx.scene2d.table
import ktx.scene2d.textButton
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.onClickWithSound

@Scene2dDsl
suspend fun Stage.statsMenuView(viewModel: StatsMenuViewModel) = viewModel.also {
    viewModel.stateFlow.collect { state ->
        actors {
            table {
                setFillParent(true)
                background(
                    TiledDrawable(
                        TextureRegionDrawable(
                            skin.getRegion("background"),
                        ),
                    ),
                )
                pad(8f)

                scrollPane {
                    label(buildContent(viewModel, state)) { wrap = true }
                }.cell(
                    expand = true,
                    fill = true,
                    align = Align.center,
                )

                row().bottom()

                textButton(viewModel.getLocalizedString("back")) {
                    onClickWithSound(viewModel) { viewModel.onBackClick() }
                }.cell(
                    width = 600f,
                    height = 60f,
                    pad = 16f,
                )
            }
        }
    }
}

private fun buildContent(viewModel: StatsMenuViewModel, state: StatsMenuState): String {
    val sb = StringBuilder()
    sb.append(state.text)
    if (state.showLeaderboards) {
        sb.append("\n\n")
        sb.append(viewModel.getLocalizedString("statsLeaderboardsHeader", fallback = "Leaderboards"))
        sb.append("\n")
        if (state.showSignInHint) {
            sb.append(viewModel.getLocalizedString("statsLeaderboardSignInHint", fallback = "Sign in to see your rank"))
        } else if (state.leaderboardLines.isEmpty()) {
            sb.append(viewModel.getLocalizedString("loading"))
        } else {
            sb.append(state.leaderboardLines.joinToString("\n"))
        }
    }
    return sb.toString()
}
