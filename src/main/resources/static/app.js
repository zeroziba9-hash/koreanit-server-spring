const { createApp, computed } = Vue;

createApp({
  setup() {
    const savedTheme = localStorage.getItem('koreanit-theme');

    const state = Vue.reactive({
      isDark: savedTheme === 'dark',
      isLoggedIn: false,
      signupForm: { username: '', password: '', nickname: '', email: '' },
      loginForm: { username: '', password: '' },
      postForm: { title: '', content: '' },
      commentForm: { postId: null, content: '' },
      selectedPostId: null,
      posts: [],
      comments: [],
      logs: [],
      topError: null,
      topInfo: null
    });

    function syncThemeClass() {
      document.documentElement.classList.toggle('dark', state.isDark);
      localStorage.setItem('koreanit-theme', state.isDark ? 'dark' : 'light');
    }
    syncThemeClass();

    function toggleDark() {
      state.isDark = !state.isDark;
      syncThemeClass();
    }

    function pushLog(title, payload) {
      const at = new Date().toLocaleTimeString();
      state.logs.unshift({ at, title, payload });

      const msg = payload?.message || payload?.body?.message || title;
      if (/실패|에러|error|unauthorized|권한|로그인/i.test(title + ' ' + msg)) {
        state.topError = { at, message: msg };
      } else {
        state.topInfo = { at, message: msg };
      }
    }

    function clearTopError() {
      state.topError = null;
    }

    async function api(url, options = {}) {
      const res = await fetch(url, {
        credentials: 'include',
        headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
        ...options
      });
      let body;
      try { body = await res.json(); }
      catch { body = { success: false, message: `JSON 아님 (${res.status})`, data: null, code: 'NON_JSON' }; }

      if (!res.ok || body.success === false) {
        const err = new Error(body.message || `HTTP ${res.status}`);
        err.body = body;
        throw err;
      }
      return body;
    }

    async function signup() {
      try {
        const payload = {
          username: state.signupForm.username,
          password: state.signupForm.password,
          nickname: state.signupForm.nickname,
          email: state.signupForm.email || null
        };
        const r = await api('/api/users', { method: 'POST', body: JSON.stringify(payload) });
        pushLog('회원가입 성공', r);
        state.signupForm = { username: '', password: '', nickname: '', email: '' };
      } catch (e) {
        pushLog('회원가입 실패', { message: e.message, body: e.body });
        alert(`회원가입 실패: ${e.message}`);
      }
    }

    async function login() {
      try {
        const r = await api('/api/login', { method: 'POST', body: JSON.stringify(state.loginForm) });
        state.isLoggedIn = true;
        pushLog('로그인 성공', r);
        await refreshPosts();
      } catch (e) {
        state.isLoggedIn = false;
        pushLog('로그인 실패', { message: e.message, body: e.body });
        alert(`로그인 실패: ${e.message}`);
      }
    }

    async function logout() {
      try {
        const r = await api('/api/logout', { method: 'POST' });
        state.isLoggedIn = false;
        pushLog('로그아웃 성공', r);
      } catch (e) {
        pushLog('로그아웃 실패', { message: e.message, body: e.body });
      }
    }

    async function fetchMe() {
      try {
        const r = await api('/api/me');
        state.isLoggedIn = true;
        pushLog('내 정보 조회 성공', r);
      } catch (e) {
        state.isLoggedIn = false;
        pushLog('내 정보 조회 실패', { message: e.message, body: e.body });
      }
    }

    async function refreshPosts() {
      try {
        const r = await api('/api/posts?page=1&limit=50');
        state.posts = r.data || [];
        if (state.selectedPostId) {
          const exists = state.posts.find(p => p.id === state.selectedPostId);
          if (!exists) state.selectedPostId = null;
        }
        pushLog('게시글 목록 조회', { count: state.posts.length });
      } catch (e) {
        pushLog('게시글 목록 실패', { message: e.message, body: e.body });
      }
    }

    function selectPost(post) {
      state.selectedPostId = post.id;
      state.commentForm.postId = post.id;
      loadComments(post.id);
    }

    async function createPost() {
      try {
        const r = await api('/api/posts', { method: 'POST', body: JSON.stringify(state.postForm) });
        pushLog('게시글 작성 성공', r);
        state.postForm = { title: '', content: '' };
        await refreshPosts();
      } catch (e) {
        pushLog('게시글 작성 실패', { message: e.message, body: e.body });
        alert(`게시글 작성 실패: ${e.message}`);
      }
    }

    async function loadComments(postId = state.selectedPostId) {
      try {
        if (!postId) {
          state.comments = [];
          return;
        }
        const r = await api(`/api/posts/${postId}/comments?limit=50`);
        state.comments = r.data || [];
        pushLog(`댓글 조회`, { postId, count: state.comments.length });
      } catch (e) {
        pushLog('댓글 조회 실패', { message: e.message, body: e.body });
      }
    }

    async function createComment() {
      try {
        const postId = state.commentForm.postId;
        const r = await api(`/api/posts/${postId}/comments`, {
          method: 'POST',
          body: JSON.stringify({ content: state.commentForm.content })
        });
        pushLog('댓글 작성 성공', r);
        state.commentForm.content = '';
        state.selectedPostId = postId;
        await loadComments(postId);
        await refreshPosts();
      } catch (e) {
        pushLog('댓글 작성 실패', { message: e.message, body: e.body });
        alert(`댓글 작성 실패: ${e.message}`);
      }
    }

    const selectedPost = computed(() => state.posts.find(p => p.id === state.selectedPostId) || null);
    const prettyLogs = computed(() => JSON.stringify(state.logs, null, 2));

    refreshPosts();

    return {
      ...Vue.toRefs(state),
      selectedPost,
      prettyLogs,
      toggleDark,
      clearTopError,
      signup,
      login,
      logout,
      fetchMe,
      refreshPosts,
      selectPost,
      createPost,
      createComment
    };
  }
}).mount('#app');