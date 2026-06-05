import axios from 'axios';
import { ElMessage } from 'element-plus';

const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
});

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

request.interceptors.response.use(
  response => response.data,
  error => {
    const msg = error.response?.data?.message || '网络错误，请稍后重试';
    ElMessage.error(msg);
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      // Only redirect if the current page requires auth
      const path = window.location.pathname;
      const isAuthPage = path.startsWith('/admin') || path === '/cart' || path === '/checkout'
        || path.startsWith('/orders') || path.startsWith('/favorites')
        || path === '/profile' || path === '/addresses' || path === '/messages';
      if (isAuthPage) {
        window.location.href = path.startsWith('/admin') ? '/admin/login' : '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default request;
