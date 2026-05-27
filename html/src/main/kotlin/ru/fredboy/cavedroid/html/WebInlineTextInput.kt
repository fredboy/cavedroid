package ru.fredboy.cavedroid.html

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.teavm.jso.JSBody
import org.teavm.jso.browser.Window
import org.teavm.jso.dom.events.Event
import org.teavm.jso.dom.events.EventListener
import org.teavm.jso.dom.html.HTMLElement
import org.teavm.jso.dom.html.HTMLInputElement
import ru.fredboy.cavedroid.common.api.InlineTextInput
import ru.fredboy.cavedroid.common.api.SoftKeyboardObserver

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

@JSBody(
    params = ["event"],
    script = "return event.key;",
)
private external fun eventKey(event: Event): String

@JSBody(
    params = ["event"],
    script = "return event.isComposing === true;",
)
private external fun isComposing(event: Event): Boolean

@JSBody(
    script = "return window.visualViewport ? window.visualViewport.height : window.innerHeight;",
)
private external fun viewportHeight(): Double

@JSBody(
    script = "return window.visualViewport ? window.visualViewport.offsetTop : 0;",
)
private external fun viewportOffsetTop(): Double

@JSBody(
    script = "return window.innerHeight;",
)
private external fun windowInnerHeight(): Double

@JSBody(
    params = ["listener"],
    script = """
        if (window.visualViewport) {
            window.visualViewport.addEventListener('resize', listener);
        }
    """,
)
private external fun addViewportResizeListener(listener: EventListener<Event>)

@JSBody(
    params = ["css"],
    script = """
        var s = document.createElement('style');
        s.textContent = css;
        document.head.appendChild(s);
    """,
)
private external fun injectStyle(css: String)

// LanaPixel.ttf lives at the webapp root (see html/src/main/resources/webapp/);
// the assets/textures/background.png path matches what the menu skin uses.
private const val OVERLAY_CSS = """
    @font-face {
        font-family: 'LanaPixel';
        src: url('LanaPixel.ttf') format('truetype');
        font-display: block;
    }
"""

// When the keyboard is up and the visual viewport keeps at least this fraction
// of window.innerHeight, the scene2d field is likely still visible — keep the
// input invisible and let the user watch the in-game field update directly.
// Below this fraction (i.e. keyboard covers more than 40%) we promote to the
// styled overlay so they can see what they're typing.
private const val OVERLAY_VIEWPORT_FRACTION = 0.6

class WebInlineTextInput : InlineTextInput, SoftKeyboardObserver {

    override val isSupported: Boolean = true

    private val _isVisible = MutableStateFlow(false)
    override val isVisible: StateFlow<Boolean> = _isVisible

    private var container: HTMLElement? = null
    private var input: HTMLInputElement? = null
    private var valueChangedCallback: ((String, Int) -> Unit)? = null

    // True once we've observed the soft keyboard come up during the current
    // session — used to distinguish "keyboard slid out" from "keyboard never
    // came up yet" so the initial focus-in doesn't auto-blur itself.
    private var wasKeyboardVisible = false

    override fun trigger(
        initialText: String,
        initialCursor: Int,
        onValueChanged: (String, Int) -> Unit,
    ) {
        val element = input ?: createElements().also { input = it }
        valueChangedCallback = onValueChanged
        element.value = initialText
        // Always start in invisible mode; the resize listener promotes to the
        // styled overlay only if the keyboard ends up covering too much.
        applyInvisibleMode()
        element.blur()
        element.focus()
        setInputSelection(element, initialCursor, initialCursor)
    }

    private fun applyInvisibleMode() {
        val inputEl = input ?: return
        val containerEl = container ?: return
        val style = inputEl.style
        style.setProperty("position", "fixed")
        style.setProperty("top", "0")
        style.setProperty("left", "0")
        style.setProperty("width", "1px")
        style.setProperty("height", "1px")
        style.setProperty("padding", "0")
        style.setProperty("margin", "0")
        style.setProperty("border", "none")
        style.setProperty("background", "transparent")
        style.setProperty("color", "transparent")
        style.setProperty("caret-color", "transparent")
        style.setProperty("opacity", "0")
        style.setProperty("outline", "none")
        style.setProperty("pointer-events", "none")
        // iOS Safari auto-zooms when an input with font-size < 16px is focused.
        style.setProperty("font-size", "16px")
        style.setProperty("z-index", "2147483647")
        style.removeProperty("font-family")
        style.removeProperty("text-align")
        containerEl.style.setProperty("display", "none")

        _isVisible.value = false
    }

    private fun applyOverlayMode() {
        val inputEl = input ?: return
        val containerEl = container ?: return
        val offsetTop = viewportOffsetTop()
        val height = viewportHeight()

        val cstyle = containerEl.style
        cstyle.setProperty("display", "block")
        cstyle.setProperty("top", "${offsetTop}px")
        cstyle.setProperty("height", "${height}px")

        val istyle = inputEl.style
        istyle.setProperty("position", "fixed")
        istyle.setProperty("top", "${offsetTop}px")
        istyle.setProperty("left", "20%")
        istyle.setProperty("width", "60%")
        istyle.setProperty("height", "${height}px")
        istyle.setProperty("box-sizing", "border-box")
        istyle.setProperty("padding", "16px")
        istyle.setProperty("margin", "0")
        istyle.setProperty("border", "none")
        istyle.setProperty("background", "rgba(0, 0, 0, 0.5)")
        istyle.setProperty("color", "#ffffff")
        istyle.setProperty("caret-color", "#ffffff")
        istyle.setProperty("opacity", "1")
        istyle.setProperty("pointer-events", "auto")
        istyle.setProperty("font-family", "'LanaPixel', monospace")
        istyle.setProperty("font-size", "32px")
        istyle.setProperty("outline", "none")
        istyle.setProperty("text-align", "left")
        // Input sits above the backdrop container.
        istyle.setProperty("z-index", "2147483647")

        _isVisible.value = true
    }

    private fun isKeyboardLikelyVisible(): Boolean {
        // Treat the keyboard as visible when the visual viewport is meaningfully
        // shorter than the window — 100px guards against URL-bar collapse/expand
        // and minor viewport jitter on mobile browsers.
        return windowInnerHeight() - viewportHeight() > 100.0
    }

    private fun shouldShowOverlay(): Boolean {
        return viewportHeight() < OVERLAY_VIEWPORT_FRACTION * windowInnerHeight()
    }

    private fun createElements(): HTMLInputElement {
        injectStyle(OVERLAY_CSS)

        val document = Window.current().document

        val containerEl = document.createElement("div") as HTMLElement
        val cstyle = containerEl.style
        cstyle.setProperty("position", "fixed")
        cstyle.setProperty("top", "0")
        cstyle.setProperty("left", "0")
        cstyle.setProperty("right", "0")
        cstyle.setProperty("width", "100%")
        cstyle.setProperty("margin", "0")
        cstyle.setProperty("padding", "0")
        cstyle.setProperty("display", "none")
        cstyle.setProperty("background-image", "url('assets/textures/background.png')")
        cstyle.setProperty("background-repeat", "repeat")
        // Match the menu skin's nearest-neighbor scaling so the tiled texture
        // stays crisp on high-DPI screens.
        cstyle.setProperty("image-rendering", "pixelated")
        // One below the input so the input draws on top.
        cstyle.setProperty("z-index", "2147483646")
        container = containerEl

        val inputEl = document.createElement("input") as HTMLInputElement
        inputEl.type = "text"
        inputEl.setAttribute("autocomplete", "off")
        inputEl.setAttribute("autocorrect", "off")
        inputEl.setAttribute("autocapitalize", "off")
        inputEl.setAttribute("spellcheck", "false")
        input = inputEl

        // Mobile virtual keyboards only fire `keydown` for special keys (Backspace,
        // Enter) — never for characters. The reliable signal is the `input` event,
        // so forward its value and caret position to the scene2d field.
        inputEl.addEventListener(
            "input",
            EventListener<Event> {
                valueChangedCallback?.invoke(inputEl.value, getInputSelectionStart(inputEl))
            },
        )

        // Blur returns the element to the invisible/dismissed state. The state
        // flag is reset so the next trigger() starts fresh.
        inputEl.addEventListener(
            "blur",
            EventListener<Event> {
                applyInvisibleMode()
                wasKeyboardVisible = false
            },
        )

        // Block libGDX from also processing the keystrokes that the input
        // already absorbs — otherwise scene2d would react to keys independently
        // (e.g., on desktop where keydown fires for chars). Enter on the IME
        // action ("Done"/"Go"/"Enter") commits the input by blurring it.
        // isComposing guards against blurring during IME composition (CJK).
        val stopProp = EventListener<Event> { it.stopPropagation() }
        inputEl.addEventListener(
            "keydown",
            EventListener<Event> { event ->
                event.stopPropagation()
                if (eventKey(event) == "Enter" && !isComposing(event)) {
                    event.preventDefault()
                    inputEl.blur()
                }
            },
        )
        inputEl.addEventListener("keypress", stopProp)
        inputEl.addEventListener("keyup", stopProp)

        // Both elements live directly on the body. The input is a sibling of
        // the backdrop, not a child, so toggling the backdrop's display can't
        // hide (and therefore unfocus) the input.
        document.body.appendChild(containerEl)
        document.body.appendChild(inputEl)

        applyInvisibleMode()

        // Mobile browsers auto-raise the keyboard for a still-focused input on
        // the next user gesture. Blur in the capture phase so the input is
        // unfocused before libGDX dispatches the touch; if the tap lands on
        // the scene2d field, its onClick refocuses us via trigger(). Skip when
        // the tap is on the input itself so it doesn't blur-then-refocus; taps
        // on the container backdrop should blur (i.e. dismiss the overlay).
        val blurListener = EventListener<Event> { event ->
            if (event.target !== inputEl) {
                inputEl.blur()
            }
        }
        document.addEventListener("touchstart", blurListener, true)
        document.addEventListener("mousedown", blurListener, true)

        // visualViewport.resize fires when the soft keyboard slides in or out.
        // Each time it fires while we're focused: re-evaluate which mode we
        // should be in (invisible if there's plenty of room, styled overlay
        // otherwise) and update its bounds. When the keyboard slides back down
        // without the user tapping anywhere else (e.g. Android back button),
        // blur to dismiss.
        addViewportResizeListener(
            EventListener<Event> {
                if (isKeyboardLikelyVisible()) {
                    wasKeyboardVisible = true
                    if (shouldShowOverlay()) {
                        applyOverlayMode()
                    } else {
                        applyInvisibleMode()
                    }
                } else if (wasKeyboardVisible) {
                    wasKeyboardVisible = false
                    applyInvisibleMode()
                    inputEl.blur()
                }
            },
        )

        return inputEl
    }
}
