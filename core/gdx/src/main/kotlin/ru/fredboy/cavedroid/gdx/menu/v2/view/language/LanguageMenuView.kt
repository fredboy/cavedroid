package ru.fredboy.cavedroid.gdx.menu.v2.view.language

import com.badlogic.gdx.scenes.scene2d.Stage
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actors
import ktx.scene2d.textButton
import ru.fredboy.cavedroid.common.utils.startWithCapital
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.menuButtonsTable
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.onClickWithSound

@Scene2dDsl
fun Stage.languageMenuView(viewModel: LanguageMenuViewModel) = viewModel.also {
    actors {
        menuButtonsTable {
            viewModel.locales.map { locale ->
                textButton(locale.getDisplayLanguage(locale).startWithCapital(locale)) {
                    onClickWithSound(viewModel) {
                        viewModel.onLanguageSelect(locale)
                    }
                }

                row()
            }

            row()
                .bottom()

            textButton(viewModel.getLocalizedString("back")) {
                onClickWithSound(viewModel) { viewModel.onBackClick() }
            }
        }
    }
}
