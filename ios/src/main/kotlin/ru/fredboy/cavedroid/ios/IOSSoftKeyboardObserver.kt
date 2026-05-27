package ru.fredboy.cavedroid.ios

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.robovm.apple.foundation.NSNotification
import org.robovm.apple.foundation.NSNotificationCenter
import org.robovm.apple.foundation.NSOperationQueue
import org.robovm.objc.block.VoidBlock1
import ru.fredboy.cavedroid.common.api.SoftKeyboardObserver

class IOSSoftKeyboardObserver : SoftKeyboardObserver {

    private val _isVisible = MutableStateFlow(false)
    override val isVisible: StateFlow<Boolean> = _isVisible

    init {
        val center = NSNotificationCenter.getDefaultCenter()
        val queue = NSOperationQueue.getMainQueue()
        center.addObserver(
            WILL_SHOW_NOTIFICATION,
            null,
            queue,
            VoidBlock1<NSNotification> { _isVisible.value = true },
        )
        center.addObserver(
            WILL_HIDE_NOTIFICATION,
            null,
            queue,
            VoidBlock1<NSNotification> { _isVisible.value = false },
        )
    }

    companion object {
        // UIKit notification name constants. RoboVM doesn't expose the typed
        // accessor on every cocoatouch version, so we name them directly.
        private const val WILL_SHOW_NOTIFICATION = "UIKeyboardWillShowNotification"
        private const val WILL_HIDE_NOTIFICATION = "UIKeyboardWillHideNotification"
    }
}
