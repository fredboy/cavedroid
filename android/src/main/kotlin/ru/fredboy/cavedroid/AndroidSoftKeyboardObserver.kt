package ru.fredboy.cavedroid

import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.fredboy.cavedroid.common.api.SoftKeyboardObserver

class AndroidSoftKeyboardObserver(activity: Activity) : SoftKeyboardObserver {

    private val _isVisible = MutableStateFlow(false)
    override val isVisible: StateFlow<Boolean> = _isVisible

    private val rootView: View = activity.window.decorView.findViewById(android.R.id.content)

    private val visibleRect = Rect()

    private val layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        rootView.getWindowVisibleDisplayFrame(visibleRect)
        val rootHeight = rootView.rootView.height
        if (rootHeight <= 0) return@OnGlobalLayoutListener
        // Anything more than 15% of the window height being clipped from the
        // visible frame is treated as the IME (system bars alone never get
        // close to that). Robust across API 23-36 without inset APIs that are
        // unreliable on older devices.
        val keyboardHeight = rootHeight - visibleRect.bottom
        val visible = keyboardHeight > rootHeight * KEYBOARD_HEIGHT_FRACTION
        if (_isVisible.value != visible) {
            _isVisible.value = visible
        }
    }

    init {
        rootView.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
    }

    companion object {
        private const val KEYBOARD_HEIGHT_FRACTION = 0.15
    }
}
