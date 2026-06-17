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

// Comparing window references is safe even cross-origin (unlike reading
// properties off window.top). True when the page is embedded in an iframe,
// e.g. the game running on Yandex Games.
@JSBody(
    script = "return window.self !== window.top;",
)
private external fun isInIframe(): Boolean

@JSBody(
    params = ["listener"],
    script = """
        if (window.visualViewport) {
            window.visualViewport.addEventListener('resize', listener);
        }
    """,
)
private external fun addViewportResizeListener(listener: EventListener<Event>)

// Injects the @font-face rule and immediately asks the browser to fetch the
// font via the CSS Font Loading API, so it's cached well before the overlay is
// first shown. Without this the .ttf is only downloaded when the overlay first
// renders, leaving the field briefly unstyled (or blank, with font-display:
// block). document.fonts.load returns a promise we don't need to await.
@JSBody(
    params = ["css", "fontSpec"],
    script = """
        var s = document.createElement('style');
        s.textContent = css;
        document.head.appendChild(s);
        if (document.fonts && document.fonts.load) {
            document.fonts.load(fontSpec);
        }
    """,
)
private external fun injectStyleAndPreloadFont(css: String, fontSpec: String)

// LanaPixel.ttf lives at the webapp root (see html/src/main/resources/webapp/);
// the assets/textures/background.png path matches what the menu skin uses.
private const val OVERLAY_CSS = """
    @font-face {
        font-family: 'LanaPixel';
        src: url('LanaPixel.ttf') format('truetype');
        font-display: block;
    }
"""

// A font spec accepted by document.fonts.load(); the size is arbitrary, only
// the family matters for triggering the fetch. Matches the overlay's family.
private const val FONT_PRELOAD_SPEC = "32px 'LanaPixel'"

// When the keyboard is up and the visual viewport keeps at least this fraction
// of window.innerHeight, the scene2d field is likely still visible — keep the
// input invisible and let the user watch the in-game field update directly.
// Below this fraction (i.e. keyboard covers more than 40%) we promote to the
// styled overlay so they can see what they're typing.
private const val OVERLAY_VIEWPORT_FRACTION = 0.6

class WebInlineTextInput :
    InlineTextInput,
    SoftKeyboardObserver {

    override val isSupported: Boolean = true

    private val _isVisible = MutableStateFlow(false)
    override val isVisible: StateFlow<Boolean> = _isVisible

    private var container: HTMLElement? = null
    private var input: HTMLInputElement? = null
    private var doneButton: HTMLElement? = null
    private var valueChangedCallback: ((String, Int) -> Unit)? = null

    // True once we've observed the soft keyboard come up during the current
    // session — used to distinguish "keyboard slid out" from "keyboard never
    // came up yet" so the initial focus-in doesn't auto-blur itself.
    private var wasKeyboardVisible = false

    // Viewport height captured at focus time, before the soft keyboard opens.
    // The (non-iframe) resize listener judges keyboard visibility relative to
    // this baseline.
    private var baselineViewportHeight = 0.0

    // Inside a cross-origin iframe (e.g. the game embedded on Yandex Games) the
    // soft keyboard resizes only the top-level visual viewport, which we can't
    // observe from here, and navigator.virtualKeyboard is blocked by the default
    // permission policy. With no way to measure keyboard coverage we always show
    // the styled overlay while focused and rely on an explicit Done button (plus
    // Enter / blur / tap-outside) for dismissal.
    private val inIframe: Boolean by lazy { isInIframe() }

    init {
        // Constructed at game start (see WebLauncher) — register the overlay
        // font and start downloading it now so it's ready by the first trigger().
        injectStyleAndPreloadFont(OVERLAY_CSS, FONT_PRELOAD_SPEC)
    }

    override fun trigger(
        initialText: String,
        initialCursor: Int,
        buttonText: String,
        onValueChanged: (String, Int) -> Unit,
    ) {
        val element = input ?: createElements().also { input = it }
        doneButton?.textContent = buttonText
        valueChangedCallback = onValueChanged
        element.value = initialText
        if (inIframe) {
            // Keyboard coverage is unobservable in a cross-origin iframe, so show
            // the overlay up front and let the Done button dismiss it.
            applyOverlayMode()
        } else {
            // Capture the keyboard-down viewport height so the resize listener
            // can judge visibility relative to it. Measured before focus()
            // raises the keyboard.
            baselineViewportHeight = viewportHeight()
            // Start invisible; the resize listener promotes to the styled
            // overlay only if the keyboard ends up covering too much.
            applyInvisibleMode()
        }
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
        style.removeProperty("right")
        style.removeProperty("transform")
        containerEl.style.setProperty("display", "none")
        doneButton?.style?.setProperty("display", "none")

        _isVisible.value = false
    }

    private fun applyOverlayMode() {
        val inputEl = input ?: return
        val containerEl = container ?: return
        val offsetTop = viewportOffsetTop()
        val height = viewportHeight()
        // Anchor the row near the top of the viewport so the soft keyboard
        // (whose height we can't measure in an iframe) can't cover it. The input
        // and the Done button are both centered on this line via translateY(-50%)
        // so they stay vertically aligned with each other.
        val anchorY = offsetTop + 48.0

        val cstyle = containerEl.style
        cstyle.setProperty("display", "block")
        cstyle.setProperty("top", "${offsetTop}px")
        cstyle.setProperty("height", "${height}px")

        val istyle = inputEl.style
        istyle.setProperty("position", "fixed")
        istyle.setProperty("top", "${anchorY}px")
        istyle.setProperty("transform", "translateY(-50%)")
        istyle.setProperty("left", "16px")
        // Stretch from the left margin up to the space reserved for the Done
        // button; no explicit height so it wraps to a single content line.
        istyle.setProperty("right", "160px")
        istyle.removeProperty("width")
        istyle.removeProperty("height")
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

        doneButton?.let { btn ->
            btn.style.setProperty("display", "block")
            btn.style.setProperty("top", "${anchorY}px")
        }

        _isVisible.value = true
    }

    private fun isKeyboardLikelyVisible(): Boolean {
        // Treat the keyboard as visible when the visual viewport is meaningfully
        // shorter than the focus-time baseline — 100px guards against URL-bar
        // collapse/expand and minor viewport jitter on mobile browsers.
        return baselineViewportHeight - viewportHeight() > 100.0
    }

    private fun shouldShowOverlay(): Boolean {
        return viewportHeight() < OVERLAY_VIEWPORT_FRACTION * baselineViewportHeight
    }

    private fun createElements(): HTMLInputElement {
        // The @font-face was already injected (and the font fetched) in init.
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

        // Explicit dismissal affordance for the overlay. It matters most inside
        // an iframe, where the soft keyboard's back button neither blurs the
        // input nor reaches us as a viewport event, so there's no other way to
        // auto-close. The capture-phase blur listener below also blurs on any
        // tap whose target isn't the input (the button included); the click
        // handler is the explicit, keyboard-accessible path.
        val buttonEl = document.createElement("button") as HTMLElement
        buttonEl.textContent = "Done"
        val bstyle = buttonEl.style
        bstyle.setProperty("position", "fixed")
        bstyle.setProperty("right", "16px")
        // top is set per-overlay; translateY(-50%) keeps it vertically centered
        // on that anchor, matching the input.
        bstyle.setProperty("transform", "translateY(-50%)")
        bstyle.setProperty("display", "none")
        bstyle.setProperty("padding", "8px 24px")
        bstyle.setProperty("margin", "0")
        bstyle.setProperty("border", "none")
        bstyle.setProperty("background", "rgba(255, 255, 255, 0.9)")
        bstyle.setProperty("color", "#000000")
        bstyle.setProperty("font-family", "'LanaPixel', monospace")
        bstyle.setProperty("font-size", "28px")
        bstyle.setProperty("cursor", "pointer")
        bstyle.setProperty("z-index", "2147483647")
        buttonEl.addEventListener("click", EventListener<Event> { inputEl.blur() })
        doneButton = buttonEl

        // Both elements live directly on the body. The input is a sibling of
        // the backdrop, not a child, so toggling the backdrop's display can't
        // hide (and therefore unfocus) the input.
        document.body.appendChild(containerEl)
        document.body.appendChild(inputEl)
        document.body.appendChild(buttonEl)

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
                // In an iframe the overlay is driven by focus + the Done button;
                // the visual viewport never reflects the keyboard here, so don't
                // let stray resize events flip modes or blur.
                if (inIframe) return@EventListener
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
