window.KApi=(function(){
  const state={logs:[],topError:null,topInfo:null};
  const listeners=[];
  let pending=0;

  function setLoading(active){
    window.dispatchEvent(new CustomEvent('kapi:loading',{detail:{active}}));
  }
  function begin(){ pending+=1; if(pending===1) setLoading(true); }
  function end(){ pending=Math.max(0,pending-1); if(pending===0) setLoading(false); }

  function emit(){listeners.forEach(fn=>fn())}
  function sub(fn){listeners.push(fn);return()=>{const i=listeners.indexOf(fn);if(i>=0)listeners.splice(i,1)}}
  function push(title,payload){const at=new Date().toLocaleTimeString();state.logs.unshift({at,title,payload});const msg=payload?.message||payload?.body?.message||title;if(/실패|에러|error|unauthorized|권한|로그인/i.test(title+' '+msg))state.topError={at,message:msg};else state.topInfo={at,message:msg};emit();}

  async function api(url,opt={}){
    begin();
    try{
      const res=await fetch(url,{credentials:'include',headers:{'Content-Type':'application/json',...(opt.headers||{})},...opt});
      let body;
      try{body=await res.json()}catch{body={success:false,message:`JSON 아님 (${res.status})`}};
      if(!res.ok||body.success===false){const e=new Error(body.message||`HTTP ${res.status}`);e.body=body;throw e;}
      return body;
    } finally { end(); }
  }

  return {
    state,sub,push,clearError(){state.topError=null;emit()},clearInfo(){state.topInfo=null;emit()},
    async signup(p){try{const r=await api('/api/users',{method:'POST',body:JSON.stringify(p)});push('회원가입 성공',r);return r;}catch(e){push('회원가입 실패',{message:e.message,body:e.body});throw e}},
    async login(p){try{const r=await api('/api/login',{method:'POST',body:JSON.stringify(p)});push('로그인 성공',r);return r;}catch(e){push('로그인 실패',{message:e.message,body:e.body});throw e}},
    async logout(){try{const r=await api('/api/logout',{method:'POST'});push('로그아웃 성공',r);return r;}catch(e){push('로그아웃 실패',{message:e.message,body:e.body});throw e}},
    async me(){try{const r=await api('/api/me');push('내정보 조회',r);return r;}catch(e){push('내정보 실패',{message:e.message,body:e.body});throw e}},
    async mePermissions(){const r=await api('/api/me/permissions');return r.data||{admin:false}},
    async posts(){const r=await api('/api/posts?page=1&limit=50');push('게시글 목록',{count:(r.data||[]).length});return r.data||[]},
    async post(id){const r=await api(`/api/posts/${id}`);push('게시글 단건',{id});return r.data},
    async createPost(p){const r=await api('/api/posts',{method:'POST',body:JSON.stringify(p)});push('글 작성 성공',r);return r.data},
    async deletePost(id){const r=await api(`/api/posts/${id}`,{method:'DELETE'});push('글 삭제 성공',{id});return r?.data},
    async comments(postId){const r=await api(`/api/posts/${postId}/comments?limit=50`);push('댓글 조회',{postId,count:(r.data||[]).length});return r.data||[]},
    async createComment(postId,content){const r=await api(`/api/posts/${postId}/comments`,{method:'POST',body:JSON.stringify({content})});push('댓글 작성 성공',r);return r.data}
  }
})();