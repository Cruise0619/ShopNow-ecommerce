import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import request from '@/utils/request';

export const useUserStore = defineStore('user', () => {
  const user = ref(null);
  const token = ref('');

  const isLoggedIn = computed(() => !!token.value);
  const isAdmin = computed(() => user.value?.role === 'admin');

  function loadFromStorage() {
    token.value = localStorage.getItem('token') || '';
    const saved = localStorage.getItem('user');
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        // Detect and clean plugin-persisted format: { user: {...}, token: "..." }
        if (parsed && typeof parsed === 'object' && 'token' in parsed && 'user' in parsed) {
          // Plugin format detected — extract the real user and token
          token.value = parsed.token || '';
          user.value = parsed.user || null;
          // Overwrite corrupted data with clean format
          localStorage.setItem('token', token.value);
          if (user.value) {
            localStorage.setItem('user', JSON.stringify(user.value));
          } else {
            localStorage.removeItem('user');
          }
        } else {
          user.value = parsed;
        }
      } catch { user.value = null; }
    }
  }

  async function login(data) {
    const res = await request.post('/auth/login', data);
    if (res.code === 200) {
      token.value = res.data.token;
      user.value = res.data.user;
      localStorage.setItem('token', res.data.token);
      localStorage.setItem('user', JSON.stringify(res.data.user));
    }
    return res;
  }

  async function register(data) {
    const res = await request.post('/auth/register', data);
    if (res.code === 200) {
      token.value = res.data.token;
      user.value = res.data.user;
      localStorage.setItem('token', res.data.token);
      localStorage.setItem('user', JSON.stringify(res.data.user));
    }
    return res;
  }

  function logout() {
    token.value = '';
    user.value = null;
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  }

  return { user, token, isLoggedIn, isAdmin, loadFromStorage, login, register, logout };
}, {
  persist: false
});
