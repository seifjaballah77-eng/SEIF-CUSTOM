package com.seifcustom

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("seif-custom", MODE_PRIVATE) }
    private var chosenUri: Uri? = null
    private lateinit var selectedFile: TextView
    private lateinit var appList: LinearLayout
    private lateinit var searchApps: EditText
    private lateinit var filterVisible: Button
    private lateinit var filterFavorites: Button
    private lateinit var filterHidden: Button
    private var appFilter = "visible"
    private var navigationPreview: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        selectedFile = findViewById(R.id.selectedFile)
        appList = findViewById(R.id.appList)
        searchApps = findViewById(R.id.searchApps)
        filterVisible = findViewById(R.id.filterVisible)
        filterFavorites = findViewById(R.id.filterFavorites)
        filterHidden = findViewById(R.id.filterHidden)
        chosenUri = prefs.getString("wallpaper_uri", null)?.let { Uri.parse(it) }
        if (chosenUri != null) selectedFile.text = "خلفية محفوظة على هذا الجهاز"
        findViewById<Button>(R.id.chooseWallpaper).setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE); type = "*/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/gif", "image/webp", "image/*", "video/mp4", "video/webm", "video/ogg"))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            startActivityForResult(intent, 41)
        }
        val seek = findViewById<SeekBar>(R.id.opacitySeek)
        val saved = prefs.getInt("wallpaper_opacity", 100).coerceIn(15, 100)
        seek.max = 85
        seek.progress = saved - 15
        updateOpacityLabel(saved)
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress + 15
                prefs.edit().putInt("wallpaper_opacity", value).apply()
                updateOpacityLabel(value)
            }
            override fun onStartTrackingTouch(bar: SeekBar?) {}
            override fun onStopTrackingTouch(bar: SeekBar?) {}
        })
        findViewById<Button>(R.id.applyWallpaper).setOnClickListener {
            if (chosenUri == null) Toast.makeText(this, "اختر صورة أو فيديو أولاً.", Toast.LENGTH_SHORT).show()
            else {
                val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                    putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, ComponentName(this@MainActivity, VideoWallpaperService::class.java))
                }
                try { startActivity(intent) } catch (_: Exception) { Toast.makeText(this, "تعذر فتح معاينة الخلفية الحية.", Toast.LENGTH_LONG).show() }
            }
        }
        findViewById<Button>(R.id.removeWallpaper).setOnClickListener {
            prefs.edit().remove("wallpaper_uri").apply()
            chosenUri = null
            selectedFile.text = "لم تختر خلفية بعد"
            Toast.makeText(this, "أزيل اختيار الخلفية من التطبيق؛ الخلفية الحالية للهاتف لم تتغير.", Toast.LENGTH_LONG).show()
        }
        searchApps.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { renderApps() }
            override fun afterTextChanged(s: Editable?) {}
        })
        filterVisible.setOnClickListener { appFilter = "visible"; updateFilterButtons(); renderApps() }
        filterFavorites.setOnClickListener { appFilter = "favorites"; updateFilterButtons(); renderApps() }
        filterHidden.setOnClickListener { appFilter = "hidden"; updateFilterButtons(); renderApps() }
        updateFilterButtons()
        renderApps()
        setupNavigationStudio()
    }

    @Deprecated("Use the document picker result callback")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 41 && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            chosenUri = uri
            prefs.edit().putString("wallpaper_uri", uri.toString()).apply()
            selectedFile.text = "تم اختيار: " + (uri.lastPathSegment ?: "ملف محلي")
        }
    }

    private fun updateOpacityLabel(value: Int) {
        findViewById<TextView>(R.id.opacityLabel).text = "وضوح الخلفية: $value%"
    }

    private fun updateFilterButtons() {
        filterVisible.isSelected = appFilter == "visible"
        filterFavorites.isSelected = appFilter == "favorites"
        filterHidden.isSelected = appFilter == "hidden"
        filterVisible.alpha = if (filterVisible.isSelected) 1f else .65f
        filterFavorites.alpha = if (filterFavorites.isSelected) 1f else .65f
        filterHidden.alpha = if (filterHidden.isSelected) 1f else .65f
    }

    private fun renderApps() {
        appList.removeAllViews()
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .filter { it.activityInfo.packageName != packageName }
            .distinctBy { it.activityInfo.packageName }
        val hidden = prefs.getStringSet("hidden_apps", emptySet())?.toMutableSet() ?: mutableSetOf()
        val favorites = prefs.getStringSet("favorite_apps", emptySet())?.toMutableSet() ?: mutableSetOf()
        val query = searchApps.text?.toString()?.trim()?.lowercase().orEmpty()
        val filtered = apps.filter { info ->
            val pkg = info.activityInfo.packageName
            val isHidden = pkg in hidden
            val inTab = when (appFilter) {
                "hidden" -> isHidden
                "favorites" -> !isHidden && pkg in favorites
                else -> !isHidden
            }
            inTab && (query.isBlank() || info.loadLabel(pm).toString().lowercase().contains(query) || pkg.lowercase().contains(query))
        }.sortedWith(compareByDescending<android.content.pm.ResolveInfo> { it.activityInfo.packageName in favorites }.thenBy { it.loadLabel(pm).toString().lowercase() })

        findViewById<TextView>(R.id.appCount).text = when (appFilter) {
            "hidden" -> "التطبيقات المخفية داخل SEIF CUSTOM: ${filtered.size}"
            "favorites" -> "تطبيقاتك المفضلة: ${filtered.size}"
            else -> "التطبيقات المتاحة في المشغّل: ${filtered.size}"
        }
        if (filtered.isEmpty()) {
            appList.addView(TextView(this).apply {
                text = when (appFilter) {
                    "hidden" -> "لا توجد تطبيقات مخفية هنا."
                    "favorites" -> "لم تضف تطبيقات للمفضلة بعد."
                    else -> "لا توجد نتائج. جرّب كلمة بحث أخرى."
                }
                setTextColor(Color.LTGRAY); textSize = 13f; setPadding(8, 18, 8, 18)
            })
            return
        }
        filtered.forEach { info ->
            val pkg = info.activityInfo.packageName
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(10, 9, 10, 9)
                setBackgroundResource(R.drawable.panel_bg)
            }
            val icon = android.widget.ImageView(this).apply {
                setImageDrawable(info.loadIcon(pm))
                layoutParams = LinearLayout.LayoutParams(42, 42)
            }
            val name = TextView(this).apply {
                text = info.loadLabel(pm)
                setTextColor(Color.WHITE)
                textSize = 14f
                setPadding(12, 0, 8, 0)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val favorite = Button(this).apply {
                text = if (pkg in favorites) "★" else "☆"
                textSize = 17f
                minWidth = 42
                setOnClickListener {
                    val current = prefs.getStringSet("favorite_apps", emptySet())?.toMutableSet() ?: mutableSetOf()
                    if (!current.add(pkg)) current.remove(pkg)
                    prefs.edit().putStringSet("favorite_apps", current).apply()
                    renderApps()
                }
            }
            val action = Button(this).apply {
                text = if (appFilter == "hidden") "استعادة" else "إخفاء"
                textSize = 10f
                setOnClickListener {
                    val current = prefs.getStringSet("hidden_apps", emptySet())?.toMutableSet() ?: mutableSetOf()
                    if (appFilter == "hidden") current.remove(pkg) else current.add(pkg)
                    prefs.edit().putStringSet("hidden_apps", current).apply()
                    renderApps()
                }
            }
            row.addView(icon); row.addView(name); row.addView(favorite); row.addView(action)
            row.setOnClickListener {
                if (appFilter != "hidden") {
                    pm.getLaunchIntentForPackage(pkg)?.let { launch ->
                        try { startActivity(launch) } catch (_: Exception) { Toast.makeText(this, "تعذر فتح التطبيق.", Toast.LENGTH_SHORT).show() }
                    }
                }
            }
            appList.addView(row)
            appList.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(1, 7) })
        }
    }

    private fun setupNavigationStudio() {
        val root = (findViewById<View>(android.R.id.content) as android.view.ViewGroup).getChildAt(0) as? android.widget.ScrollView ?: return
        val container = root.getChildAt(0) as? LinearLayout ?: return
        val section = TextView(this).apply {
            text = "SEIF Gesture Navigation"
            setTextColor(Color.WHITE); textSize = 19f; setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(14, 22, 14, 10); setBackgroundResource(R.drawable.panel_bg)
        }
        container.addView(section)
        container.addView(TextView(this).apply {
            text = "تنقل بالسحب من أسفل الشاشة، والرجوع من الحافتين. الضغط على الخط 3 ثوانٍ يفتح المساعد الافتراضي. يتطلب تفعيل خدمة إمكانية الوصول يدوياً."
            setTextColor(Color.LTGRAY); textSize = 12f; setPadding(8, 10, 8, 10)
        })
        fun addChoice(title: String, key: String, values: List<String>, default: String) {
            container.addView(TextView(this).apply { text = title; setTextColor(Color.WHITE); textSize = 14f; setPadding(8, 12, 8, 4) })
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            values.forEach { value ->
                val button = Button(this).apply {
                    text = value; textSize = 10f
                    setOnClickListener {
                        prefs.edit().putString(key, value).apply()
                        Toast.makeText(this@MainActivity, "تم اختيار: $value", Toast.LENGTH_SHORT).show()
                        setupNavigationPreview()
                    }
                }
                row.addView(button, LinearLayout.LayoutParams(0, 46, 1f))
            }
            container.addView(row)
            if (prefs.getString(key, null) == null) prefs.edit().putString(key, default).apply()
        }
        addChoice("شكل الخط السفلي", "nav_bottom_shape", listOf("شريط صغير","دائرة","مربع دائري","خط مزدوج","نيون مضيء","زجاجي"), "شريط صغير")
        addChoice("شكل مؤشرات الجانبين", "nav_side_shape", listOf("سهم","زاوية","ثلاث نقاط","دائري","مربع"), "سهم")
        addChoice("لون عناصر التنقل", "nav_color", listOf("#D8DDF0","#FFFFFF","#C6A15B","#A78BFA","#38D9FF"), "#D8DDF0")
        val sensLabel = TextView(this).apply { text = "حساسية السحب"; setTextColor(Color.WHITE); setPadding(8, 12, 8, 4) }
        container.addView(sensLabel)
        val sensitivity = SeekBar(this).apply {
            max = 65; progress = (prefs.getInt("nav_sensitivity",55)-35).coerceIn(0,65)
            setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val value=progress+35; prefs.edit().putInt("nav_sensitivity",value).apply()
                    sensLabel.text="حساسية السحب: $value"
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        sensLabel.text = "حساسية السحب: ${prefs.getInt("nav_sensitivity",55)}"
        container.addView(sensitivity)
        fun toggleButton(label: String, key: String) {
            val button = Button(this).apply {
                fun refresh() { text = label + if (prefs.getBoolean(key,true)) " — مفعّل" else " — متوقف" }
                refresh()
                setOnClickListener { prefs.edit().putBoolean(key,!prefs.getBoolean(key,true)).apply(); refresh() }
            }
            container.addView(button)
        }
        toggleButton("السحب من اليسار للرجوع", "nav_left_enabled")
        toggleButton("السحب من اليمين للرجوع", "nav_right_enabled")
        container.addView(TextView(this).apply {
            text = "الخط السفلي: سحب قصير للأعلى = التطبيقات الأخيرة، سحب أطول = الرئيسية، ضغط مستمر 3 ثوانٍ = المساعد الافتراضي."
            setTextColor(Color.LTGRAY); textSize = 12f; setPadding(8, 8, 8, 8)
        })
        val enable = Button(this).apply {
            text = "تفعيل / إدارة إيماءات SEIF من إعدادات إمكانية الوصول"
            setOnClickListener { try { startActivity(Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)) } catch (_: Exception) { Toast.makeText(this@MainActivity,"افتح إعدادات إمكانية الوصول يدوياً.",Toast.LENGTH_LONG).show() } }
        }
        container.addView(enable)
        container.addView(TextView(this).apply {
            text = "الخصوصية: الخدمة لا تقرأ نصوص الشاشة ولا تجمع بيانات. تستخدم إمكانية الوصول فقط لتنفيذ رجوع/رئيسية/التطبيقات الأخيرة بعد تفعيلك لها. قد تتعارض مناطق السحب مع إيماءات الشركة المصنّعة؛ أوقف الخدمة من إعدادات إمكانية الوصول إذا حدث ذلك."
            setTextColor(0xFF9EABC8.toInt()); textSize = 11f; setPadding(8, 8, 8, 16)
        })
        setupNavigationPreview()
    }

    private fun setupNavigationPreview() {
        val root = (findViewById<View>(android.R.id.content) as android.view.ViewGroup).getChildAt(0) as? android.widget.ScrollView ?: return
        val container = root.getChildAt(0) as? LinearLayout ?: return
        navigationPreview?.let { container.removeView(it) }
        val shape = prefs.getString("nav_bottom_shape","شريط صغير")
        val glyph = when (shape) {
            "دائرة" -> "●"
            "مربع دائري" -> "▣"
            "خط مزدوج" -> "━  ━"
            "نيون مضيء" -> "━━━━━━"
            "زجاجي" -> "▰"
            else -> "━━━━"
        }
        val color = prefs.getString("nav_color","#D8DDF0")
        val preview = TextView(this).apply {
            text = "معاينة الخط:  $glyph     |     سحب من الجانبين للرجوع"
            gravity = Gravity.CENTER; textSize = 13f; setTextColor(Color.parseColor(color))
            setPadding(10, 18, 10, 18); setBackgroundResource(R.drawable.panel_bg)
        }
        navigationPreview = preview
        val sectionIndex = container.findViewWithText("SEIF Gesture Navigation")?.let { container.indexOfChild(it) } ?: -1
        container.addView(preview, if (sectionIndex >= 0) sectionIndex+1 else 2)
    }
}
