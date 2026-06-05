import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import request from '@/utils/request';

export const useCartStore = defineStore('cart', () => {
  const items = ref([]);

  const selectedItems = computed(() => items.value.filter(i => i.selected));
  const totalCount = computed(() => items.value.reduce((s, i) => s + i.quantity, 0));
  const totalAmount = computed(() => selectedItems.value.reduce((s, i) => s + Number(i.product?.price || 0) * i.quantity, 0));

  async function fetchCart() {
    const res = await request.get('/cart');
    if (res.code === 200) items.value = res.data;
  }

  async function addToCart(data) {
    const res = await request.post('/cart', data);
    if (res.code === 200) await fetchCart();
    return res;
  }

  async function updateQuantity(id, quantity) {
    const item = items.value.find(i => i.id === id);
    const oldQty = item ? item.quantity : quantity;
    if (item) item.quantity = quantity;
    try {
      await request.put(`/cart/${id}`, { quantity });
    } catch {
      if (item) item.quantity = oldQty;
      throw new Error('更新数量失败');
    }
  }

  async function removeItem(id) {
    const removed = items.value.find(i => i.id === id);
    items.value = items.value.filter(i => i.id !== id);
    try {
      await request.delete(`/cart/${id}`);
    } catch {
      if (removed) items.value.push(removed);
      throw new Error('删除失败');
    }
  }

  async function removeBatch(ids) {
    const snapshot = items.value.filter(i => ids.includes(i.id));
    items.value = items.value.filter(i => !ids.includes(i.id));
    try {
      await request.delete('/cart/batch', { data: { ids } });
    } catch {
      items.value.push(...snapshot);
      throw new Error('批量删除失败');
    }
  }

  async function toggleSelect(id) {
    const item = items.value.find(i => i.id === id);
    if (!item) return;
    const prev = item.selected;
    item.selected = !item.selected;
    try {
      await request.put(`/cart/${id}`, { quantity: item.quantity, selected: item.selected });
    } catch {
      item.selected = prev;
      throw new Error('操作失败');
    }
  }

  async function selectAll(selected) {
    const snapshot = items.value.map(i => ({ id: i.id, selected: i.selected }));
    items.value.forEach(i => (i.selected = selected));
    try {
      await request.put('/cart/select-all', { selected });
    } catch {
      items.value.forEach(i => {
        const prev = snapshot.find(s => s.id === i.id);
        if (prev) i.selected = prev.selected;
      });
      throw new Error('全选操作失败');
    }
  }

  return { items, selectedItems, totalCount, totalAmount, fetchCart, addToCart, updateQuantity, removeItem, removeBatch, toggleSelect, selectAll };
}, {
  persist: false
});
