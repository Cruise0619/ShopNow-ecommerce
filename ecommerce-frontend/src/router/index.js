import { createRouter, createWebHistory } from 'vue-router';

const routes = [
  // Client
  { path: '/login', name: 'Login', component: () => import('@/pages/client/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('@/pages/client/Register.vue') },
  { path: '/forgot-password', name: 'ForgotPassword', component: () => import('@/pages/client/ForgotPassword.vue') },
  {
    path: '/', component: () => import('@/layouts/ClientLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/pages/client/Home.vue') },
      { path: 'products', name: 'Products', component: () => import('@/pages/client/Products.vue') },
      { path: 'products/:id', name: 'ProductDetail', component: () => import('@/pages/client/ProductDetail.vue') },
      { path: 'cart', name: 'Cart', component: () => import('@/pages/client/Cart.vue'), meta: { auth: true } },
      { path: 'checkout', name: 'Checkout', component: () => import('@/pages/client/Checkout.vue'), meta: { auth: true } },
      { path: 'orders', name: 'Orders', component: () => import('@/pages/client/Orders.vue'), meta: { auth: true } },
      { path: 'orders/:id', name: 'OrderDetail', component: () => import('@/pages/client/OrderDetail.vue'), meta: { auth: true } },
      { path: 'favorites', name: 'Favorites', component: () => import('@/pages/client/Favorites.vue'), meta: { auth: true } },
      { path: 'profile', name: 'Profile', component: () => import('@/pages/client/Profile.vue'), meta: { auth: true } },
      { path: 'addresses', name: 'Addresses', component: () => import('@/pages/client/Addresses.vue'), meta: { auth: true } },

    ],
  },
  // Public payment (no auth)
  { path: '/pay/:payToken', name: 'Payment', component: () => import('@/pages/client/Payment.vue') },
  // Admin
  { path: '/admin/login', name: 'AdminLogin', component: () => import('@/pages/admin/AdminLogin.vue') },
  {
    path: '/admin', component: () => import('@/layouts/AdminLayout.vue'), meta: { auth: true, admin: true },
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/pages/admin/Dashboard.vue') },
      { path: 'users', name: 'AdminUsers', component: () => import('@/pages/admin/Users.vue') },
      { path: 'products', name: 'AdminProducts', component: () => import('@/pages/admin/Products.vue') },
      { path: 'product-images', name: 'AdminProductImages', component: () => import('@/pages/admin/ProductImages.vue') },
      { path: 'orders', name: 'AdminOrders', component: () => import('@/pages/admin/Orders.vue') },
      { path: 'orders/:id', name: 'AdminOrderDetail', component: () => import('@/pages/admin/OrderDetail.vue') },
      { path: 'categories', name: 'AdminCategories', component: () => import('@/pages/admin/Categories.vue') },
      { path: 'reviews', name: 'AdminReviews', component: () => import('@/pages/admin/Reviews.vue') },
      { path: 'banners', name: 'AdminBanners', component: () => import('@/pages/admin/Banners.vue') },
      { path: 'announcements', name: 'AdminAnnouncements', component: () => import('@/pages/admin/Announcements.vue') },
      { path: 'coupons', name: 'AdminCoupons', component: () => import('@/pages/admin/Coupons.vue') },
      { path: 'flashsales', name: 'AdminFlashSales', component: () => import('@/pages/admin/FlashSales.vue') },
      { path: 'feedbacks', name: 'AdminFeedbacks', component: () => import('@/pages/admin/Feedbacks.vue') },
      { path: 'messages', name: 'AdminMessages', component: () => import('@/pages/admin/Messages.vue') },

      { path: 'profile', name: 'AdminProfile', component: () => import('@/pages/admin/AdminProfile.vue') },
    ],
  },
];

const router = createRouter({ history: createWebHistory(), routes, scrollBehavior: () => ({ top: 0 }) });

const clientAuthPages = ['/login', '/register', '/cart', '/checkout', '/profile', '/favorites', '/addresses']

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');
  const user = JSON.parse(localStorage.getItem('user') || 'null');

  if (to.meta.auth && !token) return next('/login');
  if (to.meta.admin && user?.role !== 'admin') return next('/admin/login');
  if (to.path === '/admin/login' && user?.role === 'admin') return next('/admin/dashboard');

  // Prevent admin-role token from being used on client auth pages
  if (user?.role === 'admin' && !to.path.startsWith('/admin')) {
    const isClientAuthPage = clientAuthPages.some(p => to.path === p || to.path.startsWith('/orders'))
    if (isClientAuthPage) return next('/admin/dashboard')
  }

  if (to.path === '/login' && token && user?.role !== 'admin') return next('/');
  next();
});

export default router;
