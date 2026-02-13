async function api(url, options = {}) {
  const res = await fetch(url, { credentials:'include', headers:{'Content-Type':'application/json', ...(options.headers||{})}, ...options });
  const body = await res.json().catch(() => ({ success:false, message:`JSON 응답 아님 (${res.status})` }));
  if (!res.ok || body.success === false) throw new Error(body.message || `HTTP ${res.status}`);
  return body.data;
}
const KApi = {
  me:()=>api('/api/me'), mePermissions:()=>api('/api/me/permissions'), login:(p)=>api('/api/login',{method:'POST',body:JSON.stringify(p)}), logout:()=>api('/api/logout',{method:'POST'}),
  signup:(p)=>api('/api/users',{method:'POST',body:JSON.stringify(p)}),
  getUser:(id)=>api(`/api/users/${id}`),
  posts:()=>api('/api/posts?page=1&limit=50'), post:(id)=>api(`/api/posts/${id}`), createPost:(p)=>api('/api/posts',{method:'POST',body:JSON.stringify(p)}), deletePost:(id)=>api(`/api/posts/${id}`,{method:'DELETE'}),
  comments:(postId)=>api(`/api/posts/${postId}/comments?limit=50`), createComment:(postId,content)=>api(`/api/posts/${postId}/comments`,{method:'POST',body:JSON.stringify({content})})
};