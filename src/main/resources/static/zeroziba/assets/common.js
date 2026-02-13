window.ZCommon=(function(){
  function $(s){return document.querySelector(s)}

  let toastBox=null;
  let lastToast='';
  let lastAt=0;

  function ensureToastBox(){
    if(toastBox) return toastBox;
    toastBox=document.createElement('div');
    toastBox.className='toast-wrap';
    document.body.appendChild(toastBox);
    return toastBox;
  }

  function toast(message,type='info'){
    if(!message) return;
    const now=Date.now();
    if(lastToast===message && now-lastAt<900) return;
    lastToast=message; lastAt=now;
    const box=ensureToastBox();
    const el=document.createElement('div');
    el.className=`toast ${type}`;
    el.textContent=message;
    box.appendChild(el);
    setTimeout(()=>el.classList.add('show'),10);
    setTimeout(()=>{
      el.classList.remove('show');
      setTimeout(()=>el.remove(),240);
    },2600);
  }

  function initPageFx(){
    document.querySelectorAll('.panel,.topbar,.hero').forEach((el,i)=>{
      if(el.classList.contains('fade-up')) return;
      el.classList.add('fade-up');
      if(i%3===1) el.classList.add('delay-1');
      if(i%3===2) el.classList.add('delay-2');
    });

    window.addEventListener('kapi:loading',(e)=>{
      document.body.classList.toggle('is-loading', !!e.detail?.active);
    });
  }

  function bindTopAlerts(){
    initPageFx();
    const errBox=$('#topError');const infoBox=$('#topInfo');const errMsg=$('#topErrorMsg');const errAt=$('#topErrorAt');const infoMsg=$('#topInfoMsg');const infoAt=$('#topInfoAt');const logEl=$('#topLog');const clearErr=$('#btnClearErr');const clearInfo=$('#btnClearInfo');
    function render(){
      const st=KApi.state;
      if(errBox){if(st.topError){errBox.style.display='flex';errMsg.textContent=st.topError.message;errAt.textContent='('+st.topError.at+')'}else errBox.style.display='none';}
      if(infoBox){if(st.topInfo){infoBox.style.display='flex';infoMsg.textContent=st.topInfo.message;infoAt.textContent='('+st.topInfo.at+')'}else infoBox.style.display='none';}
      if(logEl){logEl.textContent=JSON.stringify(st.logs.slice(0,30),null,2)}

      const latest=st.logs[0];
      if(latest){
        const msg=latest?.payload?.message || latest?.title;
        const t=/실패|에러|error|unauthorized|권한|로그인/i.test(latest.title+' '+msg)?'error':'info';
        toast(msg,t);
      }
    }
    KApi.sub(render);render();
    clearErr&&clearErr.addEventListener('click',()=>KApi.clearError());
    clearInfo&&clearInfo.addEventListener('click',()=>KApi.clearInfo());
  }

  return {$,bindTopAlerts,toast}
})();