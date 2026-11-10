package com.seifcustom

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("seif-custom", MODE_PRIVATE) }
    private var chosenUri: Uri? = null
    private lateinit var selectedFile: TextView
    private lateinit var appList: LinearLayout
    private var showingHidden = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        selectedFile = findViewById(R.id.selectedFile)
        appList = findViewById(R.id.appList)
        chosenUri = prefs.getString("wallpaper_uri", null)?.let { Uri.parse(it) }
        if (chosenUri != null) selectedFile.text = "خلفية محفوظة على هذا الجهاز"
        findViewById<Button>(R.id.chooseWallpaper).setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE); type = "*/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/gif", "image/webp", "image/*", "video/mp4", "video/webm"))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            startActivityForResult(intent, 41)
        }
        val seek = findViewById<SeekBar>(R.id.opacitySeek)
        val saved = prefs.getInt("wallpaper_opacity", 85).coerceIn(15, 85)
        seek.progress = saved - 15
        updateOpacityLabel(saved)
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress + 15; prefs.edit().putInt("wallpaper_opacity", value).apply(); updateOpacityLabel(value)
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
            prefs.edit().remove("wallpaper_uri").apply(); chosenUri = null; selectedFile.text = "لم تختر خلفية بعد"
            Toast.makeText(this, "أزيل اختيار الخلفية من التطبيق؛ الخلفية الحالية للهاتف لم تتغير.", Toast.LENGTH_LONG).show()
        }
        findViewById<Button>(R.id.showHidden).setOnClickListener {
            showingHidden = !showingHidden
            findViewById<Button>(R.id.showHidden).text = if (showingHidden) "عرض كل التطبيقات" else "عرض التطبيقات المخفية / استعادتها"
            renderApps()
        }
        renderApps()
    }

    @Deprecated("Use the document picker result callback")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 41 && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            chosenUri = uri; prefs.edit().putString("wallpaper_uri", uri.toString()).apply()
            selectedFile.text = "تم اختيار: " + (uri.lastPathSegment ?: "ملف محلي")
        }
    }

    private fun updateOpacityLabel(value: Int) {
        findViewById<TextView>(R.id.opacityLabel).text = "وضوح الخلفية: $value%"
    }

    private fun renderApps() {
        appList.removeAllViews()
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .filter { it.activityInfo.packageName != packageName }
            .distinctBy { it.activityInfo.packageName }
            .sortedBy { it.loadLabel(pm).toString().lowercase() }
        val hidden = prefs.getStringSet("hidden_apps", emptySet())?.toMutableSet() ?: mutableSetOf()
        val filtered = apps.filter { (it.activityInfo.packageName in hidden) == showingHidden }
        if (filtered.isEmpty()) {
            appList.addView(TextView(this).apply {
                text = if (showingHidden) "لا توجد تطبيقات مخفية." else "لم نعثر على تطبيقات ظاهرة."
                setTextColor(Color.LTGRAY); textSize = 13f; setPadding(8, 16, 8, 16)
            }); return
        }
        filtered.forEach { info ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(10, 10, 10, 10); setBackgroundResource(R.drawable.panel_bg) }
            val icon = android.widget.ImageView(this).apply { setImageDrawable(info.loadIcon(pm)); layoutParams = LinearLayout.LayoutParams(42, 42) }
            val name = TextView(this).apply { text = info.loadLabel(pm); setTextColor(Color.WHITE); textSize = 14f; setPadding(12, 0, 8, 0); layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) }
            val action = Button(this).apply {
                text = if (showingHidden) "استعادة" else "إخفاء"; textSize = 10f
                setOnClickListener {
                    val current = prefs.getStringSet("hidden_apps", emptySet())?.toMutableSet() ?: mutableSetOf()
                    if (showingHidden) current.remove(info.activityInfo.packageName) else current.add(info.activityInfo.packageName)
                    prefs.edit().putStringSet("hidden_apps", current).apply(); renderApps()
                }
            }
            row.addView(icon); row.addView(name); row.addView(action)
            row.setOnClickListener { if (!showingHidden) pm.getLaunchIntentForPackage(info.activityInfo.packageName)?.let { startActivity(it) } }
            appList.addView(row); appList.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(1, 8) })
        }
    }
}
