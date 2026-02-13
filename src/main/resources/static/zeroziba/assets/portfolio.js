(function(){
  const els=[...document.querySelectorAll('[data-count]')];
  if(!els.length) return;

  const io=new IntersectionObserver((entries)=>{
    entries.forEach(e=>{
      if(!e.isIntersecting) return;
      const el=e.target;
      const target=Number(el.dataset.count||0);
      const dur=900;
      const start=performance.now();
      const from=0;
      const suffix=target===99?'%':target===120?'+':'';
      function tick(now){
        const p=Math.min(1,(now-start)/dur);
        const eased=1-Math.pow(1-p,3);
        el.textContent=Math.floor(from+(target-from)*eased)+suffix;
        if(p<1) requestAnimationFrame(tick);
      }
      requestAnimationFrame(tick);
      io.unobserve(el);
    });
  },{threshold:.35});

  els.forEach(el=>io.observe(el));
})();