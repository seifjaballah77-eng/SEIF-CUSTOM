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
let state=load(),wallUrl=null;
function validUrl(s){try{const u=new URL(s);return ["http:","https:"].includes(u.protocol)?u.href:null}catch(_){return null}}
function load(){try{const x=JSON.parse(localStorage.getItem(KEY)||"null")||{};return{...defaults,...x,apps:Array.isArray(x.apps)?x.apps.filter(a=>a&&typeof a.name==="string"&&validUrl(a.url)).slice(0,24):defaults.apps.map(a=>({...a}))}}catch(_){return structuredClone(defaults)}}
function save(){localStorage.setItem(KEY,JSON.stringify(state))}
function toast(t){$("toast").textContent=t;$("toast").classList.add("show");setTimeout(()=>$("toast").classList.remove("show"),2200)}
function safeText(t){return String(t||"").trim().slice(0,22)}
function setDesign(){const root=document.documentElement;root.style.setProperty("--accent",state.accent);root.style.setProperty("--glass",state.glass/100);root.style.setProperty("--wall-opacity",state.opacity/100);root.style.setProperty("--icon-radius",({soft:"17px",round:"50%",pill:"999px",sharp:"5px"})[state.shape]);$("deviceScreen").style.backgroundColor=state.wallpaperColor;$("phoneWallpaper").style.backgroundColor=state.wallpaperColor;$("phoneWallpaper").style.backgroundImage=wallUrl?"url("+wallUrl+")":"linear-gradient(150deg,#253c61,#121a30 56%,#4a315c)";$("accent").value=state.accent;$("wallpaperColor").value=state.wallpaperColor;$("glass").value=state.glass;$("glassOut").textContent=state.glass+"%";$("opacity").value=state.opacity;$("opacityOut").textContent=state.opacity+"%";$("shape").value=state.shape;$("scale").value=state.scale;$("scaleOut").textContent=state.scale+"%";const d=$("device");d.style.transform="scale("+state.scale/100+")";d.style.transformOrigin="top center";$("deviceStage")?.style.setProperty("--unused","");}
function iconFor(a){const el=document.createElement("span");el.className="app-icon";el.textContent=a.symbol||safeText(a.name).charAt(0).toUpperCase();return el}
function makeApp(a){const link=document.createElement("a");link.className="phone-app";link.href=a.url;link.target="_blank";link.rel="noopener noreferrer";link.title="فتح رابط "+a.name;link.append(iconFor(a));const name=document.createElement("span");name.className="app-label";name.textContent=a.name;link.append(name);return link}
function renderApps(){const home=$("homeApps"),all=$("allApps"),dock=$("phoneDock");home.replaceChildren();all.replaceChildren();dock.replaceChildren();state.apps.slice(0,8).forEach(a=>home.append(makeApp(a)));state.apps.forEach(a=>all.append(makeApp(a)));state.apps.slice(0,4).forEach(a=>dock.append(iconFor(a)));$("appManager").replaceChildren();state.apps.forEach((a,i)=>{const row=document.createElement("div");row.className="managed-app";const link=document.createElement("a");link.href=a.url;link.target="_blank";link.rel="noopener noreferrer";link.textContent=a.name;const remove=document.createElement("button");remove.type="button";remove.textContent="إزالة";remove.addEventListener("click",()=>{state.apps.splice(i,1);save();renderApps()});row.append(link,remove);$("appManager").append(row)})}
function switchPage(name){document.querySelectorAll(".phone-page").forEach(p=>p.classList.toggle("active",p.id==="page-"+name));document.querySelectorAll(".phone-nav button").forEach(b=>b.classList.toggle("active",b.dataset.page===name))}
function updateClock(){const d=new Date(),time=new Intl.DateTimeFormat("ar-TN",{hour:"2-digit",minute:"2-digit",hour12:false}).format(d),date=new Intl.DateTimeFormat("ar-TN",{weekday:"long",day:"numeric",month:"long"}).format(d);$("phoneClock").textContent=time;$("phoneTime").textContent=time;$("widgetClock").textContent=time;$("phoneDay").textContent=date;$("widgetDate").textContent=date}
function reset(){state={...defaults,apps:defaults.apps.map(a=>({...a}))};wallUrl=null;$("wallpaperFile").value="";save();setDesign();renderApps();switchPage("home");toast("تمت استعادة التصميم الافتراضي")}
$("accent").addEventListener("input",e=>{state.accent=e.target.value;save();setDesign()});
$("wallpaperColor").addEventListener("input",e=>{state.wallpaperColor=e.target.value;save();setDesign()});
$("glass").addEventListener("input",e=>{state.glass=+e.target.value;save();setDesign()});
$("opacity").addEventListener("input",e=>{state.opacity=+e.target.value;save();setDesign()});
$("shape").addEventListener("change",e=>{state.shape=e.target.value;save();setDesign()});
$("scale").addEventListener("input",e=>{state.scale=+e.target.value;save();setDesign()});
$("wallpaperFile").addEventListener("change",e=>{const file=e.target.files[0];if(!file)return;if(!file.type.startsWith("image/")){toast("اختر ملف صورة.");return}if(wallUrl)URL.revokeObjectURL(wallUrl);wallUrl=URL.createObjectURL(file);$("phoneWallpaper").style.backgroundImage="url("+wallUrl+")";toast("تم تطبيق الخلفية على المعاينة فقط.")});
$("clearWallpaper").addEventListener("click",()=>{if(wallUrl)URL.revokeObjectURL(wallUrl);wallUrl=null;$("wallpaperFile").value="";setDesign()});
$("addApp").addEventListener("click",()=>{const name=safeText($("appName").value),url=validUrl($("appUrl").value.trim());if(!name||!url){toast("أدخل اسم التطبيق ورابطاً صحيحاً يبدأ بـ https://");return}if(state.apps.length>=24){toast("وصلت إلى الحد الأقصى: 24 تطبيقاً.");return}state.apps.push({name,url,symbol:name.charAt(0).toUpperCase()});$("appName").value="";$("appUrl").value="";save();renderApps();toast("أضيف التطبيق كرابط قابل للفتح.")});
document.querySelectorAll(".phone-nav button").forEach(b=>b.addEventListener("click",()=>switchPage(b.dataset.page)));
$("smaller").addEventListener("click",()=>{$("scale").value=Math.max(75,+$("scale").value-5);$("scale").dispatchEvent(new Event("input"))});
$("larger").addEventListener("click",()=>{$("scale").value=Math.min(115,+$("scale").value+5);$("scale").dispatchEvent(new Event("input"))});
$("resetPreview").addEventListener("click",()=>switchPage("home"));
$("resetAll").addEventListener("click",reset);
$("exportDesign").addEventListener("click",()=>{const blob=new Blob([JSON.stringify({version:1,design:state},null,2)],{type:"application/json"}),url=URL.createObjectURL(blob),a=document.createElement("a");a.href=url;a.download="seif-custom-phone-design.json";a.click();setTimeout(()=>URL.revokeObjectURL(url),1000);toast("تم تصدير إعدادات التصميم والتطبيقات.")});
setDesign();renderApps();switchPage("home");updateClock();setInterval(updateClock,15000);
})();