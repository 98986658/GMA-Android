package com.example.gma

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button

class AutomationService : AccessibilityService() {
    companion object { @Volatile var instance: AutomationService? = null }
    private val handler = Handler(Looper.getMainLooper())
    private var active = false
    private var generation = 0
    private var overlay: Button? = null
    private lateinit var wm: WindowManager
    override fun onServiceConnected() { super.onServiceConnected(); instance = this; wm = getSystemService(WINDOW_SERVICE) as WindowManager }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() { stop() }
    override fun onDestroy() { stop(); instance = null; super.onDestroy() }

    fun stop() {
        active = false; generation++
        handler.removeCallbacksAndMessages(null)
        overlay?.let { try { wm.removeView(it) } catch (_: Exception) {} }
        overlay = null
    }
    private fun showStopOverlay(): Boolean = try {
        val btn = Button(this).apply {
            text = "停止 GMA"; setTextColor(Color.WHITE); setBackgroundColor(Color.rgb(180, 20, 20))
            setOnClickListener { stop() }
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START; x = 12; y = 100 }
        wm.addView(btn, params); overlay = btn; true
    } catch (_: Exception) { false }

    fun startScript(steps: List<Step>, repeats: Int): String {
        stop()
        if (steps.isEmpty() || steps.size > 40 || repeats !in 1..5) return "腳本參數無效"
        val metrics = resources.displayMetrics
        if (metrics.widthPixels <= metrics.heightPixels) return "請先將手機轉為橫向，再啟動"
        if (!showStopOverlay()) return "無法顯示停止按鈕，已拒絕啟動"
        active = true
        val token = generation
        val deadline = SystemClock.elapsedRealtime() + 60_000L
        val sequence = List(repeats) { steps }.flatten()
        fun run(index: Int) {
            if (!active || token != generation) return
            if (SystemClock.elapsedRealtime() >= deadline || index >= sequence.size) { stop(); return }
            val m = resources.displayMetrics
            if (m.widthPixels <= m.heightPixels) { stop(); return }
            val step = sequence[index]
            if (step is Step.Wait) {
                handler.postDelayed({ run(index + 1) }, step.duration); return
            }
            fun px(x: Float) = (x / 100f * (m.widthPixels - 1)).coerceIn(0f, (m.widthPixels - 1).toFloat())
            fun py(y: Float) = (y / 100f * (m.heightPixels - 1)).coerceIn(0f, (m.heightPixels - 1).toFloat())
            val path = Path()
            val duration: Long
            when (step) {
                is Step.Tap -> { path.moveTo(px(step.x), py(step.y)); duration = 80L }
                is Step.Swipe -> { path.moveTo(px(step.x1), py(step.y1)); path.lineTo(px(step.x2), py(step.y2)); duration = step.duration }
                else -> return
            }
            val gesture = GestureDescription.Builder().addStroke(GestureDescription.StrokeDescription(path, 0, duration)).build()
            val accepted = dispatchGesture(gesture, object: GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    if (active && token == generation) handler.postDelayed({ run(index + 1) }, 150)
                }
                override fun onCancelled(gestureDescription: GestureDescription?) { stop() }
            }, handler)
            if (!accepted) stop()
        }
        run(0)
        handler.postDelayed({ if (token == generation) stop() }, 60_000L)
        return "已開始；可點擊紅色浮動按鈕立即停止"
    }
}
