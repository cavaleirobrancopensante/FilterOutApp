package io.github.cavaleirobrancopensante.filterout

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.os.CountDownTimer
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button

/**
 * The full-screen cover shown when a banned word is found. It sits above every
 * other app and swallows all touches until the person taps "Leave".
 */
class BlockOverlay(
    private val service: AccessibilityService,
    private val onLeave: () -> Unit
) {
    private val windowManager = service.getSystemService(WindowManager::class.java)
    private var view: View? = null
    private var timer: CountDownTimer? = null

    val isShowing: Boolean get() = view != null

    fun show() {
        if (view != null) return
        val themed = ContextThemeWrapper(service, R.style.Theme_FilterOut)
        val root = LayoutInflater.from(themed).inflate(R.layout.overlay_block, null)
        val leave = root.findViewById<Button>(R.id.leave_button)

        // A short pause before you can leave: the whole point is to think twice.
        leave.isEnabled = false
        timer = object : CountDownTimer(PAUSE_MS, 1000) {
            override fun onTick(msLeft: Long) {
                leave.text = service.getString(R.string.leave_in, (msLeft + 999) / 1000)
            }
            override fun onFinish() {
                leave.isEnabled = true
                leave.setText(R.string.leave)
            }
        }.start()
        leave.setOnClickListener { onLeave() }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.OPAQUE
        )
        windowManager.addView(root, params)
        view = root
    }

    fun hide() {
        timer?.cancel()
        timer = null
        view?.let { runCatching { windowManager.removeView(it) } }
        view = null
    }

    private companion object {
        const val PAUSE_MS = 3000L
    }
}
