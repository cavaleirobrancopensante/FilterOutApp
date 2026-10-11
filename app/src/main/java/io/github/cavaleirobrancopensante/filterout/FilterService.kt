package io.github.cavaleirobrancopensante.filterout

import android.accessibilityservice.AccessibilityService
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import androidx.annotation.RequiresApi
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

/**
 * Runs in the background once switched on in Accessibility settings.
 *
 * Two ways of reading the screen, both entirely on the phone:
 *  1. The text Android already knows is on screen (web pages, apps, what you type).
 *  2. Optional: a screenshot read by on-device text recognition, to catch words
 *     inside pictures and video frames (Android 11+).
 * If either finds a banned word, the screen is covered.
 */
class FilterService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var store: WordStore
    private lateinit var overlay: BlockOverlay

    @Volatile private var matcher = WordMatcher(emptySet())
    private var ocrEnabled = true

    private var recognizer: TextRecognizer? = null
    private var ocrBusy = false
    private var lastOcrAt = 0L
    /** Anti-loop guard: no blocking for 5 seconds after the block screen is dismissed. */
    private val gate = BlockGate(clock = { SystemClock.uptimeMillis() })

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        reloadSettings()
    }

    private val scanTextTask = Runnable { scanText() }
    private val scanImageTask = Runnable { scanImage() }

    override fun onServiceConnected() {
        super.onServiceConnected()
        store = WordStore(this)
        overlay = BlockOverlay(this) { leave() }
        store.prefs.registerOnSharedPreferenceChangeListener(prefsListener)
        reloadSettings()
    }

    private fun reloadSettings() {
        matcher = WordMatcher(store.words)
        ocrEnabled = store.ocrEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || matcher.isEmpty || overlay.isShowing) return
        if (event.packageName == packageName) return
        if (gate.inGrace()) return

        // Wait for the screen to settle a little before reading it.
        handler.removeCallbacks(scanTextTask)
        handler.postDelayed(scanTextTask, TEXT_DELAY_MS)

        if (ocrEnabled) {
            handler.removeCallbacks(scanImageTask)
            handler.postDelayed(scanImageTask, IMAGE_DELAY_MS)
        }
    }

    // ---- 1. Text Android already knows about -------------------------------------

    private fun scanText() {
        if (overlay.isShowing || ownAppInFront()) return
        val text = StringBuilder()
        val roots = runCatching {
            windows
                .filter { it.type != AccessibilityWindowInfo.TYPE_ACCESSIBILITY_OVERLAY }
                .mapNotNull { it.root }
        }.getOrNull().orEmpty().ifEmpty { listOfNotNull(rootInActiveWindow) }

        for (root in roots) {
            if (root.packageName == packageName) continue
            collectText(root, text)
        }
        if (matcher.matches(text)) block()
    }

    private fun collectText(root: AccessibilityNodeInfo, out: StringBuilder) {
        val stack = ArrayDeque<AccessibilityNodeInfo>()
        stack.addLast(root)
        var visited = 0
        while (stack.isNotEmpty() && visited < MAX_NODES) {
            val node = stack.removeLast()
            visited++
            node.text?.let { out.append(it).append('\n') }
            node.contentDescription?.let { out.append(it).append('\n') }
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { stack.addLast(it) }
            }
        }
    }

    // ---- 2. Text inside images (on-device OCR) -----------------------------------

    private fun scanImage() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        if (!ocrEnabled || ocrBusy || overlay.isShowing || ownAppInFront()) return

        // Android only allows a screenshot about once a second; space them out.
        val wait = lastOcrAt + MIN_OCR_GAP_MS - SystemClock.uptimeMillis()
        if (wait > 0) {
            handler.removeCallbacks(scanImageTask)
            handler.postDelayed(scanImageTask, wait)
            return
        }
        captureAndRecognise()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun captureAndRecognise() {
        ocrBusy = true
        lastOcrAt = SystemClock.uptimeMillis()
        takeScreenshot(Display.DEFAULT_DISPLAY, mainExecutor, object : TakeScreenshotCallback {
            override fun onSuccess(result: ScreenshotResult) {
                val bitmap = result.hardwareBuffer.use { buffer ->
                    Bitmap.wrapHardwareBuffer(buffer, result.colorSpace)
                        ?.let { hw -> hw.copy(Bitmap.Config.ARGB_8888, false).also { hw.recycle() } }
                }
                if (bitmap == null || overlay.isShowing) {
                    bitmap?.recycle()
                    ocrBusy = false
                    return
                }
                val reader = recognizer
                    ?: TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS).also { recognizer = it }
                reader.process(InputImage.fromBitmap(bitmap, 0))
                    .addOnSuccessListener { found ->
                        if (!ownAppInFront() && matcher.matches(found.text)) block()
                    }
                    .addOnCompleteListener {
                        bitmap.recycle()
                        ocrBusy = false
                    }
            }

            override fun onFailure(errorCode: Int) {
                ocrBusy = false
            }
        })
    }

    // ---- Blocking ---------------------------------------------------------------

    private fun block() {
        // Every path to the block screen goes through here, including scans that
        // were already running when "Leave" was tapped, so the grace period
        // can't be bypassed.
        if (overlay.isShowing || !gate.mayBlock()) return
        handler.removeCallbacks(scanTextTask)
        handler.removeCallbacks(scanImageTask)
        overlay.show()
    }

    private fun leave() {
        gate.dismissed()
        handler.removeCallbacks(scanTextTask)
        handler.removeCallbacks(scanImageTask)
        overlay.hide()
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    private fun ownAppInFront(): Boolean =
        runCatching { rootInActiveWindow?.packageName == packageName }.getOrDefault(false)

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        if (::store.isInitialized) store.prefs.unregisterOnSharedPreferenceChangeListener(prefsListener)
        if (::overlay.isInitialized) overlay.hide()
        recognizer?.close()
        super.onDestroy()
    }

    private companion object {
        const val TEXT_DELAY_MS = 250L
        const val IMAGE_DELAY_MS = 900L
        const val MIN_OCR_GAP_MS = 1500L
        const val MAX_NODES = 4000
    }
}
