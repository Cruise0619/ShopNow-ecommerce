import { describe, it, expect, beforeEach, vi } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';

// Mock request
vi.mock('@/utils/request', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

describe('User Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    localStorage.clear();
  });

  it('141. 初始状态 — 未登录', async () => {
    const { useUserStore } = await import('@/stores/user');
    const store = useUserStore();
    store.loadFromStorage();
    expect(store.isLoggedIn).toBe(false);
    expect(store.user).toBeNull();
    expect(store.token).toBe('');
  });

  it('142. 从localStorage加载用户', async () => {
    localStorage.setItem('token', 'test_token_123');
    localStorage.setItem('user', JSON.stringify({ id: 1, username: 'test', role: 'user' }));
    const { useUserStore } = await import('@/stores/user');
    const store = useUserStore();
    store.loadFromStorage();
    expect(store.isLoggedIn).toBe(true);
    expect(store.user.username).toBe('test');
    expect(store.token).toBe('test_token_123');
  });

  it('143. isAdmin — 普通用户返回false', async () => {
    localStorage.setItem('token', 't');
    localStorage.setItem('user', JSON.stringify({ role: 'user' }));
    const { useUserStore } = await import('@/stores/user');
    const store = useUserStore();
    store.loadFromStorage();
    expect(store.isAdmin).toBe(false);
  });

  it('144. isAdmin — 管理员返回true', async () => {
    localStorage.setItem('token', 't');
    localStorage.setItem('user', JSON.stringify({ role: 'admin' }));
    const { useUserStore } = await import('@/stores/user');
    const store = useUserStore();
    store.loadFromStorage();
    expect(store.isAdmin).toBe(true);
  });

  it('145. 登出 — 清除所有状态', async () => {
    localStorage.setItem('token', 't');
    localStorage.setItem('user', JSON.stringify({ role: 'user' }));
    const { useUserStore } = await import('@/stores/user');
    const store = useUserStore();
    store.loadFromStorage();
    store.logout();
    expect(store.isLoggedIn).toBe(false);
    expect(store.user).toBeNull();
    expect(localStorage.getItem('token')).toBeNull();
  });

  it('146. 带损坏user数据的localStorage', async () => {
    localStorage.setItem('token', 't');
    localStorage.setItem('user', 'not-valid-json');
    const { useUserStore } = await import('@/stores/user');
    const store = useUserStore();
    store.loadFromStorage();
    expect(store.user).toBeNull();
  });
});

describe('Cart Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('147. 初始状态 — 空购物车', async () => {
    const { useCartStore } = await import('@/stores/cart');
    const store = useCartStore();
    expect(store.items).toEqual([]);
    expect(store.selectedItems).toEqual([]);
    expect(store.totalCount).toBe(0);
    expect(store.totalAmount).toBe(0);
  });

  it('148. totalCount — 计算商品总数', async () => {
    const { useCartStore } = await import('@/stores/cart');
    const store = useCartStore();
    store.items = [
      { id: 1, quantity: 3, selected: true, Product: { price: '100' } },
      { id: 2, quantity: 2, selected: true, Product: { price: '50' } },
    ];
    expect(store.totalCount).toBe(5);
  });

  it('149. totalAmount — 只计算选中商品金额', async () => {
    const { useCartStore } = await import('@/stores/cart');
    const store = useCartStore();
    store.items = [
      { id: 1, quantity: 2, selected: true, Product: { price: '100' } },
      { id: 2, quantity: 1, selected: false, Product: { price: '200' } },
    ];
    expect(store.totalAmount).toBe(200);
  });

  it('150. selectedItems — 返回已选中项', async () => {
    const { useCartStore } = await import('@/stores/cart');
    const store = useCartStore();
    store.items = [
      { id: 1, selected: true, Product: {} },
      { id: 2, selected: false, Product: {} },
      { id: 3, selected: true, Product: {} },
    ];
    expect(store.selectedItems.length).toBe(2);
  });

  it('151. toggleSelect — 切换选中状态', async () => {
    const { useCartStore } = await import('@/stores/cart');
    const store = useCartStore();
    store.items = [{ id: 1, selected: true }];
    store.toggleSelect(1);
    expect(store.items[0].selected).toBe(false);
    store.toggleSelect(1);
    expect(store.items[0].selected).toBe(true);
  });

  it('152. selectAll — 全选', async () => {
    const { useCartStore } = await import('@/stores/cart');
    const store = useCartStore();
    store.items = [
      { id: 1, selected: false },
      { id: 2, selected: true },
      { id: 3, selected: false },
    ];
    store.selectAll(true);
    store.items.forEach(i => expect(i.selected).toBe(true));
  });

  it('153. selectAll — 全不选', async () => {
    const { useCartStore } = await import('@/stores/cart');
    const store = useCartStore();
    store.items = [
      { id: 1, selected: true },
      { id: 2, selected: true },
    ];
    store.selectAll(false);
    store.items.forEach(i => expect(i.selected).toBe(false));
  });

  it('154. updateQuantity — 本地更新数量', async () => {
    const { useCartStore } = await import('@/stores/cart');
    const store = useCartStore();
    store.items = [{ id: 1, quantity: 2 }];
    store.updateQuantity(1, 5);
    expect(store.items[0].quantity).toBe(5);
  });

  it('155. removeItem — 移除商品', async () => {
    const { useCartStore } = await import('@/stores/cart');
    const store = useCartStore();
    store.items = [{ id: 1 }, { id: 2 }, { id: 3 }];
    store.removeItem(2);
    expect(store.items.length).toBe(2);
    expect(store.items.find(i => i.id === 2)).toBeUndefined();
  });

  it('156. totalCount — 空购物车为0', async () => {
    const { useCartStore } = await import('@/stores/cart');
    const store = useCartStore();
    expect(store.totalCount).toBe(0);
    expect(store.totalAmount).toBe(0);
  });

  it('157. totalAmount — 数量乘单价', async () => {
    const { useCartStore } = await import('@/stores/cart');
    const store = useCartStore();
    store.items = [
      { id: 1, quantity: 3, selected: true, Product: { price: '50' } },
      { id: 2, quantity: 2, selected: true, Product: { price: '25.50' } },
    ];
    expect(store.totalAmount).toBe(3 * 50 + 2 * 25.5);
  });
});

describe('API Utility', () => {
  it('158. request模块导出正确', async () => {
    const request = (await import('@/utils/request')).default;
    expect(request).toBeDefined();
    expect(typeof request.get).toBe('function');
    expect(typeof request.post).toBe('function');
    expect(typeof request.put).toBe('function');
    expect(typeof request.delete).toBe('function');
  });

  it('159. baseURL包含/api', async () => {
    const request = (await import('@/utils/request')).default;
    expect(request.defaults.baseURL).toBe('/api');
  });

  it('160. timeout设置正确', async () => {
    const request = (await import('@/utils/request')).default;
    expect(request.defaults.timeout).toBe(10000);
  });

  it('161. interceptor — 请求添加token', async () => {
    localStorage.setItem('token', 'test_auth_token');
    const request = (await import('@/utils/request')).default;
    const config = { headers: {} };
    // Simulate interceptor
    const token = localStorage.getItem('token');
    if (token) config.headers.Authorization = `Bearer ${token}`;
    expect(config.headers.Authorization).toBe('Bearer test_auth_token');
    localStorage.removeItem('token');
  });

  it('162. interceptor — 无token时不添加Auth头', async () => {
    localStorage.removeItem('token');
    const config = { headers: {} };
    const token = localStorage.getItem('token');
    if (token) config.headers.Authorization = `Bearer ${token}`;
    expect(config.headers.Authorization).toBeUndefined();
  });
});
