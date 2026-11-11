package com.seifcustom

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.view.Gravity
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("seif-custom", MODE_PRIVATE) }
    private lateinit var page: LinearLayout
    private var chosenUri: Uri? = null
    private var unlocked = false
    private var vaultItems = JSONArray()
    private val accent get() = try { Color.parseColor(prefs.getString("accent_color", "#B58CFF")) } catch (_: Exception) { 0xFFB58CFF.toInt() }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun tint(c: Int) = android.content.res.ColorStateList.valueOf(c)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chosenUri = prefs.getString("wallpaper_uri", null)?.let { Uri.parse(it) }
        val root = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setBackgroundColor(0xFF0B1020.toInt()) }
        val side = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(7), dp(10), dp(7), dp(10)); setBackgroundColor(0xFF151C31.toInt()) }
        side.addView(label("SEIF\nCUSTOM", 18f, accent, true).apply { gravity = Gravity.CENTER; setPadding(0, dp(14), 0, dp(18)) })
        root.addView(ScrollView(this).apply { addView(side) }, LinearLayout.LayoutParams(dp(112), -1))
        page = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(13), dp(12), dp(13), dp(22)) }
        root.addView(ScrollView(this).apply { isFillViewport = true; addView(page) }, LinearLayout.LayoutParams(0, -1, 1f))
        setContentView(root)
        listOf(Triple("home","⌂","الرئيسية"),Triple("launcher","▦","شاشة SEIF"),Triple("apps","▣","التطبيقات"),Triple("permissions","✓","السماحات"),Triple("wallpaper","▧","الخلفيات"),Triple("personal","✦","التخصيص"),Triple("system","◉","أدوات الهاتف"),Triple("vault","◆","الخزنة"),Triple("gestures","⌁","الإيماءات")).forEach { (id,icon,title) ->
            side.addView(Button(this).apply { text="$icon\n$title"; textSize=10f; isAllCaps=false; setTextColor(Color.WHITE); backgroundTintList=tint(0xFF26314D.toInt()); minHeight=dp(55); setOnClickListener { show(id) } }, LinearLayout.LayoutParams(-1,dp(58)).apply { bottomMargin=dp(5) })
        }
        show(if (intent?.hasCategory(Intent.CATEGORY_HOME) == true) "launcher" else "home")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        show(if (intent.hasCategory(Intent.CATEGORY_HOME)) "launcher" else "home")
    }

    private fun label(s:String,size:Float=13f,color:Int=0xFFEAF0FF.toInt(),bold:Boolean=false)=TextView(this).apply { text=s;textSize=size;setTextColor(color);if(bold)setTypeface(null,android.graphics.Typeface.BOLD);setPadding(dp(2),dp(5),dp(2),dp(5)) }
    private fun card(title:String,subtitle:String?=null):LinearLayout {
        val c=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL;setPadding(dp(12),dp(10),dp(12),dp(10));background=android.graphics.drawable.GradientDrawable().apply { setColor(0xFF171F34.toInt());cornerRadius=dp(15).toFloat();setStroke(dp(1),0xFF2D3A59.toInt()) } }
        c.addView(label(title,15f,accent,true));if(!subtitle.isNullOrBlank())c.addView(label(subtitle,11f,0xFFADB9D3.toInt()))
        page.addView(c,LinearLayout.LayoutParams(-1,-2).apply { topMargin=dp(10) });return c
    }
    private fun button(parent:LinearLayout,title:String,run:()->Unit) { parent.addView(Button(this).apply { text=title;isAllCaps=false;textSize=12f;setTextColor(Color.WHITE);backgroundTintList=tint(0xFF303D60.toInt());setOnClickListener { run() } },LinearLayout.LayoutParams(-1,dp(47)).apply { topMargin=dp(5) }) }
    private fun show(section:String) {
        page.removeAllViews()
        val title=when(section){"apps"->"مركز التطبيقات";"launcher"->"الشاشة الرئيسية";"permissions"->"مركز السماحات";"wallpaper"->"استوديو الخلفيات";"personal"->"استوديو التخصيص";"system"->"أدوات الهاتف";"vault"->"الخزنة الخاصة";"gestures"->"التنقل والإيماءات";else->"تجربتك الرقمية بطريقتك"}
        page.addView(label("SEIF CUSTOM  /  $title",18f,Color.WHITE,true));page.addView(label("PERSONAL DEVICE EXPERIENCE",9f,0xFF8998B9.toInt()))
        when(section){"apps"->apps();"launcher"->launcherHome();"permissions"->permissions();"wallpaper"->wallpaper();"personal"->personal();"system"->systemTools();"vault"->vault();"gestures"->gestures();else->home()}
    }
    private fun home() {
        val c=card("مرحباً بك في SEIF CUSTOM","مركز واحد للخلفيات والتطبيقات والألوان والأدوات والخزنة.")
        button(c,"فتح شاشة SEIF الرئيسية"){show("launcher")};button(c,"مركز السماحات المطلوبة"){show("permissions")};button(c,"ابدأ تخصيص الألوان والأزرار"){show("personal")};button(c,"اختيار صورة أو فيديو للخلفية"){show("wallpaper")};button(c,"أدوات Wi-Fi وBluetooth والصوت"){show("system")};button(c,"افتح الخزنة بكلمة مرور"){show("vault")}
        val h=card("اجعل SEIF مشغّل الهاتف","الإخفاء داخل قائمة SEIF فقط؛ لا يخفي تطبيقات أخرى من إعدادات Android أو المشغّلات الأخرى.")
        button(h,"اختيار المشغّل الافتراضي"){openHomeSettings()}
    }
    private fun apps() {
        val c=card("التطبيقات المثبتة","بحث ومفضلة وإخفاء من قائمة SEIF CUSTOM فقط. اختر SEIF كمشغّل افتراضي لاستخدام قائمته كشاشة رئيسية.")
        val search=EditText(this).apply{hint="ابحث عن تطبيق";setSingleLine(true);setTextColor(Color.WHITE);setHintTextColor(0xFF8795B1.toInt())};c.addView(search)
        val tabs=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL};c.addView(tabs)
        val list=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};c.addView(list)
        var filter="all"
        fun render(){
            list.removeAllViews();val pm=packageManager
            val all=pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),0).filter{it.activityInfo.packageName!=packageName}.distinctBy{it.activityInfo.packageName}
            val hidden=prefs.getStringSet("hidden_apps",emptySet())?.toMutableSet()?:mutableSetOf();val favs=prefs.getStringSet("favorite_apps",emptySet())?.toMutableSet()?:mutableSetOf();val q=search.text.toString().trim().lowercase()
            val results=all.filter{info->val pkg=info.activityInfo.packageName;val ok=when(filter){"hidden"->pkg in hidden;"fav"->pkg in favs&&pkg !in hidden;else->pkg !in hidden};ok&&(q.isBlank()||info.loadLabel(pm).toString().lowercase().contains(q))}.sortedBy{it.loadLabel(pm).toString().lowercase()}
            list.addView(label("عدد النتائج: ${results.size}",11f,0xFFAEBBD6.toInt()))
            results.forEach{info->val pkg=info.activityInfo.packageName;val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
                row.addView(ImageView(this).apply{setImageDrawable(info.loadIcon(pm));layoutParams=LinearLayout.LayoutParams(dp(34),dp(34))})
                row.addView(label(info.loadLabel(pm).toString(),11f,Color.WHITE).apply{layoutParams=LinearLayout.LayoutParams(0,-2,1f)})
                row.addView(Button(this).apply{text=if(pkg in favs)"★" else "☆";minWidth=dp(36);setOnClickListener{val s=prefs.getStringSet("favorite_apps",emptySet())!!.toMutableSet();if(!s.add(pkg))s.remove(pkg);prefs.edit().putStringSet("favorite_apps",s).apply();render()}})
                row.addView(Button(this).apply{text=if(pkg in hidden)"إظهار" else "إخفاء";textSize=9f;setOnClickListener{val s=prefs.getStringSet("hidden_apps",emptySet())!!.toMutableSet();if(!s.add(pkg))s.remove(pkg);prefs.edit().putStringSet("hidden_apps",s).apply();render()}})
                row.setOnClickListener{pm.getLaunchIntentForPackage(pkg)?.let{try{startActivity(it)}catch(_:Exception){}}};list.addView(row)
            }
        }
        listOf("الكل" to "all","المفضلة" to "fav","المخفية" to "hidden").forEach{(n,k)->tabs.addView(Button(this).apply{text=n;textSize=9f;setOnClickListener{filter=k;render()}},LinearLayout.LayoutParams(0,dp(42),1f))}
        search.addTextChangedListener(object:android.text.TextWatcher{override fun beforeTextChanged(s:CharSequence?,st:Int,count:Int,a:Int){};override fun onTextChanged(s:CharSequence?,st:Int,b:Int,count:Int){render()};override fun afterTextChanged(s:android.text.Editable?){}})
        render()
        button(c,"إظهار جميع التطبيقات المخفية"){prefs.edit().remove("hidden_apps").apply();render();toast("تم إظهار التطبيقات داخل SEIF")}
        button(c,"تعيين SEIF كمشغّل افتراضي"){requestHomeRole()}
    }

    private fun launcherHome() {
        page.addView(label("شاشة SEIF الرئيسية",20f,Color.WHITE,true))
        page.addView(label("اضغط مطولاً على التطبيق لإخفائه من شاشة SEIF فقط. اضغط عليه لفتحه.",11f,0xFFADB9D3.toInt()))
        val search=EditText(this).apply { hint="ابحث في تطبيقاتك";setSingleLine(true);setTextColor(Color.WHITE);setHintTextColor(0xFF8795B1.toInt()) }
        page.addView(search,LinearLayout.LayoutParams(-1,dp(48)))
        val list=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }
        page.addView(list)
        fun render() {
            list.removeAllViews()
            val pm=packageManager
            val hidden=prefs.getStringSet("hidden_apps",emptySet())?.toSet()?:emptySet()
            val q=search.text.toString().trim().lowercase()
            val apps=pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),0)
                .filter { it.activityInfo.packageName!=packageName && it.activityInfo.packageName !in hidden }
                .distinctBy { it.activityInfo.packageName }
                .filter { it.loadLabel(pm).toString().lowercase().contains(q) }
                .sortedBy { it.loadLabel(pm).toString().lowercase() }
            list.addView(label("التطبيقات الظاهرة: " + apps.size,11f,0xFFADB9D3.toInt()))
            apps.forEach { info ->
                val pkg=info.activityInfo.packageName
                val row=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(5),dp(5),dp(5),dp(5)) }
                row.addView(ImageView(this).apply { setImageDrawable(info.loadIcon(pm));layoutParams=LinearLayout.LayoutParams(dp(42),dp(42)) })
                row.addView(label(info.loadLabel(pm).toString(),13f,Color.WHITE,true).apply { layoutParams=LinearLayout.LayoutParams(0,-2,1f) })
                row.setOnClickListener { try { pm.getLaunchIntentForPackage(pkg)?.let { startActivity(it) } } catch (_:Exception) { toast("تعذر فتح التطبيق") } }
                row.setOnLongClickListener {
                    android.app.AlertDialog.Builder(this).setTitle("إخفاء التطبيق؟")
                        .setMessage("سيختفي هذا التطبيق من شاشة SEIF فقط، ويمكن إظهاره من مركز التطبيقات.")
                        .setNegativeButton("إلغاء",null).setPositiveButton("إخفاء") { _,_ ->
                            val set=prefs.getStringSet("hidden_apps",emptySet())?.toMutableSet()?:mutableSetOf()
                            set.add(pkg);prefs.edit().putStringSet("hidden_apps",set).apply();render();toast("أُخفي التطبيق من شاشة SEIF")
                        }.show()
                    true
                }
                list.addView(row)
            }
            if(apps.isEmpty()) list.addView(label("لا توجد تطبيقات مطابقة. استعد المخفي من مركز التطبيقات.",12f))
        }
        search.addTextChangedListener(object:android.text.TextWatcher {
            override fun beforeTextChanged(s:CharSequence?,st:Int,count:Int,a:Int) {}
            override fun onTextChanged(s:CharSequence?,st:Int,b:Int,count:Int) { render() }
            override fun afterTextChanged(s:android.text.Editable?) {}
        })
        render()
        button(card("إدارة الشاشة"),"إدارة التطبيقات المخفية") { show("apps") }
        button(card("تفعيل الشاشة الرئيسية"),"اختيار SEIF كمشغّل الهاتف") { requestHomeRole() }
    }

    private fun isAccessibilityEnabled(): Boolean {
        val enabled=Settings.Secure.getString(contentResolver,Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)?:return false
        val component=ComponentName(this,SeifGestureNavigationService::class.java).flattenToString()
        return Settings.Secure.getInt(contentResolver,Settings.Secure.ACCESSIBILITY_ENABLED,0)==1 &&
            enabled.split(':').any { it.equals(component,true) }
    }

    private fun isKeyboardEnabled(): Boolean {
        val enabled=Settings.Secure.getString(contentResolver,Settings.Secure.ENABLED_INPUT_METHODS)?:return false
        return enabled.contains(packageName)
    }

    private fun isHomeRoleHeld(): Boolean {
        if(Build.VERSION.SDK_INT<29) return false
        return try { getSystemService(android.app.role.RoleManager::class.java)?.isRoleHeld(android.app.role.RoleManager.ROLE_HOME)==true } catch (_:Exception) { false }
    }

    private fun requestHomeRole() {
        try {
            if(Build.VERSION.SDK_INT>=29) {
                val manager=getSystemService(android.app.role.RoleManager::class.java)
                if(manager != null && manager.isRoleAvailable(android.app.role.RoleManager.ROLE_HOME) && !manager.isRoleHeld(android.app.role.RoleManager.ROLE_HOME)) {
                    startActivityForResult(manager.createRequestRoleIntent(android.app.role.RoleManager.ROLE_HOME),82)
                } else openHomeSettings()
            } else openHomeSettings()
        } catch (_:Exception) { openHomeSettings() }
    }

    private fun permissions() {
        val home=card("مشغّل الهاتف",if(isHomeRoleHeld()) "الحالة: تم اختيار SEIF كمشغّل افتراضي" else "الحالة: لم يتم اختيار SEIF كمشغّل افتراضي")
        button(home,if(isHomeRoleHeld()) "إدارة المشغّل الافتراضي" else "السماح لـ SEIF بأن يكون الشاشة الرئيسية") { requestHomeRole() }
        val access=card("خدمة الإيماءات",if(isAccessibilityEnabled()) "الحالة: مفعّلة" else "الحالة: غير مفعّلة")
        button(access,"فتح صفحة تفعيل إمكانية الوصول") { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        val keyboard=card("لوحة مفاتيح SEIF",if(isKeyboardEnabled()) "الحالة: مفعّلة ضمن لوحات المفاتيح" else "الحالة: لم تُفعّل بعد")
        button(keyboard,"إدارة وتفعيل لوحة المفاتيح") { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }
        val app=card("صلاحيات التطبيق","افتح صفحة معلومات SEIF لمراجعة الأذونات التي يسمح بها إصدار Android لديك.")
        button(app,"فتح أذونات SEIF وإعداداته") {
            try { startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+packageName))) }
            catch (_:Exception) { startActivity(Intent(Settings.ACTION_SETTINGS)) }
        }
        card("لماذا لا نفعّل كل شيء تلقائياً؟","Android يمنع التطبيقات من منح نفسها صلاحيات حساسة أو التحكم الكامل بالهاتف دون موافقة صاحبه. هذه الصفحة تفتح الإعداد الصحيح وتعرض حالة بعض التفعيلات بدلاً من الادعاء بأنها تمت.")
    }

    private fun wallpaper(){
        val c=card("خلفيات من جهازك","اختر صورة أو GIF أو فيديو ثم افتح معاينة الخلفية الحية وطبّقها.")
        c.addView(label(if(chosenUri==null)"لم تختر ملفاً بعد" else "الملف المختار: ${chosenUri?.lastPathSegment}",11f))
        button(c,"＋ رفع صورة / GIF / فيديو"){val i=Intent(Intent.ACTION_OPEN_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type="*/*";putExtra(Intent.EXTRA_MIME_TYPES,arrayOf("image/*","video/*"));addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)};startActivityForResult(i,41)}
        val op=label("شفافية الصورة: ${prefs.getInt("wallpaper_opacity",100)}%");c.addView(op)
        c.addView(SeekBar(this).apply{max=85;progress=(prefs.getInt("wallpaper_opacity",100)-15).coerceIn(0,85);setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{override fun onProgressChanged(s:SeekBar?,p:Int,u:Boolean){val n=p+15;prefs.edit().putInt("wallpaper_opacity",n).apply();op.text="شفافية الصورة: $n%"};override fun onStartTrackingTouch(s:SeekBar?){};override fun onStopTrackingTouch(s:SeekBar?){}})})
        button(c,"معاينة وتطبيق الخلفية"){if(chosenUri==null)toast("اختر صورة أو فيديو أولاً")else try{startActivity(Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,ComponentName(this,VideoWallpaperService::class.java)))}catch(_:Exception){toast("تعذر فتح معاينة الخلفية")}}
        button(c,"مسح الملف المختار"){prefs.edit().remove("wallpaper_uri").apply();chosenUri=null;show("wallpaper")}
        card("ملاحظة","الفيديو يعمل صامتاً كخلفية حية إذا كان الترميز مدعوماً. بعض الصيغ قد لا تعمل على كل الأجهزة.")
    }
    private fun personal(){
        val c=card("ألوان SEIF CUSTOM","اضغط على لون لتطبيقه وحفظه.")
        listOf("#B58CFF","#38D9FF","#E8B85C","#55D6A5","#FF7C9B","#F1F4FF","#7B9CFF","#FF8A45").chunked(4).forEach{colors->val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL};colors.forEach{hex->row.addView(Button(this).apply{text="●";textSize=22f;setTextColor(Color.parseColor(hex));backgroundTintList=tint(0xFF26314D.toInt());setOnClickListener{prefs.edit().putString("accent_color",hex).apply();show("personal")}},LinearLayout.LayoutParams(0,dp(48),1f).apply{setMargins(dp(2),dp(2),dp(2),dp(2))})};c.addView(row)}
        val appearance=card("تفضيلات المظهر");button(appearance,"حفظ الوضع الداكن"){prefs.edit().putString("app_mode","dark").apply();toast("تم حفظ الوضع الداكن")};button(appearance,"حفظ الوضع الفاتح"){prefs.edit().putString("app_mode","light").apply();toast("تم حفظ الوضع الفاتح")}
        val kb=card("لوحة مفاتيح SEIF","فعّلها من Android ثم اخترها. لوحة المفاتيح لا تحفظ النصوص. غيّر لون الأزرار والخلفية وحجم الحروف هنا.")
        listOf("#171F30","#202A40","#2B1744","#102F3B").forEach{hex->button(kb,"خلفية لوحة المفاتيح $hex"){prefs.edit().putString("keyboard_bg_color",hex).apply();toast("تم حفظ لون الخلفية")}}
        listOf("#29364D","#49366B","#31546B","#4B4935").forEach{hex->button(kb,"لون أزرار لوحة المفاتيح $hex"){prefs.edit().putString("keyboard_key_color",hex).apply();toast("تم حفظ لون الأزرار")}}
        button(kb,"حروف أكبر"){prefs.edit().putInt("keyboard_font_size",20).apply();toast("تم تكبير الحروف")}
        button(kb,"حجم حروف متوسط"){prefs.edit().putInt("keyboard_font_size",17).apply();toast("تم حفظ حجم الحروف")}
        button(kb,"إدارة لوحات المفاتيح"){startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))};button(kb,"اختيار لوحة المفاتيح"){(getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager).showInputMethodPicker()}
        val sys=card("أزرار الهاتف","أزرار Wi-Fi والسطوع والصوت تفتح لوحات Android الرسمية؛ لا يسمح Android للتطبيقات العادية بالتحكم المباشر بكل المفاتيح.")
        button(sys,"فتح أدوات النظام"){show("system")}
    }
    private fun systemTools(){
        val c=card("مفاتيح الهاتف","تفتح الأزرار لوحة النظام المناسبة بدلاً من أزرار شكلية لا تعمل.")
        listOf("Wi-Fi والإنترنت" to "wifi","Bluetooth" to "bluetooth","الصوت والاهتزاز" to "sound","السطوع والشاشة" to "display","نقطة الاتصال" to "hotspot","استهلاك البيانات" to "data","وضع الطيران" to "airplane","الموقع" to "location","إدارة التطبيقات" to "apps").forEach{(n,k)->button(c,"◉  $n"){openPanel(k)}}
        val q=card("لوحة الإعدادات السريعة","اسحب من أعلى الهاتف للوصول إلى مفاتيح النظام الأصلية.");button(q,"فتح إعدادات Android"){startActivity(Intent(Settings.ACTION_SETTINGS))}
    }
    private fun openPanel(key:String){
        val i=when(key){"wifi"->if(Build.VERSION.SDK_INT>=29)Intent(Settings.Panel.ACTION_WIFI)else Intent(Settings.ACTION_WIFI_SETTINGS);"bluetooth"->if(Build.VERSION.SDK_INT>=29)Intent(Settings.ACTION_BLUETOOTH_SETTINGS)else Intent(Settings.ACTION_BLUETOOTH_SETTINGS);"sound"->Intent(Settings.ACTION_SOUND_SETTINGS);"display"->Intent(Settings.ACTION_DISPLAY_SETTINGS);"hotspot"->Intent(Settings.ACTION_WIRELESS_SETTINGS);"data"->Intent(Settings.ACTION_DATA_USAGE_SETTINGS);"airplane"->Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);"location"->Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);else->Intent(Settings.ACTION_APPLICATION_SETTINGS)}
        try{startActivity(i)}catch(_:Exception){toast("افتح إعدادات الهاتف يدوياً")}
    }
    private fun openHomeSettings(){try{startActivity(Intent(Settings.ACTION_HOME_SETTINGS))}catch(_:Exception){startActivity(Intent(Settings.ACTION_SETTINGS))}}
    private fun gestures(){
        val c=card("إيماءات SEIF","سحب قصير من أسفل الشاشة للتطبيقات الأخيرة، وسحب أطول للرئيسية، ومن الحافتين للرجوع. ضغط 3 ثوانٍ لاستدعاء المساعد.")
        listOf("شريط صغير","دائرة","مربع دائري","خط مزدوج","نيون مضيء","زجاجي").forEach{v->button(c,"شكل المؤشر: $v"){prefs.edit().putString("nav_bottom_shape",v).apply();toast("تم حفظ الشكل")}}
        listOf("سهم","زاوية","ثلاث نقاط","دائري","مربع").forEach{v->button(c,"شكل الحافة: $v"){prefs.edit().putString("nav_side_shape",v).apply();toast("تم حفظ الشكل")}}
        listOf("nav_left_enabled" to "السحب من اليسار","nav_right_enabled" to "السحب من اليمين").forEach{(key,name)->button(c,"تبديل $name"){val enabled=!prefs.getBoolean(key,true);prefs.edit().putBoolean(key,enabled).apply();toast(if(enabled)"تم التفعيل" else "تم الإيقاف")}}
        val seek=SeekBar(this).apply{max=65;progress=(prefs.getInt("nav_sensitivity",55)-35).coerceIn(0,65);setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{override fun onProgressChanged(s:SeekBar?,p:Int,u:Boolean){prefs.edit().putInt("nav_sensitivity",p+35).apply()};override fun onStartTrackingTouch(s:SeekBar?){};override fun onStopTrackingTouch(s:SeekBar?){}})}
        c.addView(label("حساسية السحب: ${prefs.getInt("nav_sensitivity",55)}"));c.addView(seek)
        listOf("#FFFFFF","#B58CFF","#38D9FF","#E8B85C","#55D6A5").forEach{hex->button(c,"لون المؤشر $hex"){prefs.edit().putString("nav_color",hex).apply();toast("تم حفظ اللون")}}
        button(c,"إدارة خدمة إمكانية الوصول"){startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))}
        card("تنبيه","يجب تفعيل الخدمة يدوياً. قد تختلف الإيماءات حسب الشركة المصنّعة وقد تتعارض مع حواف النظام.")
    }
    private fun vault(){
        val c=card("الخزنة المشفرة","كلمة مرور 8 أحرف على الأقل، تشفير AES-GCM بمفتاح Android Keystore، وتخزين محلي. لا يوجد استرداد تلقائي؛ احتفظ بكلمة المرور.")
        if(!prefs.contains("vault_hash")){c.addView(label("لم تنشئ كلمة مرور بعد.",12f,0xFFE7C987.toInt()));button(c,"إنشاء كلمة مرور"){createPassword()};return}
        if(!unlocked){c.addView(label("الخزنة مقفلة."));button(c,"فتح الخزنة"){unlockVault()};return}
        c.addView(label("الخزنة مفتوحة",12f,0xFF79D8B0.toInt()))
        button(c,"＋ إضافة ملاحظة مشفرة"){addNote()}
        button(c,"＋ حفظ صورة أو فيديو أو ملف مشفر (12MB كحد أقصى)"){startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type="*/*"},73)}
        for(i in 0 until vaultItems.length()){val item=vaultItems.optJSONObject(i)?:continue;val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL};row.addView(label(item.optString("name"),11f,Color.WHITE).apply{layoutParams=LinearLayout.LayoutParams(0,-2,1f)});row.addView(Button(this).apply{text="حذف";textSize=9f;setOnClickListener{vaultItems.remove(i);saveVault();show("vault")}});c.addView(row)}
        button(c,"قفل الخزنة"){unlocked=false;vaultItems=JSONArray();show("vault")}
    }
    private fun passwordHash(p:String,salt:ByteArray):String{val spec=PBEKeySpec(p.toCharArray(),salt,120000,256);return try{Base64.encodeToString(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).encoded,Base64.NO_WRAP)}finally{spec.clearPassword()}}
    private fun createPassword(){
        val form=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(18),dp(8),dp(18),dp(4))}
        val a=EditText(this).apply{hint="كلمة المرور";inputType=129};val b=EditText(this).apply{hint="تأكيد كلمة المرور";inputType=129};form.addView(a);form.addView(b)
        val d=android.app.AlertDialog.Builder(this).setTitle("إنشاء الخزنة").setView(form).setNegativeButton("إلغاء",null).setPositiveButton("إنشاء",null).create()
        d.setOnShowListener{d.getButton(-1).setOnClickListener{if(a.text.length<8||a.text.toString()!=b.text.toString()){toast("استخدم 8 أحرف على الأقل وتأكد من التطابق");return@setOnClickListener};val salt=ByteArray(16).also{SecureRandom().nextBytes(it)};prefs.edit().putString("vault_salt",Base64.encodeToString(salt,Base64.NO_WRAP)).putString("vault_hash",passwordHash(a.text.toString(),salt)).apply();vaultItems=JSONArray();saveVault();unlocked=true;d.dismiss();show("vault")}};d.show()
    }
    private fun unlockVault(){
        val input=EditText(this).apply{hint="كلمة المرور";inputType=129}
        val d=android.app.AlertDialog.Builder(this).setTitle("فتح الخزنة").setView(input).setNegativeButton("إلغاء",null).setPositiveButton("فتح",null).create()
        d.setOnShowListener{d.getButton(-1).setOnClickListener{val salt=Base64.decode(prefs.getString("vault_salt","")?: "",Base64.NO_WRAP);val expected=prefs.getString("vault_hash","")?:"";val actual=passwordHash(input.text.toString(),salt);if(!MessageDigest.isEqual(actual.toByteArray(),expected.toByteArray())){toast("كلمة المرور غير صحيحة");return@setOnClickListener};try{vaultItems=JSONArray(String(VaultCrypto.decrypt(openFileInput("seif-vault.bin").use{it.readBytes()}),Charsets.UTF_8))}catch(_:java.io.FileNotFoundException){vaultItems=JSONArray()}catch(_:Exception){toast("تعذر فك تشفير الخزنة");return@setOnClickListener};unlocked=true;d.dismiss();show("vault")}};d.show()
    }
    private fun addNote(){
        val input=EditText(this).apply{hint="اكتب ملاحظتك الخاصة";minLines=4;gravity=Gravity.TOP;setTextColor(Color.WHITE)}
        val d=android.app.AlertDialog.Builder(this).setTitle("ملاحظة مشفرة").setView(input).setNegativeButton("إلغاء",null).setPositiveButton("حفظ",null).create()
        d.setOnShowListener{d.getButton(-1).setOnClickListener{if(input.text.isBlank()){toast("اكتب محتوى أولاً");return@setOnClickListener};vaultItems.put(JSONObject().put("name","ملاحظة ${vaultItems.length()+1}").put("type","نص").put("data",input.text.toString()));saveVault();d.dismiss();show("vault")}};d.show()
    }
    private fun saveVault(){try{openFileOutput("seif-vault.bin",MODE_PRIVATE).use{it.write(VaultCrypto.encrypt(vaultItems.toString().toByteArray(Charsets.UTF_8)))}}catch(_:Exception){toast("تعذر حفظ الخزنة")}}
    private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_SHORT).show()

    @Deprecated("Legacy document picker callback")
    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){
        super.onActivityResult(requestCode,resultCode,data)
        if(requestCode==82){show(if(isHomeRoleHeld()) "launcher" else "permissions");return}
        if(resultCode!=RESULT_OK)return;val uri=data?.data?:return
        if(requestCode==41){try{contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION)}catch(_:Exception){};chosenUri=uri;prefs.edit().putString("wallpaper_uri",uri.toString()).apply();toast("تم حفظ اختيار الخلفية");show("wallpaper")}
        else if(requestCode==73&&unlocked){try{val bytes=contentResolver.openInputStream(uri)?.use{it.readBytes()}?:byteArrayOf();if(bytes.size>12*1024*1024){toast("حجم الملف أكبر من 12MB");return};vaultItems.put(JSONObject().put("name",uri.lastPathSegment?:"ملف").put("type","ملف مشفر").put("data",Base64.encodeToString(bytes,Base64.NO_WRAP)));saveVault();toast("تم حفظ الملف مشفراً");show("vault")}catch(_:Exception){toast("تعذر قراءة الملف")}}
    }
}
