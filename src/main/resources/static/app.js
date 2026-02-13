const $ = (s) => document.querySelector(s);
const state = { me: null, posts: [], filtered: [], selectedPost: null, comments: [] };

async function req(url, options = {}) {
  const res = await fetch(url, { credentials: 'include', headers: { 'Content-Type': 'application/json', ...(options.headers||{}) }, ...options });
  const body = await res.json().catch(() => ({ success:false, message:`JSON 응답 아님 (${res.status})` }));
  if (!res.ok || body.success === false) throw new Error(body.message || `HTTP ${res.status}`);
  return body.data;
}

async function boot() {
  await loadMe();
  await loadPosts();
  bindEvents();
  renderAll();
}

function bindEvents() {
  $('#btnRefresh').onclick = async () => { await loadPosts(); renderPosts(); };
  $('#btnSearch').onclick = filterPosts;
  $('#q').addEventListener('keydown', (e) => e.key === 'Enter' && filterPosts());
  $('#btnCreatePost').onclick = createPost;
}

async function loadMe() { try { state.me = await req('/api/me'); } catch { state.me = null; } }
async function loadPosts() {
  try {
    state.posts = await req('/api/posts?page=1&limit=50');
    state.posts.sort((a,b)=>(b.id||0)-(a.id||0));
    state.filtered = state.posts;
  } catch(e) { alert(e.message); }
}

function filterPosts() {
  const q = $('#q').value.trim().toLowerCase();
  state.filtered = !q ? state.posts : state.posts.filter(p => (p.title||'').toLowerCase().includes(q));
  renderPosts();
}

function renderAll() {
  renderAuthArea();
  renderAuthPanel();
  renderPosts();
  $('#writePanel').classList.toggle('hidden', !state.me);
}

function renderAuthArea() {
  const el = $('#authArea');
  el.innerHTML = state.me
    ? `${state.me.nickname || state.me.username} 님 <button id='btnLogout'>로그아웃</button>`
    : `<span class='muted'>비로그인</span>`;
  const btn = $('#btnLogout');
  if (btn) btn.onclick = async () => { await req('/api/logout', { method:'POST' }); await loadMe(); renderAll(); };
}

function renderAuthPanel() {
  const el = $('#authPanel');
  if (state.me) {
    el.innerHTML = `<h3>내 정보</h3><p><b>${state.me.nickname || '-'}</b> (@${state.me.username})</p><p class='muted'>${state.me.email || '이메일 미등록'}</p>`;
    return;
  }
  el.innerHTML = `
    <h3>로그인</h3>
    <input id='loginId' placeholder='아이디'>
    <input id='loginPw' type='password' placeholder='비밀번호'>
    <button id='btnLogin' class='primary'>로그인</button>
    <hr>
    <h3>회원가입</h3>
    <input id='suId' placeholder='아이디'>
    <div class='muted'>4~20자, 영문/숫자</div>
    <input id='suPw' type='password' placeholder='비밀번호'>
    <div class='muted'>4~50자, 공백 불가</div>
    <input id='suPw2' type='password' placeholder='비밀번호 확인'>
    <input id='suNick' placeholder='닉네임'>
    <div class='muted'>2~20자, 공백 불가</div>
    <input id='suEmail' placeholder='이메일(선택)'>
    <button id='btnSignup'>회원가입</button>`;

  $('#btnLogin').onclick = async () => {
    try {
      await req('/api/login', { method:'POST', body: JSON.stringify({ username: $('#loginId').value, password: $('#loginPw').value }) });
      await loadMe(); renderAll();
    } catch(e) { alert(e.message); }
  };

  $('#btnSignup').onclick = async () => {
    const pw = $('#suPw').value, pw2 = $('#suPw2').value;
    if (pw !== pw2) return alert('비밀번호 확인이 일치하지 않습니다.');
    try {
      await req('/api/users', { method:'POST', body: JSON.stringify({ username: $('#suId').value, password: pw, nickname: $('#suNick').value, email: $('#suEmail').value || null }) });
      alert('회원가입 완료. 로그인 해주세요.');
    } catch(e) { alert(e.message); }
  };
}

function renderPosts() {
  const tbody = $('#postTbody');
  tbody.innerHTML = '';
  state.filtered.forEach(p => {
    const tr = document.createElement('tr');
    tr.className = 'post-row';
    tr.innerHTML = `<td>${p.id}</td><td class='title-cell'>${p.title}</td><td>${p.userId}</td><td class='muted'>-</td><td>${p.commentsCnt ?? 0}</td>`;
    tr.onclick = () => openPost(p.id);
    tbody.appendChild(tr);
  });
}

async function openPost(id) {
  try {
    state.selectedPost = await req(`/api/posts/${id}`);
    state.comments = await req(`/api/posts/${id}/comments?limit=50`);
    renderPostDetail();
  } catch(e) { alert(e.message); }
}

function renderPostDetail() {
  const box = $('#postDetail');
  const p = state.selectedPost;
  if (!p) return box.classList.add('hidden');

  box.classList.remove('hidden');
  box.innerHTML = `
    <h3>${p.title}</h3>
    <p class='muted'>글번호 #${p.id} · 작성자 ${p.userId}</p>
    <p>${(p.content||'').replace(/\n/g,'<br>')}</p>
    ${state.me && state.me.id === p.userId ? `<button id='btnDeletePost' class='btn-danger'>글 삭제</button>` : ''}
    <hr>
    <h4>댓글</h4>
    <div id='commentList'></div>
    ${state.me ? `<input id='commentInput' placeholder='댓글 입력'><button id='btnAddComment' class='primary'>댓글 등록</button>` : `<p class='muted'>댓글 작성은 로그인 후 가능합니다.</p>`}
  `;

  const list = $('#commentList');
  list.innerHTML = state.comments.length
    ? state.comments.map(c => `<div class='comment-item'>#${c.id} · ${c.content}</div>`).join('')
    : `<p class='muted'>댓글이 없습니다.</p>`;

  const delBtn = $('#btnDeletePost');
  if (delBtn) delBtn.onclick = async () => {
    if (!confirm('이 글을 삭제할까요?')) return;
    try {
      await req(`/api/posts/${p.id}`, { method:'DELETE' });
      box.classList.add('hidden');
      await loadPosts();
      renderPosts();
    } catch(e) { alert(e.message); }
  };

  const addBtn = $('#btnAddComment');
  if (addBtn) addBtn.onclick = async () => {
    try {
      await req(`/api/posts/${p.id}/comments`, { method:'POST', body: JSON.stringify({ content: $('#commentInput').value }) });
      await openPost(p.id);
      await loadPosts();
      renderPosts();
    } catch(e) { alert(e.message); }
  };
}

async function createPost() {
  try {
    await req('/api/posts', { method:'POST', body: JSON.stringify({ title: $('#postTitle').value, content: $('#postContent').value }) });
    $('#postTitle').value=''; $('#postContent').value='';
    await loadPosts(); renderPosts();
  } catch(e) { alert(e.message); }
}

boot();
