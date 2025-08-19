package ru.fredboy.cavedroid.gdx.menu.v2

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable
import com.badlogic.gdx.utils.Align
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.actors
import ktx.scene2d.image
import ktx.scene2d.label
import ktx.scene2d.table
import ru.fredboy.cavedroid.common.CaveDroidConstants
import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.domain.assets.usecase.GetFontUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.menu.model.MenuButton
import ru.fredboy.cavedroid.domain.menu.repository.MenuButtonRepository
import ru.fredboy.cavedroid.gdx.menu.action.IMenuAction
import ru.fredboy.cavedroid.gdx.menu.option.bool.IMenuBooleanOption
import ru.fredboy.cavedroid.gdx.menu.option.numerical.IMenuNumericalOption
import javax.inject.Inject

@MenuScope
class MenuUi @Inject constructor(
    private val appCtx: ApplicationContextRepository,
    private val menuButtonRepository: MenuButtonRepository,
    private val actions: Map<String, @JvmSuppressWildcards IMenuAction>,
    private val booleanOptions: Map<String, @JvmSuppressWildcards IMenuBooleanOption>,
    private val numericalOptions: Map<String, @JvmSuppressWildcards IMenuNumericalOption>,
    private val getFont: GetFontUseCase,
    private val getRegion: GetTextureRegionByNameUseCase,
) {
    fun resetMenuTo(key: String) {
        menuButtonRepository.setCurrentMenu(key)
    }

    fun build(stage: Stage) {
        Scene2DSkin.defaultSkin = Skin(Gdx.files.internal("skin/skin"))

        stage.actors {
            table {
                setFillParent(true)
                background("background")

                table {
                    setFillParent(true)
                    image("gamelogo")
                    rebuildButtons(this)
                    row()
                    table {
                        label("CaveDroid ${CaveDroidConstants.VERSION}",)
                    }
                }
            }
        }
    }

    fun onResize() {
        // No special work required; Table will relayout automatically.
    }

    fun dispose() {
    }

    // --- Helpers ---

    private fun rebuildButtons(content: Table) {
        // Clear previous rows except logo at the top (first row)
        // Easiest: clear and re-add logo if you prefer. Here we clear and rebuild all:
        content.clearChildren()

        // Re-add logo if present
        getRegion["gamelogo"]?.let { logoRegion ->
            val logo = Image(TextureRegionDrawable(logoRegion))
            content.add(logo).padBottom(16f).row()
        }

        val buttons = menuButtonRepository.getCurrentMenuButtons()?.values.orEmpty()

        buttons.forEachIndexed { index, button ->
            when (button) {
                is MenuButton.Simple -> {
                    val b = TextButton(button.label, Scene2DSkin.defaultSkin, "default").apply {
//                        isDisabled = !(button.isEnabled && (actions[button.actionKey]?.canPerform() == true))
                    }
                    b.addListener(
                        click { _ ->
                            val a = actions[button.actionKey]
                            if (a?.canPerform() == true) a.perform()
                        },
                    )
                    content.add(b).width(220f).height(28f).row()
                }

                is MenuButton.BooleanOption -> {
                    val labelText = booleanLabel(button)
                    val b = TextButton(labelText, Scene2DSkin.defaultSkin, "default").apply {
                        isDisabled = !button.isEnabled
                    }
                    b.addListener(
                        click { _ ->
                            button.optionKeys.forEach { key ->
                                booleanOptions[key]?.toggleOption()
                            }
                            b.setText(booleanLabel(button))
                        },
                    )
                    content.add(b).width(220f).height(28f).row()
                }

                is MenuButton.NumericalOption -> {
                    val labelText = numericalLabel(button)
                    val b = TextButton(labelText, Scene2DSkin.defaultSkin, "default").apply {
                        isDisabled = !button.isEnabled
                    }
                    b.addListener(
                        click { _ ->
                            button.optionKeys.forEach { key ->
                                numericalOptions[key]?.setNextOption()
                            }
                            b.setText(numericalLabel(button))
                        },
                    )
                    content.add(b).width(220f).height(28f).row()
                }
            }
        }
    }

    private fun booleanLabel(button: MenuButton.BooleanOption): String {
        val args = button.optionKeys.map { key ->
            booleanOptions[key]?.getOption().toString()
        }.toTypedArray()
        return String.format(button.label, *args)
    }

    private fun numericalLabel(button: MenuButton.NumericalOption): String {
        val args = button.optionKeys.mapNotNull { key ->
            numericalOptions[key]?.getOption()
        }.toTypedArray()
        return String.format(button.label, *args)
    }

    private inline fun click(crossinline body: (InputEventWrapper) -> Unit) = object : ClickListener() {
        override fun clicked(event: InputEvent?, x: Float, y: Float) {
            body(InputEventWrapper(event))
        }
    }

    class InputEventWrapper(val raw: InputEvent?)
}
