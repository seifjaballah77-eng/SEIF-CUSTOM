package com.seifcustom

import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout

/** Local-only starter keyboard. It never stores or transmits typed text. */
class SeifKeyboardService : InputMethodService() {
    private var language = 0
    private var symbols = false
    private val arabic = listOf("ض ص ث ق ف غ ع ه خ ح ج د", "ش س ي ب ل ا ت ن م ك ط", "ئ ء ؤ ر لا ى ة و ز ظ")
    private val english = listOf("q w e r t y u i o p", "a s d f g h j k l", "z x c v b n m")
    private val symbolRows = listOf("1 2 3 4 5 6 7 8 9 0", "@ # $ % & - + ( ) /", "* \" ' : ; ! ?")
    private val root by lazy { LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(3), dp(4), dp(3), dp(4)); setBackgroundColor(0xFF171F30.toInt()) } }

    override fun onCreateInputView(): View {
        rebuild()
        return root
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        symbols = false
        if (::root.isInitialized) rebuild()
    }

    private fun rebuild() {
        root.removeAllViews()
        val rows = if (symbols) symbolRows else if (language == 0) arabic else english
        rows.forEach { row ->
            val line = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; weightSum = row.split(" ").size.toFloat() }
            row.split(" ").forEach { key -> line.addView(keyButton(key) ) }
            root.addView(line)
        }
        val bottom = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        listOf("🌐", if (symbols) "ABC" else "?123", "،", "مسافة", "⌫", "↵").forEach { bottom.addView(keyButton(it)) }
        root.addView(bottom)
    }

    private fun keyButton(label: String): Button = Button(this).apply {
        text = label
        textSize = if (label == "مسافة") 12f else 17f
        isAllCaps = false
        setTextColor(0xFFF5F7FF.toInt())
        backgroundTintList = android.content.res.ColorStateList.valueOf(
            if (label == "مسافة") 0xFF34466D.toInt() else 0xFF29364D.toInt()
        )
        minWidth = 0; minimumWidth = 0; minHeight = dp(46); minimumHeight = dp(46)
        layoutParams = LinearLayout.LayoutParams(0, dp(48), 1f).apply { setMargins(dp(2), dp(2), dp(2), dp(2)) }
        setOnClickListener { press(label) }
    }

    private fun press(key: String) {
        val connection = currentInputConnection ?: return
        when (key) {
            "🌐" -> { language = 1 - language; symbols = false; rebuild() }
            "?123" -> { symbols = true; rebuild() }
            "ABC" -> { symbols = false; rebuild() }
            "مسافة" -> connection.commitText(" ", 1)
            "⌫" -> connection.deleteSurroundingText(1, 0)
            "↵" -> connection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            "،" -> connection.commitText(if (language == 0) "،" else ",", 1)
            else -> connection.commitText(key, 1)
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
