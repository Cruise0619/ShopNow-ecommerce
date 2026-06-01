import request from '@/utils/request';

// Auth
export const authAPI = { login: d => request.post('/auth/login', d), register: d => request.post('/auth/register', d), sendCode: d => request.post('/auth/send-code', d), forgotPassword: d => request.post('/auth/forgot-password', d), resetPassword: d => request.post('/auth/reset-password', d) };

// Products
export const productAPI = { list: p => request.get('/products', { params: p }), detail: id => request.get(`/products/${id}`) };

// Cart (handled by store)

// Orders
export const orderAPI = { list: p => request.get('/orders', { params: p }), create: d => request.post('/orders', d), detail: id => request.get(`/orders/${id}`), cancel: id => request.put(`/orders/${id}/cancel`), confirm: id => request.put(`/orders/${id}/confirm`), pay: id => request.put(`/orders/${id}/pay`), refund: id => request.put(`/orders/${id}/refund`) };

// Favorites
export const favAPI = { list: () => request.get('/favorites'), toggle: id => request.post(`/favorites/${id}`) };

// Reviews
export const reviewAPI = { list: p => request.get('/reviews', { params: p }), create: d => request.post('/reviews', d) };

// Addresses
export const addrAPI = { list: () => request.get('/addresses'), create: d => request.post('/addresses', d), update: (id, d) => request.put(`/addresses/${id}`, d), remove: id => request.delete(`/addresses/${id}`), setDefault: id => request.put(`/addresses/${id}/default`) };

// Coupons
export const couponAPI = { list: () => request.get('/coupons'), claim: id => request.post(`/coupons/${id}/claim`) };

// Profile
export const profileAPI = { get: () => request.get('/profile'), update: d => request.put('/profile', d), changePassword: d => request.put('/profile/password', d), uploadAvatar: d => request.post('/profile/avatar', d) };

// Banners & Announcements
export const homeAPI = { banners: () => request.get('/banners'), announcements: () => request.get('/announcements') };

// Admin
export const adminAPI = {
  dashboard: () => request.get('/admin/dashboard'),
  users: p => request.get('/admin/users', { params: p }),
  userCreate: d => request.post('/admin/users', d),
  userDetail: id => request.get(`/admin/users/${id}`),
  userUpdate: (id, d) => request.put(`/admin/users/${id}`, d),
  userDelete: id => request.delete(`/admin/users/${id}`),
  userStatus: (id, s) => request.put(`/admin/users/${id}/status`, { status: s }),
  categories: () => request.get('/admin/categories'),
  categoryCreate: d => request.post('/admin/categories', d),
  categoryUpdate: (id, d) => request.put(`/admin/categories/${id}`, d),
  categoryDelete: id => request.delete(`/admin/categories/${id}`),
  products: p => request.get('/admin/products', { params: p }),
  productCreate: d => request.post('/admin/products', d),
  productUpdate: (id, d) => request.post(`/admin/products/${id}`, d),
  productDelete: id => request.delete(`/admin/products/${id}`),
  orders: p => request.get('/admin/orders', { params: p }),
  orderDetail: id => request.get(`/admin/orders/${id}`),
  orderShip: (id, d) => request.put(`/admin/orders/${id}/ship`, d),
  orderCancel: id => request.put(`/admin/orders/${id}/cancel`),
  orderRefund: id => request.put(`/admin/orders/${id}/refund`),
  reviews: p => request.get('/admin/reviews', { params: p }),
  reviewDelete: id => request.delete(`/admin/reviews/${id}`),
  banners: () => request.get('/admin/banners'),
  bannerCreate: d => request.post('/admin/banners', d),
  bannerUpdate: (id, d) => request.put(`/admin/banners/${id}`, d),
  bannerDelete: id => request.delete(`/admin/banners/${id}`),
  bannerUploadImage: d => request.post('/admin/banners/upload-image', d),
  announcements: () => request.get('/admin/announcements'),
  annoCreate: d => request.post('/admin/announcements', d),
  annoUpdate: (id, d) => request.put(`/admin/announcements/${id}`, d),
  annoDelete: id => request.delete(`/admin/announcements/${id}`),
  coupons: () => request.get('/admin/coupons'),
  couponCreate: d => request.post('/admin/coupons', d),
  couponUpdate: (id, d) => request.put(`/admin/coupons/${id}`, d),
  couponDelete: id => request.delete(`/admin/coupons/${id}`),
  feedbacks: p => request.get('/admin/feedbacks', { params: p }),
  feedbackReply: (id, d) => request.put(`/admin/feedbacks/${id}/reply`, d),
  messageConversations: p => request.get('/admin/messages/conversations', { params: p }),
  messageDetail: userId => request.get(`/admin/messages/${userId}`),
  messageReply: (userId, d) => request.post(`/admin/messages/${userId}/reply`, { content: d }),
};

// Flash Sales
export const flashsaleAPI = { list: () => request.get('/flashsales') };

// Public payment (no auth required)
export const publicAPI = {
  getPaymentInfo: payToken => request.get(`/public/orders/${payToken}`),
  executePay: payToken => request.put(`/public/orders/${payToken}/pay`),
};

// Messages (客服)
export const messageAPI = {
  list: () => request.get('/messages'),
  send: content => request.post('/messages', { content }),
};
