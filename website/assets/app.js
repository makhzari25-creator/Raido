
(function(){
 var $=function(i){return document.getElementById(i)},fa=function(n){return Math.round(n).toLocaleString('fa-IR').replace(/,|٬/g,'٬')};
 function verdict(h,t){return h>=t?'g':h>=t*.8?'y':'r'}
 var T={g:['✓ به‌صرفه است'],y:['! نزدیک هدف'],r:['✕ به‌صرفه نیست']};
 // hero capsule
 var tg=$('tg');function hero(){var t=+tg.value,v=verdict(192214,t);$('tgv').textContent=fa(t);$('cv').dataset.v=v;$('cvt').textContent=T[v][0]}
 tg.addEventListener('input',hero);hero();
 // calculator
 function calc(){var g=function(i){return +$(i).value||0},p=g('c1'),km=g('c2'),a=g('c3'),b=g('c4'),c=g('c5'),t=g('c6'),m=a+b;
  var net=p-km*c,h=m>0?net/(m/60):0,v=verdict(h,t);$('rv').dataset.v=v;$('rt').textContent=T[v][0];$('rh').textContent=fa(Math.max(0,h));
  $('rn').textContent='درآمد خالص سفر: '+fa(net)+' تومان، زمان کل: '+fa(m)+' دقیقه'}
 document.querySelectorAll('.fld input').forEach(function(i){i.addEventListener('input',calc)});calc();
 var PL=[{code:'MONTHLY',title:'ماهانه عادی',price:99900,desc:'تحلیل کامل درخواست‌ها · ۳۰ روز'},{code:'YEARLY',title:'سالانه عادی',price:999900,desc:'تحلیل کامل · ۳۶۵ روز'},{code:'PREMIUM_MONTHLY',title:'پرمیوم ماهانه',price:149900,desc:'نشان + محدوده شهری + برگشت خالی · ۳۰ روز'},{code:'PREMIUM_YEARLY',title:'پرمیوم سالانه',price:1499900,desc:'نشان + محدوده شهری + برگشت خالی · ۳۶۵ روز'}];
 function esc(s){return String(s).replace(/[&<>"]/g,function(c){return{'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;'}[c]})}
 function plans(a){$('pl').innerHTML=a.map(function(p){var hot=/YEARLY/.test(p.code)&&!/PREMIUM/.test(p.code);return '<div class="plan'+(hot?' hot':'')+'"><h3>'+esc(p.title)+'</h3><span class="tg">'+(hot?'به‌صرفه‌تر':'&nbsp;')+'</span><div class="amt">'+fa(+p.price)+'<small>تومان</small></div><div class="pr">'+esc(p.desc||'')+'</div><a class="btn '+(hot?'p':'s')+'" href="https://t.me/TripAnalyzerbot">دریافت کد</a></div>'}).join('')}
 plans(PL);
 fetch('https://atzyjiljtzxhctblziti.supabase.co/functions/v1/app-config',{method:'POST',headers:{'content-type':'application/json'},body:'{}'}).then(function(r){return r.ok?r.json():null}).then(function(j){if(j&&Array.isArray(j.plans)&&j.plans.length)plans(j.plans)}).catch(function(){});
 var ap=$('apk');
 fetch('/version.json',{cache:'no-store'}).then(function(r){if(!r.ok)throw 0;return r.json()}).then(function(v){
  $('ver').textContent='نسخه '+String(v.version).replace(/\d/g,function(d){return '۰۱۲۳۴۵۶۷۸۹'[d]}).replace(/\./g,'٫')+' برای اندروید ۸ به بالا';
  $('vm').textContent='حجم '+(v.size/1048576).toLocaleString('fa-IR',{maximumFractionDigits:1})+' مگابایت · امضا با کلید انتشار رایدو · فایل از همین سایت دریافت می‌شود.';
 }).catch(function(){ap.setAttribute('aria-disabled','true');ap.removeAttribute('href');$('vm').textContent='لینک دانلود به‌زودی فعال می‌شود.'});

 // phone screenshots
 var R='<path d="M0 60 Q80 70 140 50 T290 80" stroke="#F5D77A" stroke-width="5" fill="none"/><path d="M60 0 Q70 60 110 120 T170 230" stroke="#fff" stroke-width="4" fill="none"/><path d="M200 0 L215 90 L250 230" stroke="#fff" stroke-width="3" fill="none"/>';
 var W='<path d="M0 20 L60 15 L70 70 L20 90 Z" fill="#A9CCF2"/><path d="M230 150 L290 140 L290 210 L240 215 Z" fill="#B9D6F5"/>';
 var maps=[
  W+R+'<path d="M25 168 Q90 185 150 150 T215 70" stroke="#0A44C8" stroke-width="5" fill="none" stroke-linecap="round"/><rect x="12" y="140" width="22" height="24" rx="5" fill="#0b1b3a"/>',
  R+'<path d="M150 20 Q140 90 125 160 T110 220" stroke="#0A44C8" stroke-width="5" fill="none" stroke-linecap="round"/><rect x="98" y="196" width="22" height="24" rx="5" fill="#0b1b3a"/><path d="M0 120 L50 100 L60 150 L10 170 Z" fill="#BFE0C2"/>',
  W+R+'<path d="M40 190 Q60 140 110 130 T190 60 L215 30" stroke="#3B7BFF" stroke-width="5" fill="none" stroke-linecap="round"/><rect x="204" y="14" width="22" height="24" rx="5" fill="#0b1b3a"/>'];
 var D=[
  {v:'r',t:'این سفر به‌صرفه نیست',p:'۱۱۵٬۰۰۰',h:'۱۹۲٬۲۱۴',z:'داخل محدوده · ۱٫۷ کم (شعاع ۵ کم)',a:'۹ دقیقه تا مبدأ',b:'۱۹ دقیقه',cap:'قرمز',sub:'درآمد ساعتی کمتر از کف شما'},
  {v:'y',t:'نزدیک هدف — با احتیاط',p:'۸۱٬۰۰۰',h:'۲۲۲٬۹۸۸',z:'داخل محدوده · ۱٫۸ کم (شعاع ۵ کم)',a:'۴ دقیقه تا مبدأ',b:'۱۳ دقیقه',cap:'زرد',sub:'نزدیک هدف شما'},
  {v:'g',t:'✓ سفر به‌صرفه است',p:'۱۸۲٬۰۰۰',h:'۳۴۰٬۷۰۴',z:'محدوده شهری (خارج از محل فعالیت)',a:'۵ دقیقه تا مبدأ · ۴ کیلومتر',b:'۲۰ دقیقه · ۱۷ کیلومتر',cap:'سبز',sub:'بالاتر از هدف شما'}];
 $('phr').innerHTML=D.map(function(d,i){return '<figure class="ph-item"><div class="phone" role="img" aria-label="نمونه صفحه گوشی: '+d.t+'، درآمد ساعتی '+d.h+' تومان برای کرایه '+d.p+' تومان" data-i="'+i+'"><div class="map"><svg viewBox="0 0 290 230" preserveAspectRatio="xMidYMid slice" aria-hidden="true">'+maps[i]+'</svg></div>'+
  '<div class="ov" data-v="'+d.v+'"><b>'+d.t+'</b><p>قیمت درخواست: '+d.p+' تومان</p><p>درآمد ساعتی این سفر: '+d.h+' تومان</p><p class="m">منبع مسافت: واقعی</p><p class="m">'+d.z+'</p><p class="m">لمس هرجای صفحه = بستن</p></div>'+
  '<div class="sheet"><div class="pr">'+d.p+'<small>تومان</small></div><div class="rt"><p><i></i><span><b>'+d.a+'</b><br>مبدأ نمونه</span></p><p><i class="e"></i><span><b>'+d.b+'</b><br>مقصد نمونه</span></p></div><div class="dtl">مشاهده جزئیات</div><div class="acc"><span>قبول سفر</span><em></em></div></div></div><figcaption>'+d.cap+'<small>'+d.sub+'</small></figcaption></figure>'}).join('');
 // 3D tilt on pointer
 var ph=document.querySelectorAll('.phone');
 if(matchMedia('(hover:hover) and (prefers-reduced-motion:no-preference)').matches){$('phr').addEventListener('pointermove',function(e){var r=this.getBoundingClientRect(),x=(e.clientX-r.left)/r.width-.5,y=(e.clientY-r.top)/r.height-.5;ph.forEach(function(p,i){p.style.setProperty('--ry',(x*22-(i-1)*8)+'deg');p.style.setProperty('--rx',(-y*8)+'deg')})});
  $('phr').addEventListener('pointerleave',function(){ph.forEach(function(p){p.style.setProperty('--ry','0deg');p.style.setProperty('--rx','0deg')})})}
})();
// 3D hero: بارگذاری تنبل، فقط وقتی مرورگر آزاد است و کاربر صرفه‌جویی داده را روشن نکرده
(function(){var c=navigator.connection||{};if(c.saveData||/2g/.test(c.effectiveType||'')||matchMedia('(prefers-reduced-data:reduce)').matches)return;
 function go(){import('/assets/hero3d.js').catch(function(){})}
 if(document.readyState==='complete')setTimeout(go,300);else addEventListener('load',function(){(window.requestIdleCallback||setTimeout)(go,{timeout:1500})})})();
