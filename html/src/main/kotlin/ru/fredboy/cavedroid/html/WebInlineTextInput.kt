package ru.fredboy.cavedroid.html

import org.teavm.jso.JSBody
import org.teavm.jso.browser.Window
import org.teavm.jso.dom.events.Event
import org.teavm.jso.dom.events.EventListener
import org.teavm.jso.dom.html.HTMLInputElement
import ru.fredboy.cavedroid.common.api.InlineTextInput

@JSBody(
    params = ["el", "start", "end"],
    script = "el.setSelectionRange(start, end);",
)
private external fun setInputSelection(el: HTMLInputElement, start: Int, end: Int)

@JSBody(
    params = ["el"],
    script = "return el.selectionStart != null ? el.selectionStart : el.value.length;",
)
private external fun getInputSelectionStart(el: HTMLInputElement): Int

class WebInlineTextInput : InlineTextInput {

    override val isSupported: Boolean = true

    private var input: HTMLInputElement? = null
    private var valueChangedCallback: ((String, Int) -> Unit)? = null

    override fun trigger(
        initialText: String,
        initialCursor: Int,
        onValueChanged: (String, Int) -> Unit,
    ) {
        val element = input ?: createInput().also { input = it }
        valueChangedCallback = onValueChanged
        element.value = initialText
        element.blur()
        element.focus()
        setInputSelection(element, initialCursor, initialCursor)
    }

    private fun createInput(): HTMLInputElement {
        val document = Window.current().document
        val element = document.createElement("input") as HTMLInputElement
        element.type = "text"

        val style = element.style
        style.setProperty("position", "fixed")
        style.setProperty("top", "0")
        style.setProperty("left", "0")
        style.setProperty("width", "1px")
        style.setProperty("height", "1px")
        style.setProperty("opacity", "0")
        style.setProperty("pointer-events", "none")
        // iOS Safari auto-zooms when an input with font-size < 16px is focused.
        style.setProperty("font-size", "16px")

        // Mobile virtual keyboards only fire `keydown` for special keys (Backspace,
        // Enter) — never for characters. The reliable signal is the `input` event,
        // so forward its value and caret position to the scene2d field.
        element.addEventListener(
            "input",
            EventListener<Event> {
                valueChangedCallback?.invoke(element.value, getInputSelectionStart(element))
            },
        )

        // Block libGDX from also processing the keystrokes that the invisible input
        // already absorbs — otherwise scene2d would react to keys independently
        // (e.g., on desktop where keydown fires for chars).
        val stopProp = EventListener<Event> { it.stopPropagation() }
        element.addEventListener("keydown", stopProp)
        element.addEventListener("keypress", stopProp)
        element.addEventListener("keyup", stopProp)

        document.body.appendChild(element)

        // Mobile browsers auto-raise the keyboard for a still-focused input on the
        // next user gesture. Blur in the capture phase so the input is unfocused
        // before libGDX dispatches the touch; if the tap lands on the scene2d
        // field, its onClick refocuses us via trigger().
        val blurListener = EventListener<Event> { element.blur() }
        document.addEventListener("touchstart", blurListener, true)
        document.addEventListener("mousedown", blurListener, true)

        return element
    }
}
