/* SunCalc 1.9.0 uses the bundled BSD-2-Clause license. No network requests. */
(()=>{
'use strict';
const key='saerok.appearance.v1';let prefs={manual:'light',auto:false,last:'light'},position=null,timer=0,status='';
try{const p=JSON.parse(localStorage.getItem(key)||'null');if(p&&(p.manual==='light'||p.manual==='dark'))prefs={manual:p.manual,auto:p.auto===true,last:p.last==='dark'?'dark':p.manual}}catch(e){}
let current=prefs.auto?prefs.last:prefs.manual;
const esc=s=>String(s).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
function solarState(now,latitude,longitude){
 const date=new Date(now),noon=new Date(date.getFullYear(),date.getMonth(),date.getDate(),12);
 const times=SunCalc.getTimes(noon,latitude,longitude),rise=+times.sunrise,set=+times.sunset;
 if(!Number.isFinite(rise)||!Number.isFinite(set))return {theme:SunCalc.getPosition(date,latitude,longitude).altitude>-.01454?'light':'dark',sunrise:null,sunset:null,next:null};
 const theme=now>=rise&&now<set?'light':'dark';let next;
 if(now<rise)next=rise;else if(now<set)next=set;else{const tomorrow=new Date(noon);tomorrow.setDate(tomorrow.getDate()+1);next=+SunCalc.getTimes(tomorrow,latitude,longitude).sunrise}
 return {theme,sunrise:rise,sunset:set,next:Number.isFinite(next)?next:null};
}
function save(){try{localStorage.setItem(key,JSON.stringify(prefs))}catch(e){status='설정을 저장하지 못했어요.'}}
function apply(theme,animate=true){
 const changed=current!==theme;current=theme;if(prefs.last!==theme){prefs.last=theme;save()}document.documentElement.dataset.theme=theme;
 try{window.SaerokNative?.setSurfaceDark(theme==='dark')}catch(e){}
 if(changed&&animate&&!matchMedia('(prefers-reduced-motion: reduce)').matches){const stage=document.querySelector('#stage');stage?.animate([{opacity:.65},{opacity:1}],{duration:420,easing:'ease-out'})}
 if(changed)document.dispatchEvent(new CustomEvent('saerokthemechange',{detail:{theme}}));sync();
}
function refresh(animate=true){
 clearTimeout(timer);
 if(prefs.auto&&position){const result=solarState(Date.now(),position.lat,position.lon);apply(result.theme,animate);const time=x=>new Date(x).toLocaleTimeString([],{hour:'2-digit',minute:'2-digit'});status=result.sunrise?'오늘 일출 '+time(result.sunrise)+' · 일몰 '+time(result.sunset)+' · 기기 현지 시각 기준':'현재 지역의 태양 고도로 자동 전환 중';sync();const wait=result.next?Math.min(60000,Math.max(100,result.next-Date.now()+50)):60000;timer=setTimeout(()=>refresh(),wait)}
 else {apply(prefs.auto?current:prefs.manual,animate);if(prefs.auto&&!status)status='자동 전환에는 대략적인 위치가 필요해요.';sync();if(prefs.auto)timer=setTimeout(()=>refresh(),60000)}
}
function nativeAuto(){try{window.SaerokNative?.setSolarAuto(prefs.auto)}catch(e){}}
function requestLocation(){
 if(window.SaerokNative){status='위치를 확인하고 있어요…';sync();window.SaerokNative.requestSunLocation();}
 else if(navigator.geolocation){status='위치를 확인하고 있어요…';sync();navigator.geolocation.getCurrentPosition(p=>receiveLocation(p.coords.latitude,p.coords.longitude),()=>locationError('자동 전환을 위해 위치 권한을 허용해주세요.'),{enableHighAccuracy:false,timeout:15000,maximumAge:1800000})}
 else locationError('위치를 사용할 수 없어요. 일반 또는 다크 모드를 직접 선택해주세요.');
}
function receiveLocation(lat,lon){if(!Number.isFinite(lat)||!Number.isFinite(lon)||Math.abs(lat)>90||Math.abs(lon)>180)return;position={lat,lon};status='';if(prefs.auto)refresh()}
function locationError(message){if(!prefs.auto)return;status=String(message);sync()}
function manual(theme){if(prefs.auto)return;prefs.manual=theme;status='';save();refresh()}
function automatic(){prefs.auto=!prefs.auto;status='';save();nativeAuto();refresh();if(prefs.auto&&!position)requestLocation()}
function sync(){
 for(const input of document.querySelectorAll('input[name=appearance]')){input.checked=input.value===current;input.disabled=prefs.auto;input.closest('label')?.classList.toggle('selected',input.checked)}
 const control=document.querySelector('[data-act=theme-auto]');if(control){control.setAttribute('aria-checked',String(prefs.auto));control.querySelector('.switch-label').textContent=prefs.auto?'켜짐':'꺼짐'}
 const note=document.querySelector('#themeStatus');if(note)note.textContent=prefs.auto?status||'일출·일몰에 따라 자동 전환 중':'원하는 화면을 직접 선택할 수 있어요.';
 const choices=document.querySelector('.theme-choices');if(choices)choices.classList.toggle('automatic',prefs.auto);
 const retry=document.querySelector('[data-act=theme-location]');if(retry)retry.hidden=!prefs.auto||!!position;
 const help=document.querySelector('#themeAutoHelp');if(help)help.textContent=prefs.auto?'자동을 끄면 직접 선택할 수 있어요.':'일출에는 일반 모드, 일몰에는 다크 모드로 바뀝니다.';
}
function settings(){return `<section class="appearance"><h2>화면 모드</h2><p class="note">낮에는 한지의 온기, 밤에는 차분한 먹빛.</p><div class="theme-choices" role="radiogroup" aria-label="화면 모드"><label class="theme-choice"><span class="theme-swatch light-swatch" aria-hidden="true"></span><span>일반 모드 사용</span><input type="radio" name="appearance" value="light" aria-label="일반 모드 사용"></label><label class="theme-choice"><span class="theme-swatch dark-swatch" aria-hidden="true"></span><span>다크 모드 사용</span><input type="radio" name="appearance" value="dark" aria-label="다크 모드 사용"></label></div><div class="setting-row"><div>자동<small id="themeAutoHelp">일출에는 일반 모드, 일몰에는 다크 모드로 바뀝니다.</small></div><button class="solar-switch" type="button" role="switch" aria-checked="${prefs.auto}" aria-label="일출·일몰에 따른 자동 전환" data-act="theme-auto"><span class="switch-track" aria-hidden="true"><span></span></span><span class="switch-label">${prefs.auto?'켜짐':'꺼짐'}</span></button></div><p id="themeStatus" class="note" role="status" aria-live="polite">${esc(status)}</p><button class="btn line" data-act="theme-location" ${!prefs.auto||position?'hidden':''}>자동 전환 위치 확인</button></section>`}
window.SaerokTheme={settings,sync,refresh,receiveLocation,locationError,solarState,get isDark(){return current==='dark'},get preference(){return {...prefs}}};
document.addEventListener('change',e=>{if(e.target.name==='appearance')manual(e.target.value)});
document.addEventListener('click',e=>{const action=e.target.closest('[data-act]')?.dataset.act;if(action==='theme-auto')automatic();else if(action==='theme-location')requestLocation()});
document.addEventListener('visibilitychange',()=>{if(!document.hidden)refresh()});
apply(current,false);nativeAuto();if(prefs.auto){status='위치를 확인한 뒤 자동 전환됩니다.';refresh(false)}
})();
