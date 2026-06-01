<template>
  <div class="client-layout">
    <header class="header">
      <div class="header-inner">
        <router-link to="/" class="logo">🛒 ShopNow</router-link>
        <div class="search-area">
          <div class="search-bar">
            <el-input v-model="keyword" placeholder="搜索商品" size="large" @keyup.enter="search" clearable>
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-button type="primary" size="large" @click="search">搜索</el-button>
          </div>
          <div class="hot-keywords" v-if="hotKeywords.length">
            <span class="hot-label">热门搜索：</span>
            <el-tag
              v-for="kw in hotKeywords"
              :key="kw"
              class="hot-tag"
              size="small"
              @click="quickSearch(kw)"
            >{{ kw }}</el-tag>
          </div>
        </div>
        <div class="header-actions">
          <template v-if="userStore.isLoggedIn">
            <router-link to="/cart" class="action-btn">
              <el-badge :value="cartStore.totalCount" :hidden="!cartStore.totalCount"><el-icon size="20"><ShoppingCart /></el-icon></el-badge>
              <span>购物车</span>
            </router-link>
            <el-dropdown>
              <span class="action-btn">{{ userStore.user?.username }} <el-icon><ArrowDown /></el-icon></span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="$router.push('/orders')">我的订单</el-dropdown-item>
                  <el-dropdown-item @click="$router.push('/favorites')">我的收藏</el-dropdown-item>
                  <el-dropdown-item @click="$router.push('/profile')">个人中心</el-dropdown-item>
                  <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <router-link to="/login" class="action-btn">登录</router-link>
            <router-link to="/register"><el-button type="primary" size="small">注册</el-button></router-link>
          </template>
        </div>
      </div>
      <nav class="nav-bar">
        <div class="nav-inner">
          <el-menu mode="horizontal" :ellipsis="false" router>
            <el-menu-item index="/">首页</el-menu-item>
            <el-menu-item v-for="cat in categories" :key="cat.id" :index="`/products?category_id=${cat.id}`">{{ cat.name }}</el-menu-item>
          </el-menu>
        </div>
      </nav>
    </header>
    <main class="main-content">
      <router-view />
    </main>
    <footer class="footer">
      <div class="footer-inner">
        <p>&copy; 2024 ShopNow 品质生活电商平台 | 客服电话: 400-888-8888</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { useCartStore } from '@/stores/cart';
import { homeAPI } from '@/api';
import { Search, ShoppingCart, ArrowDown } from '@element-plus/icons-vue';

const router = useRouter();
const userStore = useUserStore();
const cartStore = useCartStore();
const keyword = ref('');
const categories = ref([]);
const hotKeywords = ref(['笔记本电脑', '蓝牙耳机', '机械键盘', '连衣裙', '运动鞋', '面膜套装', '智能手表', '零食大礼包']);

onMounted(async () => {
  const res = await homeAPI.banners();
  if (res.code === 200) { /* banners loaded */ }
  // In production, load categories from API
  const catRes = await import('@/api').then(m => m.adminAPI.categories());
  if (catRes.code === 200) categories.value = catRes.data.filter(c => !c.parentId);
  if (userStore.isLoggedIn) cartStore.fetchCart();
});

function search() {
  if (keyword.value.trim()) {
    router.push(`/products?keyword=${encodeURIComponent(keyword.value)}`);
  }
}

function quickSearch(kw) {
  router.push(`/products?keyword=${encodeURIComponent(kw)}`);
}

function logout() {
  userStore.logout();
  router.push('/');
}
</script>

<style scoped>
.header { background: var(--gradient-header); box-shadow: 0 2px 16px rgba(0,0,0,0.06); position: sticky; top: 0; z-index: 100; }
.header-inner { max-width: 2000px; margin: 0 auto; padding: 8px 16px; display: flex; align-items: center; gap: 20px; }
.logo { font-size: 1.35rem; font-weight: 800; background: var(--color-primary-gradient); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; letter-spacing: 0.02em; }
.search-area { flex: 1; max-width: 700px; }
.search-bar { display: flex; gap: 0; }
.search-bar :deep(.el-input__wrapper) { border-radius: 8px 0 0 8px; }
.search-bar .el-button { border-radius: 0 8px 8px 0; }
.hot-keywords { display: flex; align-items: center; flex-wrap: wrap; gap: 6px; margin-top: 6px; }
.hot-label { font-size: 12px; color: #999; white-space: nowrap; }
.hot-tag { cursor: pointer; font-size: 11px; }
.hot-tag:hover { color: var(--color-primary); border-color: var(--color-primary); }
.header-actions { display: flex; align-items: center; gap: 16px; margin-left: auto; }
.action-btn { display: flex; align-items: center; gap: 4px; cursor: pointer; font-size: 0.9rem; color: #555; white-space: nowrap; }
.action-btn:hover { color: var(--color-primary); }
.nav-bar { background: var(--gradient-nav); box-shadow: 0 3px 12px rgba(220, 38, 38, 0.18); }
.nav-inner { max-width: 2000px; margin: 0 auto; padding: 0 20px; }
.nav-inner :deep(.el-menu) { background: transparent; border: none; }
.nav-inner :deep(.el-menu-item) { color: rgba(255,255,255,0.9) !important; border: none !important; font-size: 0.95rem; }
.nav-inner :deep(.el-menu-item:hover), .nav-inner :deep(.el-menu-item.is-active) { color: #fff !important; background: rgba(255,255,255,0.15) !important; border-radius: 6px; backdrop-filter: blur(4px); }
.main-content { min-height: calc(100vh - 200px); background: var(--gradient-page); background-attachment: fixed; }
.footer { background: var(--gradient-footer); color: #94a3b8; text-align: center; padding: 16px; border-top: 1px solid rgba(255,255,255,0.05); font-size: 0.85rem; }
</style>
