(function(){
"use strict";
const $=id=>document.getElementById(id);
const KEY="seif-custom-phone-simulator-v1";
const defaults={accent:"#b6a1ff",wallpaperColor:"#18223b",glass:35,opacity:100,shape:"soft",scale:100,apps:[
{name:"المتصفح",url:"https://www.google.com/",symbol:"⌕"},
{name:"YouTube",url:"https://www.youtube.com/",symbol:"▶"},
{name:"WhatsApp",url:"https://www.whatsapp.com/download/",symbol:"W"},
{name:"Instagram",url:"https://www.instagram.com/",symbol:"◎"},
{name:"Google",url:"https://www.google.com/",symbol:"G"},
{name:"GitHub",url:"https://github.com/",symbol:"⌘"},
{name:"خرائط",url:"https://maps.google.com/",symbol:"⌖"},
{name:"Gmail",url:"https://mail.google.com/",symbol:"✉"},
{name:"Telegram",url:"https://telegram.org/apps",symbol:"➤"},
{name:"Spotify",url:"https://www.spotify.com/download/",symbol:"♫"},
{name:"TikTok",url:"https://www.tiktok.com/",symbol:"♪"},
{name:"ChatGPT",url:"https://chatgpt.com/",symbol:"✳"}
]};
let state=load(),wallUrl=null,wallBlob=null,wallVideoUrl=null,dbPromise=null;
function validUrl(s){try{const u=new URL(s);return ["http:","https:"].includes(u.protocol)?u.href:null}catch(_){return null}}
function cloneDefaults(){return {...defaults,apps:defaults.apps.map(a=>({...a}))}}
function load(){try{const x=JSON.parse(localStorage.getItem(KEY)||"null")||{};return{accent:/^#[0-9a-f]{6}$/i.test(x.accent||"")?x.accent:defaults.accent,wallpaperColor:/^#[0-9a-f]{6}$/i.test(x.wallpaperColor||"")?x.wallpaperColor:defaults.wallpaperColor,glass:Math.min(90,Math.max(10,Number(x.glass)||defaults.glass)),opacity:Math.min(100,Math.max(15,Number(x.opacity)||defaults.opacity)),shape:["soft","round","pill","sharp"].includes(x.shape)?x.shape:defaults.shape,scale:Math.min(115,Math.max(75,Number(x.scale)||100)),apps:Array.isArray(x.apps)?x.apps.filter(a=>a&&typeof a.name==="string"&&validUrl(a.url)).slice(0,24):cloneDefaults().apps}}catch(_){return cloneDefaults()}}
function openMediaDB(){if(!window.indexedDB)return Promise.reject(Error("IndexedDB unavailable"));if(!dbPromise)dbPromise=new Promise((resolve,reject)=>{const r=indexedDB.open("seif-custom-phone-preview",1);r.onupgradeneeded=()=>{if(!r.result.objectStoreNames.contains("media"))r.result.createObjectStore("media")};r.onsuccess=()=>resolve(r.result);r.onerror=()=>reject(r.error)});return dbPromise}
function mediaAction(mode,action,value){return openMediaDB().then(db=>new Promise((resolve,reject)=>{const tx=db.transaction("media",mode),store=tx.objectStore("media");const req=action==="put"?store.put(value,"wallpaper"):action==="get"?store.get("wallpaper"):store.delete("wallpaper");req.onsuccess=()=>resolve(req.result);req.onerror=()=>reject(req.error);tx.onerror=()=>reject(tx.error)}))}
function showWallpaper(blob){if(wallUrl)URL.revokeObjectURL(wallUrl);if(wallVideoUrl)URL.revokeObjectURL(wallVideoUrl);wallBlob=blob||null;wallUrl=null;wallVideoUrl=null;const video=$("phoneWallpaperVideo");video.pause();video.removeAttribute("src");video.load();if(!blob){$("phoneWallpaper").style.backgroundImage="linear-gradient(150deg,#253c61,#121a30 56%,#4a315c)";return}const url=URL.createObjectURL(blob);if(blob.type.startsWith("video/")){$("phoneWallpaper").style.backgroundImage="linear-gradient(150deg,#253c61,#121a30 56%,#4a315c)";wallVideoUrl=url;video.src=url;video.play().catch(()=>{});}else{wallUrl=url;video.removeAttribute("src");$("phoneWallpaper").style.backgroundImage="url("+url+")"}}
async function restoreWallpaper(){try{const item=await mediaAction("readonly","get");if(item&&item.blob)showWallpaper(item.blob)}catch(_){}}
function save(){localStorage.setItem(KEY,JSON.stringify(state))}
function toast(t){$("toast").textContent=t;$("toast").classList.add("show");setTimeout(()=>$("toast").classList.remove("show"),2200)}
function safeText(t){return String(t||"").trim().slice(0,22)}
function setDesign(){const root=document.documentElement;root.style.setProperty("--accent",state.accent);root.style.setProperty("--glass",state.glass/100);root.style.setProperty("--wall-opacity",state.opacity/100);root.style.setProperty("--icon-radius",({soft:"17px",round:"50%",pill:"999px",sharp:"5px"})[state.shape]);$("deviceScreen").style.backgroundColor=state.wallpaperColor;$("phoneWallpaper").style.backgroundColor=state.wallpaperColor;$("phoneWallpaper").style.backgroundImage=wallUrl?"url("+wallUrl+")":"linear-gradient(150deg,#253c61,#121a30 56%,#4a315c)";$("accent").value=state.accent;$("wallpaperColor").value=state.wallpaperColor;$("glass").value=state.glass;$("glassOut").textContent=state.glass+"%";$("opacity").value=state.opacity;$("opacityOut").textContent=state.opacity+"%";$("shape").value=state.shape;$("scale").value=state.scale;$("scaleOut").textContent=state.scale+"%";const d=$("device");d.style.transform="scale("+state.scale/100+")";d.style.transformOrigin="top center";}
function iconFor(a){const el=document.createElement("span");el.className="app-icon";el.textContent=a.symbol||safeText(a.name).charAt(0).toUpperCase();return el}
function makeApp(a){const link=document.createElement("a");link.className="phone-app";link.href=a.url;link.target="_blank";link.rel="noopener noreferrer";link.title="فتح رابط "+a.name;link.append(iconFor(a));const name=document.createElement("span");name.className="app-label";name.textContent=a.name;link.append(name);return link}
function makeDockApp(a){const link=document.createElement("a");link.className="dock-app";link.href=a.url;link.target="_blank";link.rel="noopener noreferrer";link.title="فتح "+a.name;link.append(iconFor(a));return link}
function renderApps(){const home=$("homeApps"),all=$("allApps"),dock=$("phoneDock");home.replaceChildren();all.replaceChildren();dock.replaceChildren();state.apps.slice(0,8).forEach(a=>home.append(makeApp(a)));state.apps.forEach(a=>all.append(makeApp(a)));state.apps.slice(0,4).forEach(a=>dock.append(makeDockApp(a)));$("appManager").replaceChildren();state.apps.forEach((a,i)=>{const row=document.createElement("div");row.className="managed-app";const link=document.createElement("a");link.href=a.url;link.target="_blank";link.rel="noopener noreferrer";link.textContent=a.name;const remove=document.createElement("button");remove.type="button";remove.textContent="إزالة";remove.addEventListener("click",()=>{state.apps.splice(i,1);save();renderApps()});row.append(link,remove);$("appManager").append(row)})}
function switchPage(name){document.querySelectorAll(".phone-page").forEach(p=>p.classList.toggle("active",p.id==="page-"+name));document.querySelectorAll(".phone-nav button").forEach(b=>b.classList.toggle("active",b.dataset.page===name))}
function updateClock(){const d=new Date(),time=new Intl.DateTimeFormat("ar-TN",{hour:"2-digit",minute:"2-digit",hour12:false}).format(d),date=new Intl.DateTimeFormat("ar-TN",{weekday:"long",day:"numeric",month:"long"}).format(d);$("phoneClock").textContent=time;$("phoneTime").textContent=time;$("widgetClock").textContent=time;$("phoneDay").textContent=date;$("widgetDate").textContent=date}
async function reset(){state=cloneDefaults();showWallpaper(null);$("wallpaperFile").value="";try{await mediaAction("readwrite","delete")}catch(_){}save();setDesign();renderApps();switchPage("home");toast("تمت استعادة التصميم الافتراضي")}
$("accent").addEventListener("input",e=>{state.accent=e.target.value;save();setDesign()});
$("wallpaperColor").addEventListener("input",e=>{state.wallpaperColor=e.target.value;save();setDesign()});
$("glass").addEventListener("input",e=>{state.glass=+e.target.value;save();setDesign()});
$("opacity").addEventListener("input",e=>{state.opacity=+e.target.value;save();setDesign()});
$("shape").addEventListener("change",e=>{state.shape=e.target.value;save();setDesign()});
$("scale").addEventListener("input",e=>{state.scale=+e.target.value;save();setDesign()});
$("wallpaperFile").addEventListener("change",e=>{const file=e.target.files[0];if(!file)return;if(!file.type.startsWith("image/")&&!["video/mp4","video/webm","video/ogg"].includes(file.type)){toast("اختر صورة أو فيديو MP4 أو WebM.");return}showWallpaper(file);mediaAction("readwrite","put",{blob:file,name:file.name,type:file.type}).then(()=>toast("تم حفظ الخلفية للمعاينة على هذا المتصفح.")).catch(()=>toast("تم عرض الخلفية مؤقتاً؛ تعذر حفظ الملف."))});
$("clearWallpaper").addEventListener("click",()=>{showWallpaper(null);$("wallpaperFile").value="";mediaAction("readwrite","delete").catch(()=>{});setDesign()});
$("addApp").addEventListener("click",()=>{const name=safeText($("appName").value),url=validUrl($("appUrl").value.trim());if(!name||!url){toast("أدخل اسم التطبيق ورابطاً صحيحاً يبدأ بـ https://");return}if(state.apps.length>=24){toast("وصلت إلى الحد الأقصى: 24 تطبيقاً.");return}state.apps.push({name,url,symbol:name.charAt(0).toUpperCase()});$("appName").value="";$("appUrl").value="";save();renderApps();toast("أضيف التطبيق كرابط قابل للفتح.")});
document.querySelectorAll(".phone-nav button").forEach(b=>b.addEventListener("click",()=>switchPage(b.dataset.page)));
$("smaller").addEventListener("click",()=>{$("scale").value=Math.max(75,+$("scale").value-5);$("scale").dispatchEvent(new Event("input"))});
$("larger").addEventListener("click",()=>{$("scale").value=Math.min(115,+$("scale").value+5);$("scale").dispatchEvent(new Event("input"))});
$("resetPreview").addEventListener("click",()=>switchPage("home"));
$("resetAll").addEventListener("click",reset);
$("exportDesign").addEventListener("click",()=>{const blob=new Blob([JSON.stringify({version:1,design:state},null,2)],{type:"application/json"}),url=URL.createObjectURL(blob),a=document.createElement("a");a.href=url;a.download="seif-custom-phone-design.json";a.click();setTimeout(()=>URL.revokeObjectURL(url),1000);toast("تم تصدير إعدادات التصميم والتطبيقات.")});
setDesign();renderApps();switchPage("home");restoreWallpaper();updateClock();setInterval(updateClock,15000);
})();