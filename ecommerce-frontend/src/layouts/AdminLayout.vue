<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <router-link to="/admin/dashboard" class="sidebar-logo">🛒 ShopNow Admin</router-link>
      <el-menu :default-active="route.path" router background-color="#1e293b" text-color="#94a3b8" active-text-color="#dc2626">
        <el-menu-item index="/admin/dashboard"><el-icon><DataAnalysis /></el-icon> 数据看板</el-menu-item>
        <el-menu-item index="/admin/users"><el-icon><User /></el-icon> 用户管理</el-menu-item>
        <el-menu-item index="/admin/products"><el-icon><Goods /></el-icon> 商品管理</el-menu-item>
        <el-menu-item index="/admin/product-images"><el-icon><Picture /></el-icon> 商品图片上传</el-menu-item>
        <el-menu-item index="/admin/categories"><el-icon><Menu /></el-icon> 分类管理</el-menu-item>
        <el-menu-item index="/admin/orders"><el-icon><Document /></el-icon> 订单管理</el-menu-item>
        <el-menu-item index="/admin/reviews"><el-icon><ChatDotRound /></el-icon> 评价管理</el-menu-item>
        <el-menu-item index="/admin/banners"><el-icon><Picture /></el-icon> 轮播管理</el-menu-item>
        <el-menu-item index="/admin/announcements"><el-icon><Bell /></el-icon> 公告管理</el-menu-item>
        <el-menu-item index="/admin/coupons"><el-icon><Ticket /></el-icon> 优惠券管理</el-menu-item>
        <el-menu-item index="/admin/flashsales"><el-icon><Timer /></el-icon> 秒杀管理</el-menu-item>
        <el-menu-item index="/admin/feedbacks"><el-icon><Comment /></el-icon> 反馈管理</el-menu-item>
        <el-menu-item index="/admin/messages"><el-icon><ChatDotRound /></el-icon> 客服消息</el-menu-item>
      </el-menu>
    </aside>
    <div class="admin-main">
      <header class="admin-header">
        <span class="admin-title">管理后台</span>
        <div class="admin-header-right">
          <span>{{ userStore.user?.username }}</span>
          <router-link to="/admin/profile"><el-button text>个人中心</el-button></router-link>
          <router-link to="/" target="_blank"><el-button text>前台首页</el-button></router-link>
          <el-button text type="danger" @click="logout">退出</el-button>
        </div>
      </header>
      <main class="admin-content"><router-view /></main>
    </div>
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

function logout() {
  userStore.logout();
  router.push('/admin/login');
}
</script>

<style scoped>
.admin-layout { display: flex; min-height: 100vh; }
.sidebar { width: 260px; background: var(--gradient-sidebar); flex-shrink: 0; }
.sidebar-logo { display: block; padding: 22px 20px; font-size: 1.25rem; font-weight: 700; background: var(--color-primary-gradient); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; text-align: center; border-bottom: 1px solid rgba(255,255,255,0.08); }
.sidebar :deep(.el-menu) { border-right: none; }
.sidebar :deep(.el-menu-item) { font-size: 1rem; height: 52px; line-height: 52px; }
.admin-main { flex: 1; display: flex; flex-direction: column; background: var(--gradient-page); background-attachment: fixed; }
.admin-header { background: var(--gradient-header); padding: 14px 24px; display: flex; align-items: center; justify-content: space-between; box-shadow: var(--shadow-sm); }
.admin-title { font-size: 1rem; font-weight: 600; color: var(--color-text-primary); }
.admin-header-right { display: flex; align-items: center; gap: 8px; font-size: 0.9rem; }
.admin-content { flex: 1; padding: 24px; overflow-y: auto; }
</style>
