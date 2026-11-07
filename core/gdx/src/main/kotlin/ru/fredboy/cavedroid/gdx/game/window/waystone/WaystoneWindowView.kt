package ru.fredboy.cavedroid.gdx.game.window.waystone

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.I18NBundle
import ru.fredboy.cavedroid.common.api.InlineTextInput
import ru.fredboy.cavedroid.common.api.SoftKeyboardObserver
import ru.fredboy.cavedroid.domain.world.waystone.WaystoneTravelResult
import ru.fredboy.cavedroid.game.window.waystone.WaystoneDestination
import ru.fredboy.cavedroid.game.window.waystone.WaystoneWindowModel
import ru.fredboy.cavedroid.gdx.game.window.Scene2dGameWindow
import ru.fredboy.cavedroid.gdx.game.window.onChange
import kotlin.math.abs

class WaystoneWindowView(
    private val model: WaystoneWindowModel,
    skin: Skin,
    touch: Boolean,
    private val strings: I18NBundle,
    title: String,
    private val inlineTextInput: InlineTextInput,
    private val keyboard: SoftKeyboardObserver,
    private val close: () -> Unit,
    private val icon: Drawable,
) : Scene2dGameWindow(skin, touch, title, if (touch) 636f else 288f, if (touch) 316f else 242f, close) {
    private val labelStyle = if (touch) "touch" else "default"
    private val mutedStyle = if (touch) "touch-muted" else "muted"
    private val buttonStyle = if (touch) "touch" else "default"
    private val rowHeight = if (touch) 44f else 34f
    private var displayedRevision = -1
    private var page = 0
    private var renaming = false
    private var disposed = false
    private var field: TextField? = null
    private var error: Label? = null
    private var keyboardVisible = false
    private var scroll: ScrollPane? = null
    private var revealSelection = false
    private var chargeBar: ProgressBar? = null

    init {
        window.addListener(object : InputListener() {
            override fun keyDown(event: InputEvent, keycode: Int): Boolean {
                if (model.isCharging || model.failure != null) return false
                if (renaming) {
                    if (keycode == Input.Keys.ENTER) {
                        saveRename()
                        return true
                    }
                    return false
                }
                when (keycode) {
                    Input.Keys.ENTER -> {
                        if (model.canTravel) {
                            model.teleport()
                        }
                    }
                    Input.Keys.UP, Input.Keys.DOWN -> {
                        val index = model.destinations.indexOfFirst { it.id == model.selectedId }
                        val next = (index + if (keycode == Input.Keys.UP) -1 else 1).coerceIn(0, (model.destinations.size - 1).coerceAtLeast(0))
                        model.destinations.getOrNull(next)?.let { model.select(it.id) }
                        page = next / PAGE_SIZE
                        revealSelection = true
                    }
                    else -> return false
                }
                return true
            }
        })
        buildBrowser()
    }

    private fun text(key: String) = strings[key]
    private fun label(value: String, style: String = labelStyle) = Label(value, skin, style).apply { setEllipsis(true) }
    private fun smallLabel(value: String) = label(value, mutedStyle).apply {
        setFontScale(style.font.data.scaleX * if (touch) .65f else .7f)
    }
    private fun coordinates(value: WaystoneDestination) = strings.format("waystone_coordinates", value.x.toString(), value.y.toString())

    private fun button(caption: String, action: () -> Unit) = TextButton(caption, skin, buttonStyle).apply { onChange(action) }

    private fun buildBrowser() {
        content.clearChildren()
        field = null
        error = null
        displayedRevision = model.revision
        chargeBar = null
        if (model.isCharging) {
            val destination = model.selected
            content.add(label(strings.format("waystone_charging", destination?.name.orEmpty()))).growX().pad(8f)
            content.row()
            chargeBar = ProgressBar(0f, 1f, .001f, false, skin).apply { value = model.chargeProgress }
            content.add(chargeBar).growX().pad(8f)
            content.row()
            content.add(
                button(text("cancel")) {
                    model.cancelCharge()
                    buildBrowser()
                },
            ).growX()
                .height(if (touch) 44f else 24f).pad(8f)
            return
        }
        model.failure?.let { failure ->
            content.add(
                Label(failureText(failure), skin, labelStyle).apply {
                    setWrap(true)
                    setAlignment(Align.center)
                },
            )
                .width(if (touch) 590f else 250f).growY().pad(8f)
            content.row()
            content.add(
                button(text("done")) {
                    model.dismissFailure()
                    buildBrowser()
                },
            ).growX()
                .height(if (touch) 44f else 24f).pad(8f)
            return
        }
        val source = Table()
        source.add(Image(icon)).size(if (touch) 32f else 16f).padRight(8f)
        val name = Table()
        name.add(label(model.source.name)).growX().left()
        if (!touch) name.row()
        name.add(smallLabel(coordinates(model.source))).growX().left()
        source.add(name).growX().padRight(8f)
        source.add(button(text("rename")) { beginRename() }).minWidth(if (touch) 104f else 56f).height(if (touch) 44f else 24f)
        content.add(source).growX().pad(0f, 6f, 4f, 6f).colspan(2)
        content.row()

        val listPanel = Table()
        listPanel.add(smallLabel(text("waystone_destinations"))).growX().left().padBottom(4f)
        listPanel.row()
        val rows = Table()
        var selectedRow: Button? = null
        if (model.destinations.isEmpty()) {
            rows.add(
                Label(text("waystone_empty"), skin, mutedStyle).apply {
                    setWrap(true)
                    setAlignment(Align.center)
                },
            )
                .width(if (touch) 310f else 240f).pad(8f).growY()
        } else {
            val values = if (touch) model.destinations.drop(page * PAGE_SIZE).take(PAGE_SIZE) else model.destinations
            values.forEach { destination ->
                val row = Button(skin, if (touch) "destination-touch" else "destination").apply {
                    this.name = "waystone-destination-${destination.id}"
                    isChecked = destination.id == model.selectedId
                    add(Image(icon)).size(if (touch) 24f else 16f).padRight(8f)
                    val description = Table()
                    description.add(label(destination.name)).growX().left()
                    description.row()
                    description.add(smallLabel(coordinates(destination))).growX().left()
                    add(description).growX()
                    onChange {
                        model.select(destination.id)
                        isChecked = model.selectedId == destination.id
                    }
                }
                rows.add(row).growX().height(rowHeight).padBottom(if (touch) 0f else 2f)
                rows.row()
                if (destination.id == model.selectedId) selectedRow = row
            }
        }
        rows.top()
        val previousScroll = scroll?.scrollY ?: 0f
        scroll = ScrollPane(rows, skin, if (touch) "touch" else "default").apply {
            setScrollingDisabled(true, touch)
            setFadeScrollBars(false)
            setOverscroll(false, false)
            this.name = "waystone-destinations"
        }
        listPanel.add(scroll).grow()
        content.add(listPanel).grow().pad(0f, 6f, 4f, 6f).width(if (touch) 340f else 264f)
        if (touch) {
            val details = Table().apply { top() }
            details.add(smallLabel(text("waystone_destination"))).growX().left().padBottom(10f)
            details.row()
            model.selected?.let { selected ->
                details.add(label(selected.name, "touch-title")).growX().left().padBottom(12f)
                details.row()
                details.add(label(coordinates(selected))).growX().left().padBottom(12f)
                details.row()
                val distance = (selected.distance ?: abs(selected.x.toLong() - model.source.x.toLong())).toString()
                details.add(label(strings.format("waystone_distance", distance), mutedStyle)).growX().left()
            }
            content.add(details).growY().width(242f).pad(0f, 6f, 4f, 6f)
        }
        content.row()
        if (touch) {
            val paging = Table()
            val pages = ((model.destinations.size + PAGE_SIZE - 1) / PAGE_SIZE).coerceAtLeast(1)
            paging.add(
                ImageButton(skin, "previous-touch").apply {
                    isDisabled = page == 0
                    onChange {
                        page--
                        buildBrowser()
                    }
                },
            ).size(44f)
            paging.add(label("${page + 1} / $pages", mutedStyle).apply { setAlignment(Align.center) }).growX()
            paging.add(
                ImageButton(skin, "next-touch").apply {
                    isDisabled = page >= pages - 1
                    onChange {
                        page++
                        buildBrowser()
                    }
                },
            ).size(44f)
            content.add(paging).growX().pad(0f, 6f, 0f, 6f)
        }
        val teleport = TextButton(text("waystone_teleport"), skin, if (touch) "teleport-touch" else "teleport").apply {
            isDisabled = !model.canTravel
            onChange {
                if (model.canTravel) {
                    model.teleport()
                }
            }
        }
        content.add(teleport).growX().height(if (touch) 44f else 24f).pad(0f, 6f, 2f, 6f)
        window.stage?.let { stage ->
            root.validate()
            scroll?.validate()
            scroll?.scrollY = previousScroll
            if (revealSelection && !touch) {
                selectedRow?.let { row -> scroll?.scrollTo(row.x, row.y, row.width, row.height) }
            }
            scroll?.updateVisualScroll()
            stage.keyboardFocus = window
            stage.scrollFocus = scroll
        }
        revealSelection = false
    }

    override fun onShow(stage: Stage) {
        super.onShow(stage)
        stage.scrollFocus = scroll
    }

    private fun beginRename() {
        renaming = true
        content.clearChildren()
        val nameField = TextField(model.source.name, skin, buttonStyle).apply {
            messageText = text("waystone_name")
            maxLength = WaystoneWindowModel.MAX_NAME_LENGTH
            selectAll()
            setFocusTraversal(false)
            setTextFieldListener { textField, character ->
                if (renaming && field === textField && (character == '\n' || character == '\r')) saveRename()
            }
        }
        field = nameField
        nameField.onscreenKeyboard = object : TextField.OnscreenKeyboard {
            override fun show(textField: TextField) {
                if (inlineTextInput.isSupported) {
                    inlineTextInput.trigger(textField.text, textField.cursorPosition, text("done")) { value, cursor ->
                        if (!disposed && renaming && field === textField) {
                            textField.text = value
                            textField.cursorPosition = cursor.coerceIn(0, textField.text.length)
                        }
                    }
                } else {
                    Gdx.input.setOnscreenKeyboardVisible(true)
                }
            }

            override fun close() {
                Gdx.input.setOnscreenKeyboardVisible(false)
            }
        }
        buildRename()
        window.stage?.keyboardFocus = nameField
    }

    private fun buildRename() {
        val nameField = field ?: return
        content.clearChildren()
        content.add(label(text("waystone_name"))).growX().left().colspan(2).pad(4f)
        content.row()
        content.add(nameField).growX().height(if (touch) 44f else 26f).colspan(2).pad(4f)
        if (keyboardVisible) {
            window.stage?.keyboardFocus = nameField
            return
        }
        content.row()
        error = Label("", skin, mutedStyle)
        content.add(error).growX().colspan(2).pad(2f)
        content.row()
        content.add(button(text("cancel")) { cancelRename() }).growX().height(if (touch) 44f else 24f).pad(4f)
        content.add(button(text("confirm")) { saveRename() }).growX().height(if (touch) 44f else 24f).pad(4f)
        window.stage?.keyboardFocus = nameField
    }

    private fun saveRename() {
        val value = field?.text ?: return
        if (!model.rename(value)) {
            error?.setText(model.failure?.let(::failureText) ?: text("waystone_name_invalid"))
            return
        }
        cancelRename()
    }

    private fun cancelRename() {
        renaming = false
        Gdx.input.setOnscreenKeyboardVisible(false)
        keyboardVisible = false
        compactForKeyboard(false, 144f)
        buildBrowser()
    }

    override fun onBack(): Boolean {
        if (renaming) {
            cancelRename()
            return true
        }
        if (model.isCharging) {
            model.cancelCharge()
            buildBrowser()
            return true
        }
        if (model.failure != null) {
            model.dismissFailure()
            buildBrowser()
            return true
        }
        return false
    }

    override fun update(delta: Float) {
        model.update(delta)
        if (model.completed) {
            close()
            return
        }
        chargeBar?.value = model.chargeProgress
        if (renaming) {
            val visible = touch && keyboard.isVisible.value
            if (keyboardVisible != visible) {
                keyboardVisible = visible
                compactForKeyboard(visible, 144f)
                buildRename()
            }
        } else if (model.revision != displayedRevision) {
            page = page.coerceAtMost(((model.destinations.size - 1) / PAGE_SIZE).coerceAtLeast(0))
            buildBrowser()
        }
    }

    override fun dispose() {
        disposed = true
        field = null
    }

    private fun failureText(result: WaystoneTravelResult): String = text(
        when (result) {
            WaystoneTravelResult.SOURCE_UNAVAILABLE -> "waystone_source_unavailable"
            WaystoneTravelResult.DESTINATION_UNAVAILABLE -> "waystone_destination_unavailable"
            WaystoneTravelResult.DESTINATION_BLOCKED -> "waystone_destination_blocked"
            WaystoneTravelResult.INTERRUPTED -> "waystone_interrupted"
            WaystoneTravelResult.SUCCESS -> "done"
        },
    )

    companion object {
        private const val PAGE_SIZE = 3
    }
}
