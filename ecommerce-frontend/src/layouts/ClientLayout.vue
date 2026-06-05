<template>
  <div class="client-layout">
    <header class="header">
      <div class="header-inner">
        <router-link to="/" class="logo">
          <span class="logo-icon">🛒</span>
          <span class="logo-text">ShopNow</span>
        </router-link>
        <div class="search-area">
          <div class="search-bar">
            <el-input v-model="keyword" placeholder="搜索你想要的商品" size="large" @keyup.enter="search" clearable>
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <button class="search-btn" @click="search">
              <el-icon size="18"><Search /></el-icon>
              <span>搜索</span>
            </button>
          </div>
        </div>
        <div class="header-actions">
          <template v-if="userStore.isLoggedIn">
            <router-link to="/cart" class="action-btn">
              <el-badge :value="cartStore.totalCount" :hidden="!cartStore.totalCount">
                <div class="action-icon"><el-icon size="22"><ShoppingCart /></el-icon></div>
              </el-badge>
              <span class="action-label">购物车</span>
            </router-link>
            <router-link to="/favorites" class="action-btn">
              <div class="action-icon"><el-icon size="22"><StarFilled /></el-icon></div>
              <span class="action-label">我的收藏</span>
            </router-link>
            <el-dropdown>
              <span class="action-btn user-btn">
                <div class="action-icon" v-if="userStore.user?.avatar">
                  <el-avatar :src="userStore.user.avatar" size="small" />
                </div>
                <div class="action-icon" v-else><el-icon size="22"><UserFilled /></el-icon></div>
                <span class="action-label">{{ userStore.user?.username }}</span>
                <el-icon size="14"><ArrowDown /></el-icon>
              </span>
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
            <router-link to="/login" class="action-btn">
              <div class="action-icon"><el-icon size="22"><UserFilled /></el-icon></div>
              <span class="action-label">登录</span>
            </router-link>
            <router-link to="/register"><el-button type="primary" size="large">注册</el-button></router-link>
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
import { Search, ShoppingCart, ArrowDown, StarFilled, UserFilled } from '@element-plus/icons-vue';

const router = useRouter();
const userStore = useUserStore();
const cartStore = useCartStore();
const keyword = ref('');
const categories = ref([]);

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

function logout() {
  userStore.logout();
  router.push('/');
}
</script>

<style scoped>
.header { background: var(--gradient-header); box-shadow: 0 2px 20px rgba(0,0,0,0.08); position: sticky; top: 0; z-index: 100; }
.header-inner { max-width: 2200px; margin: 0 auto; padding: 10px 16px; display: flex; align-items: center; position: relative; }

/* ===== Logo ===== */
.logo { display: flex; align-items: center; gap: 8px; text-decoration: none; flex-shrink: 0; }
.logo-icon { font-size: 2rem; line-height: 1; }
.logo-text { font-size: 1.5rem; font-weight: 900; background: var(--color-primary-gradient); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; letter-spacing: 0.03em; }

/* ===== Search ===== */
.search-area { position: absolute; left: 50%; transform: translateX(-50%); width: 800px; max-width: 55vw; }
.search-bar { display: flex; align-items: stretch; background: #fff; border-radius: 6px; overflow: hidden; box-shadow: 0 2px 16px rgba(220, 38, 38, 0.12), 0 0 0 2px rgba(220, 38, 38, 0.08); transition: box-shadow 0.3s; }
.search-bar:focus-within { box-shadow: 0 4px 24px rgba(220, 38, 38, 0.2), 0 0 0 2px rgba(220, 38, 38, 0.2); }
.search-bar :deep(.el-input) { flex: 1; }
.search-bar :deep(.el-input__wrapper) { border-radius: 0; border: none; box-shadow: none; background: transparent; padding: 8px 16px; }
.search-bar :deep(.el-input__inner) { font-size: 15px; }
.search-bar :deep(.el-input__wrapper:hover), .search-bar :deep(.el-input__wrapper.is-focus) { box-shadow: none; }
.search-btn { display: flex; align-items: center; justify-content: center; gap: 6px; padding: 0 28px; border: none; background: linear-gradient(135deg, #dc2626, #ef4444); color: #fff; font-size: 15px; font-weight: 700; cursor: pointer; white-space: nowrap; transition: all 0.25s; letter-spacing: 0.04em; }
.search-btn:hover { background: linear-gradient(135deg, #b91c1c, #dc2626); }

/* ===== Header Actions ===== */
.header-actions { display: flex; align-items: center; gap: 8px; margin-left: auto; flex-shrink: 0; }
.action-btn { display: flex; flex-direction: column; align-items: center; gap: 3px; cursor: pointer; color: #555; white-space: nowrap; text-decoration: none; padding: 6px 14px; border-radius: 4px; transition: all 0.25s; position: relative; }
.action-btn:hover { color: var(--color-primary); background: rgba(220, 38, 38, 0.06); }
.action-btn.user-btn { flex-direction: row; gap: 6px; }
.action-icon { width: 40px; height: 40px; border-radius: 4px; background: rgba(220, 38, 38, 0.08); display: flex; align-items: center; justify-content: center; color: var(--color-primary); transition: all 0.25s; }
.action-btn:hover .action-icon { background: rgba(220, 38, 38, 0.15); transform: scale(1.05); }
.action-label { font-size: 13px; font-weight: 600; line-height: 1; }
.action-btn :deep(.el-badge__content) { font-size: 11px; }

/* ===== Nav Bar ===== */
.nav-bar { background: var(--gradient-nav); box-shadow: 0 3px 14px rgba(220, 38, 38, 0.22); }
.nav-inner { max-width: 2200px; margin: 0 auto; padding: 0 16px; }
.nav-inner :deep(.el-menu) { background: transparent; border: none; }
.nav-inner :deep(.el-menu-item) { color: rgba(255,255,255,0.9) !important; border: none !important; font-size: 1rem; font-weight: 500; padding: 0 24px; height: 48px; line-height: 48px; }
.nav-inner :deep(.el-menu-item:hover), .nav-inner :deep(.el-menu-item.is-active) { color: #fff !important; background: rgba(255,255,255,0.18) !important; border-radius: 3px; backdrop-filter: blur(4px); }

/* ===== Main & Footer ===== */
.main-content { min-height: calc(100vh - 170px); background: var(--gradient-page); background-attachment: fixed; }
.footer { background: var(--gradient-footer); color: #94a3b8; text-align: center; padding: 14px; border-top: 1px solid rgba(255,255,255,0.05); font-size: 0.8rem; }
</style>
