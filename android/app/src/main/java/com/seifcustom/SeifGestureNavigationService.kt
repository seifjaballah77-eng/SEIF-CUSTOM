package com.seifcustom

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent

/**
 * Gesture-only service. It does not read window text or inspect screen content.
 * The user explicitly enables it in Android Accessibility settings.
 */
class SeifGestureNavigationService : AccessibilityService() {
    private lateinit var wm: WindowManager
    private var bottom: GestureZone? = null
    private var left: GestureZone? = null
    private var right: GestureZone? = null
    private val preferenceListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "nav_left_enabled" || key == "nav_right_enabled") installZones()
        if (key == "nav_bottom_shape" || key == "nav_side_shape" || key == "nav_color") {
            listOfNotNull(bottom, left, right).forEach { it.invalidate() }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        getSharedPreferences("seif-custom", MODE_PRIVATE).registerOnSharedPreferenceChangeListener(preferenceListener)
        installZones()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onDestroy() {
        getSharedPreferences("seif-custom", MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(preferenceListener)
        removeZones()
        super.onDestroy()
    }

    private fun installZones() {
        removeZones()
        val prefs = getSharedPreferences("seif-custom", MODE_PRIVATE)
        bottom = GestureZone(this, "bottom")
        left = if (prefs.getBoolean("nav_left_enabled", true)) GestureZone(this, "left") else null
        right = if (prefs.getBoolean("nav_right_enabled", true)) GestureZone(this, "right") else null
        add(bottom!!, WindowManager.LayoutParams.MATCH_PARENT, dp(76), Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL)
        left?.let { add(it, dp(34), WindowManager.LayoutParams.MATCH_PARENT, Gravity.START or Gravity.CENTER_VERTICAL) }
        right?.let { add(it, dp(34), WindowManager.LayoutParams.MATCH_PARENT, Gravity.END or Gravity.CENTER_VERTICAL) }
    }

    private fun add(view: View, width: Int, height: Int, gravity: Int) {
        val params = WindowManager.LayoutParams(
            width, height, WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = gravity
        try { wm.addView(view, params) } catch (_: Exception) {}
    }

    private fun removeZones() {
        listOfNotNull(bottom, left, right).forEach { try { wm.removeView(it) } catch (_: Exception) {} }
        bottom = null; left = null; right = null
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun runGlobal(action: Int) {
        try { performGlobalAction(action) } catch (_: Exception) {}
    }

    private fun openAssistant() {
        val voice = Intent(Intent.ACTION_VOICE_COMMAND).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try { startActivity(voice) } catch (_: Exception) {
            val assist = Intent(Intent.ACTION_ASSIST).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try { startActivity(assist) } catch (_: Exception) {}
        }
    }

    private inner class GestureZone(context: android.content.Context, private val zone: String) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val prefs get() = getSharedPreferences("seif-custom", MODE_PRIVATE)
        private var downX = 0f
        private var downY = 0f
        private var longPressed = false
        private val longPress = Runnable { longPressed = true; openAssistant() }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val color = try { Color.parseColor(prefs.getString("nav_color", "#D8DDF0")) } catch (_: Exception) { Color.WHITE }
            paint.color = color
            paint.style = Paint.Style.FILL
            paint.alpha = if (zone == "bottom") 230 else 175
            if (zone == "bottom") {
                val shape = prefs.getString("nav_bottom_shape", "شريط صغير")
                val cx = width / 2f
                val cy = height - dp(16).toFloat()
                when (shape) {
                    "دائرة" -> canvas.drawCircle(cx, cy, dp(7).toFloat(), paint)
                    "مربع دائري" -> canvas.drawRoundRect(RectF(cx-dp(7),cy-dp(7),cx+dp(7),cy+dp(7)), dp(3).toFloat(), dp(3).toFloat(), paint)
                    "خط مزدوج" -> { canvas.drawRoundRect(RectF(cx-dp(32),cy-dp(5),cx-dp(3),cy+dp(1)), dp(3).toFloat(), dp(3).toFloat(), paint); canvas.drawRoundRect(RectF(cx+dp(3),cy-dp(5),cx+dp(32),cy+dp(1)), dp(3).toFloat(), dp(3).toFloat(), paint) }
                    "نيون مضيء" -> { paint.alpha=55; canvas.drawRoundRect(RectF(cx-dp(48),cy-dp(8),cx+dp(48),cy+dp(8)), dp(9).toFloat(), dp(9).toFloat(), paint); paint.alpha=245; canvas.drawRoundRect(RectF(cx-dp(34),cy-dp(2),cx+dp(34),cy+dp(2)), dp(3).toFloat(), dp(3).toFloat(), paint) }
                    "زجاجي" -> { paint.alpha=100; canvas.drawRoundRect(RectF(cx-dp(42),cy-dp(5),cx+dp(42),cy+dp(5)), dp(8).toFloat(), dp(8).toFloat(), paint) }
                    else -> canvas.drawRoundRect(RectF(cx-dp(34),cy-dp(3),cx+dp(34),cy+dp(3)), dp(4).toFloat(), dp(4).toFloat(), paint)
                }
            } else {
                val shape = prefs.getString("nav_side_shape", "سهم")
                val cx = if (zone == "left") dp(17).toFloat() else width-dp(17).toFloat()
                val cy = height / 2f
                when (shape) {
                    "دائري" -> canvas.drawCircle(cx, cy, dp(13).toFloat(), paint)
                    "مربع" -> canvas.drawRoundRect(RectF(cx-dp(12),cy-dp(12),cx+dp(12),cy+dp(12)), dp(5).toFloat(), dp(5).toFloat(), paint)
                    "ثلاث نقاط" -> { canvas.drawCircle(cx,cy-dp(5).toFloat(),dp(2).toFloat(),paint); canvas.drawCircle(cx,cy,dp(2).toFloat(),paint); canvas.drawCircle(cx,cy+dp(5).toFloat(),dp(2).toFloat(),paint) }
                    else -> {
                        paint.textSize = dp(28).toFloat(); paint.textAlign = Paint.Align.CENTER
                        canvas.drawText(if (zone == "left") "‹" else "›", cx, cy+dp(9).toFloat(), paint)
                    }
                }
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val threshold = dp(prefs.getInt("nav_sensitivity", 55))
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX; downY = event.rawY; longPressed = false
                    if (zone == "bottom") Handler(Looper.getMainLooper()).postDelayed(longPress, 3000)
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (zone == "bottom" && downY-event.rawY > dp(24)) Handler(Looper.getMainLooper()).removeCallbacks(longPress)
                    if (zone != "bottom" && kotlin.math.abs(event.rawX-downX) > dp(20)) Handler(Looper.getMainLooper()).removeCallbacks(longPress)
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    Handler(Looper.getMainLooper()).removeCallbacks(longPress)
                    if (longPressed) return true
                    if (zone == "bottom") {
                        val rise = downY-event.rawY
                        if (rise >= threshold*2.6f) runGlobal(GLOBAL_ACTION_HOME)
                        else if (rise >= threshold) runGlobal(GLOBAL_ACTION_RECENTS)
                    } else {
                        val dx = event.rawX-downX
                        if ((zone == "left" && dx >= threshold) || (zone == "right" && dx <= -threshold)) runGlobal(GLOBAL_ACTION_BACK)
                    }
                    return true
                }
                MotionEvent.ACTION_CANCEL -> { Handler(Looper.getMainLooper()).removeCallbacks(longPress); return true }
            }
            return true
        }
    }
}
