<template>
  <div class="home-page" v-loading="loading">
    <div class="home-container">
      <!-- 左栏 - 秒杀 & 推荐 -->
      <aside class="home-sidebar-left">
        <!-- 限时秒杀卡片 -->
        <div class="sidebar-card flash-card">
          <div class="flash-card-header">
            <span class="flash-card-icon">⚡</span>
            <span class="flash-card-title">限时秒杀</span>
          </div>
          <div class="flash-card-countdown" v-if="flashSaleEndTime">
            <span class="cd-block">{{ flashHours }}</span>
            <span class="cd-sep">:</span>
            <span class="cd-block">{{ flashMinutes }}</span>
            <span class="cd-sep">:</span>
            <span class="cd-block">{{ flashSeconds }}</span>
          </div>
          <p class="flash-card-desc" v-if="flashSaleEndTime">超值好物 手慢无</p>
          <p class="flash-card-desc" v-else>暂无秒杀活动</p>
          <a href="javascript:void(0)" class="flash-card-btn" @click="scrollToFlashSales" v-if="flashSaleProducts.length">
            立即抢购 <el-icon><ArrowRight /></el-icon>
          </a>
          <router-link to="/products" class="flash-card-btn" v-else>去逛逛 <el-icon><ArrowRight /></el-icon></router-link>
        </div>

        <!-- 猜你喜欢卡片 -->
        <div class="sidebar-card recommend-card">
          <h3 class="sidebar-card-title">
            <el-icon><StarFilled /></el-icon> 猜你喜欢
            <el-button class="refresh-btn-mini" @click="refreshRecommend" :loading="recommendLoading" text size="small">
              <el-icon><Refresh /></el-icon>
            </el-button>
          </h3>
          <div class="recommend-mini-list" v-if="recommendProducts.length">
            <div
              class="recommend-mini-item"
              v-for="p in recommendProducts"
              :key="p.id"
              @click="$router.push(`/products/${p.id}`)"
            >
              <el-image
                :src="p.coverImage || p.image"
                fit="cover"
                class="recommend-thumb"
                lazy
              >
                <template #error><div class="rank-thumb-fallback"></div></template>
              </el-image>
              <div class="recommend-mini-info">
                <p class="recommend-mini-name">{{ p.name }}</p>
                <p class="recommend-mini-price">¥{{ Number(p.price).toFixed(2) }}</p>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无推荐" :image-size="40" />
        </div>
      </aside>

      <!-- 主体内容 -->
      <main class="home-main">
        <section class="hero-section" v-if="banners.length">
          <el-carousel :interval="5000" arrow="always" height="320px" indicator-position="outside">
            <el-carousel-item v-for="item in banners" :key="item.id">
              <div
                class="banner-slide"
                :style="{ backgroundImage: `url(${item.imageUrl})` }"
                @click="$router.push(item.linkUrl || '/products')"
              >
                <div class="banner-content">
                  <h2>{{ item.title }}</h2>
                </div>
              </div>
            </el-carousel-item>
          </el-carousel>
        </section>

        <section class="category-section">
          <h2 class="section-title">商品分类</h2>
          <el-row :gutter="16" v-if="categories.length">
            <el-col :xs="8" :sm="6" :md="4" :lg="3" v-for="cat in visibleCategories" :key="cat.id">
              <div class="category-card" @click="$router.push(`/products?category_id=${cat.id}`)">
                <div class="category-icon">
                  <el-icon size="28" v-if="cat.icon">
                    <component :is="cat.icon" />
                  </el-icon>
                  <span v-else class="category-emoji">{{ fallbackEmojis[cat.id % fallbackEmojis.length] }}</span>
                </div>
                <span class="category-name">{{ cat.name }}</span>
              </div>
            </el-col>
            <el-col :xs="8" :sm="6" :md="4" :lg="3">
              <div class="category-card category-more-card" @click="$router.push('/products')">
                <div class="category-icon category-more-icon">
                  <el-icon size="24"><ArrowRight /></el-icon>
                </div>
                <span class="category-name category-more-text">更多分类</span>
              </div>
            </el-col>
          </el-row>
          <el-empty v-else description="暂无分类" />
        </section>

        <section class="product-section" v-if="flashSaleProducts.length">
          <div class="section-header">
            <h2 class="section-title flash-section-title">限时秒杀</h2>
            <router-link to="/products" class="section-more">查看更多 <el-icon><ArrowRight /></el-icon></router-link>
          </div>
          <el-row :gutter="16">
            <el-col :xs="12" :sm="8" :md="6" v-for="fs in flashSaleProducts" :key="fs.id">
              <el-card shadow="hover" class="product-card flash-product-card" @click="$router.push(`/products/${fs.productId}`)">
                <div class="product-image">
                  <el-image :src="fs.product?.coverImage || fs.product?.image" fit="cover" lazy class="product-thumb-img">
                    <template #error><div class="image-error-placeholder">暂无图片</div></template>
                  </el-image>
                  <div class="product-tags">
                    <el-tag size="small" type="danger" class="flash-badge-tag">秒杀</el-tag>
                  </div>
                </div>
                <div class="product-info">
                  <p class="product-name">{{ fs.product?.name || '商品#' + fs.productId }}</p>
                  <div class="product-price-row">
                    <span class="product-price flash-price">¥{{ Number(fs.flashPrice).toFixed(2) }}</span>
                    <span class="product-original-price">¥{{ Number(fs.product?.price).toFixed(2) }}</span>
                  </div>
                  <div class="flash-progress">
                    <el-progress :percentage="flashProgress(fs)" :stroke-width="6" :show-text="false" color="#ef4444" />
                    <span class="flash-progress-text">已售 {{ fs.sold || 0 }}/{{ fs.stock }}</span>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </section>

        <section class="product-section">
          <div class="section-header">
            <h2 class="section-title">热门推荐</h2>
            <router-link to="/products?sort=sales" class="section-more">查看更多 <el-icon><ArrowRight /></el-icon></router-link>
          </div>
          <el-row :gutter="16" v-if="hotProducts.length">
            <el-col :xs="12" :sm="8" :md="6" v-for="p in hotProducts" :key="p.id">
              <el-card shadow="hover" class="product-card flash-product-card" @click="$router.push(`/products/${p.id}`)">
                <div class="product-image">
                  <el-image :src="p.coverImage || p.image" fit="cover" lazy class="product-thumb-img">
                    <template #error><div class="image-error-placeholder">暂无图片</div></template>
                  </el-image>
                  <div class="product-tags">
                    <el-tag v-if="p.is_hot" size="small" type="danger" class="flash-badge-tag">热卖</el-tag>
                    <el-tag v-if="p.is_new" size="small" type="danger" class="flash-badge-tag">新品</el-tag>
                  </div>
                </div>
                <div class="product-info">
                  <p class="product-name">{{ p.name }}</p>
                  <div class="product-price-row">
                    <span class="product-price flash-price">¥{{ Number(p.price).toFixed(2) }}</span>
                    <span v-if="p.original_price && p.original_price > p.price" class="product-original-price">¥{{ Number(p.original_price).toFixed(2) }}</span>
                  </div>
                  <span class="flash-progress-text">已售 {{ p.sales || 0 }}</span>
                </div>
              </el-card>
            </el-col>
          </el-row>
          <el-empty v-else description="暂无商品" />
        </section>

        <section class="product-section">
          <div class="section-header">
            <h2 class="section-title">新品上架</h2>
            <router-link to="/products?sort=newest" class="section-more">查看更多 <el-icon><ArrowRight /></el-icon></router-link>
          </div>
          <el-row :gutter="16" v-if="newProducts.length">
            <el-col :xs="12" :sm="8" :md="6" v-for="p in newProducts" :key="p.id">
              <el-card shadow="hover" class="product-card flash-product-card" @click="$router.push(`/products/${p.id}`)">
                <div class="product-image">
                  <el-image :src="p.coverImage || p.image" fit="cover" lazy class="product-thumb-img">
                    <template #error><div class="image-error-placeholder">暂无图片</div></template>
                  </el-image>
                  <div class="product-tags">
                    <el-tag v-if="p.is_hot" size="small" type="danger" class="flash-badge-tag">热卖</el-tag>
                    <el-tag v-if="p.is_new" size="small" type="danger" class="flash-badge-tag">新品</el-tag>
                  </div>
                </div>
                <div class="product-info">
                  <p class="product-name">{{ p.name }}</p>
                  <div class="product-price-row">
                    <span class="product-price flash-price">¥{{ Number(p.price).toFixed(2) }}</span>
                    <span v-if="p.original_price && p.original_price > p.price" class="product-original-price">¥{{ Number(p.original_price).toFixed(2) }}</span>
                  </div>
                  <span class="flash-progress-text">已售 {{ p.sales || 0 }}</span>
                </div>
              </el-card>
            </el-col>
          </el-row>
          <el-empty v-else description="暂无商品" />
        </section>

        <section class="product-section" v-if="promoProducts.length">
          <div class="section-header">
            <h2 class="section-title">限时促销</h2>
            <router-link to="/products" class="section-more">查看更多 <el-icon><ArrowRight /></el-icon></router-link>
          </div>
          <el-row :gutter="16">
            <el-col :xs="12" :sm="8" :md="6" v-for="p in promoProducts" :key="p.id">
              <el-card shadow="hover" class="product-card flash-product-card" @click="$router.push(`/products/${p.id}`)">
                <div class="product-image">
                  <el-image :src="p.coverImage || p.image" fit="cover" lazy class="product-thumb-img">
                    <template #error><div class="image-error-placeholder">暂无图片</div></template>
                  </el-image>
                  <div class="product-tags">
                    <el-tag v-if="p.is_hot" size="small" type="danger" class="flash-badge-tag">热卖</el-tag>
                    <el-tag v-if="p.is_new" size="small" type="danger" class="flash-badge-tag">新品</el-tag>
                  </div>
                </div>
                <div class="product-info">
                  <p class="product-name">{{ p.name }}</p>
                  <div class="product-price-row">
                    <span class="product-price flash-price">¥{{ Number(p.price).toFixed(2) }}</span>
                    <span v-if="p.original_price && p.original_price > p.price" class="product-original-price">¥{{ Number(p.original_price).toFixed(2) }}</span>
                  </div>
                  <span class="flash-progress-text">已售 {{ p.sales || 0 }}</span>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </section>

        <!-- 服务保障栏 -->
        <div class="service-bar">
          <div class="service-item" v-for="s in services" :key="s.label">
            <span class="service-icon">{{ s.icon }}</span>
            <div class="service-text">
              <strong>{{ s.label }}</strong>
              <span>{{ s.desc }}</span>
            </div>
          </div>
        </div>
      </main>

      <!-- 右栏 - 信息面板 -->
      <aside class="home-sidebar-right">
        <!-- 用户面板 -->
        <div class="sidebar-card">
          <div class="user-panel" v-if="userStore.isLoggedIn">
            <div class="user-avatar">{{ userStore.user?.username?.charAt(0)?.toUpperCase() }}</div>
            <div class="user-info">
              <p class="user-name">{{ userStore.user?.username }}</p>
              <p class="user-sub">欢迎回来</p>
            </div>
          </div>
          <div class="user-panel guest" v-else>
            <div class="user-avatar guest-avatar"><el-icon size="24"><UserFilled /></el-icon></div>
            <div class="user-info">
              <p class="user-name">你好，欢迎光临</p>
              <div class="user-links">
                <router-link to="/login">登录</router-link>
                <span>|</span>
                <router-link to="/register">注册</router-link>
              </div>
            </div>
          </div>
          <div class="user-quick-links" v-if="userStore.isLoggedIn">
            <router-link to="/orders" class="quick-link">
              <el-icon><Tickets /></el-icon><span>我的订单</span>
            </router-link>
            <router-link to="/favorites" class="quick-link">
              <el-icon><StarFilled /></el-icon><span>我的收藏</span>
            </router-link>
            <router-link to="/cart" class="quick-link">
              <el-icon><ShoppingCart /></el-icon><span>购物车</span>
            </router-link>
          </div>
        </div>

        <!-- 公告栏 -->
        <div class="sidebar-card" v-if="announcements.length">
          <h3 class="sidebar-card-title">
            <el-icon><Bell /></el-icon> 平台公告
          </h3>
          <div class="announcement-list">
            <div
              class="announcement-item"
              v-for="a in announcements"
              :key="a.id"
              @click="showAnnouncement(a)"
            >
              <span class="anno-dot"></span>
              <span class="anno-text">{{ a.title }}</span>
            </div>
          </div>
        </div>

        <!-- 热销排行 -->
        <div class="sidebar-card" v-if="hotProducts.length">
          <h3 class="sidebar-card-title">
            <el-icon><TrendCharts /></el-icon> 热销排行
          </h3>
          <div class="ranking-list">
            <div
              class="ranking-item"
              v-for="(p, i) in hotProducts.slice(0, 5)"
              :key="p.id"
              @click="$router.push(`/products/${p.id}`)"
            >
              <span class="rank-badge" :class="'rank-' + (i + 1)">{{ i + 1 }}</span>
              <el-image
                :src="p.coverImage || p.image"
                fit="cover"
                class="rank-thumb"
                lazy
              >
                <template #error><div class="rank-thumb-fallback"></div></template>
              </el-image>
              <div class="rank-info">
                <p class="rank-name">{{ p.name }}</p>
                <p class="rank-price">¥{{ Number(p.price).toFixed(2) }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 浏览历史 -->
        <div class="sidebar-card" v-if="browseHistory.length">
          <h3 class="sidebar-card-title">
            <el-icon><Clock /></el-icon> 浏览历史
          </h3>
          <div class="history-list">
            <div
              class="history-item"
              v-for="p in browseHistory"
              :key="p.id"
              @click="$router.push(`/products/${p.id}`)"
            >
              <el-image
                :src="p.coverImage || p.image"
                fit="cover"
                class="history-thumb"
                lazy
              >
                <template #error><div class="rank-thumb-fallback"></div></template>
              </el-image>
              <div class="history-info">
                <p class="history-name">{{ p.name }}</p>
                <p class="history-price">¥{{ Number(p.price).toFixed(2) }}</p>
              </div>
            </div>
          </div>
        </div>
      </aside>
    </div>

    <!-- 回到顶部 -->
    <transition name="fade-up">
      <div class="back-to-top" v-show="showBackTop" @click="scrollToTop">
        <el-icon size="22"><ArrowUp /></el-icon>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { homeAPI, productAPI, adminAPI, flashsaleAPI } from '@/api'
import {
  ArrowRight, ArrowUp, Iphone, Monitor, ShoppingBag, Food, HomeFilled,
  MagicStick, TrophyBase, Reading, Headset, Notebook, Key, Sunny, Timer,
  UserFilled, Tickets, StarFilled, ShoppingCart, Bell, TrendCharts, Clock, Refresh
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)
const banners = ref([])
const categories = ref([])
const allCategories = ref([])
const hotProducts = ref([])
const newProducts = ref([])
const promoProducts = ref([])
const recommendProducts = ref([])
const recommendLoading = ref(false)
const announcements = ref([])
const browseHistory = ref([])
const showBackTop = ref(false)

// Responsive category grid: show N-1 categories + "更多分类" card to fill exactly one row
const windowWidth = ref(window.innerWidth)
const colsPerRow = computed(() => {
  if (windowWidth.value >= 1200) return 8   // lg: 3 → 24/3 = 8
  if (windowWidth.value >= 992) return 6    // md: 4 → 24/4 = 6
  if (windowWidth.value >= 768) return 4    // sm: 6 → 24/6 = 4
  return 3                                   // xs: 8 → 24/8 = 3
})
const visibleCategories = computed(() => {
  return categories.value.slice(0, Math.max(0, colsPerRow.value - 1))
})

function onResize() {
  windowWidth.value = window.innerWidth
}

// Flash sale countdown
const flashSaleProducts = ref([])
const flashSaleEndTime = ref(null) // timestamp of earliest ending flash sale
const flashHours = ref('00')
const flashMinutes = ref('00')
const flashSeconds = ref('00')
let flashTimer = null

function updateFlashCountdown() {
  if (!flashSaleEndTime.value) {
    flashHours.value = '--'
    flashMinutes.value = '--'
    flashSeconds.value = '--'
    return
  }
  const now = Date.now()
  const diff = Math.max(0, Math.floor((flashSaleEndTime.value - now) / 1000))
  flashHours.value = String(Math.floor(diff / 3600)).padStart(2, '0')
  flashMinutes.value = String(Math.floor((diff % 3600) / 60)).padStart(2, '0')
  flashSeconds.value = String(diff % 60).padStart(2, '0')
}

function flashProgress(fs) {
  if (!fs.stock) return 0
  return Math.min(100, Math.round(((fs.sold || 0) / fs.stock) * 100))
}

function scrollToFlashSales() {
  const el = document.querySelector('.flash-section-title')
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

// Service highlights
const services = [
  { icon: '✓', label: '品质保证', desc: '正品保障 假一赔十' },
  { icon: '🚚', label: '极速配送', desc: '全国包邮 次日送达' },
  { icon: '↩', label: '无忧退换', desc: '7天无理由退换货' },
  { icon: '💬', label: '专属客服', desc: '7×24小时在线服务' },
]

function onScroll() {
  showBackTop.value = window.scrollY > 600
}

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const iconMap = [
  { keys: ['手机', '数码', '智能'], icon: Iphone },
  { keys: ['电脑', '办公', '笔记本', '键盘', '鼠标'], icon: Monitor },
  { keys: ['服饰', '鞋包', '女装', '男装', '鞋'], icon: ShoppingBag },
  { keys: ['食品', '生鲜', '零食', '水果'], icon: Food },
  { keys: ['家居', '家装', '家具', '灯具', '灯'], icon: HomeFilled },
  { keys: ['美妆', '护肤', '面部', '彩妆'], icon: MagicStick },
  { keys: ['运动', '户外', '跑步', '健身'], icon: TrophyBase },
  { keys: ['图书', '文具', '书籍'], icon: Reading },
  { keys: ['耳机', '音箱', '耳'], icon: Headset },
  { keys: ['笔记本'], icon: Notebook },
  { keys: ['键盘', '鼠标'], icon: Key },
  { keys: ['灯'], icon: Sunny },
  { keys: ['跑步', '运动鞋'], icon: Timer },
  { keys: ['零食', '食品', '水果', '生鲜', '美味'], icon: Food },
]

const fallbackEmojis = ['📱', '💻', '👗', '👟', '🏠', '💄', '🎮', '🍜', '📚', '🎧', '⌚', '🧸']

function getCategoryIcon(name) {
  for (const { keys, icon } of iconMap) {
    if (keys.some(k => name.includes(k))) return icon
  }
  return null
}

function showAnnouncement(a) {
  // Simple alert for now — could be extended to a modal
  import('element-plus').then(({ ElMessageBox }) => {
    ElMessageBox.alert(a.content || a.title, a.title, { confirmButtonText: '知道了' })
  })
}

onMounted(async () => {
  // Start flash countdown
  updateFlashCountdown()
  flashTimer = setInterval(updateFlashCountdown, 1000)
  // Scroll listener for back-to-top
  window.addEventListener('scroll', onScroll, { passive: true })
  window.addEventListener('resize', onResize, { passive: true })

  loading.value = true
  try {
    const [bannerRes, catRes, hotRes, newRes, promoRes, annoRes, flashRes] = await Promise.all([
      homeAPI.banners(),
      adminAPI.categories(),
      productAPI.list({ sort: 'sales', page_size: 8, page: 1 }),
      productAPI.list({ sort: 'newest', page_size: 8, page: 1 }),
      productAPI.list({ sort: 'price_asc', page_size: 8, page: 1 }),
      homeAPI.announcements(),
      flashsaleAPI.list()
    ])

    if (bannerRes.code === 200) banners.value = bannerRes.data || []
    if (catRes.code === 200) {
      allCategories.value = catRes.data || []
      categories.value = allCategories.value.filter(c => !c.parentId).map(c => ({ ...c, icon: getCategoryIcon(c.name) }))
    }
    if (hotRes.code === 200) hotProducts.value = hotRes.data?.list || hotRes.data || []
    if (newRes.code === 200) newProducts.value = newRes.data?.list || newRes.data || []
    if (promoRes.code === 200) promoProducts.value = promoRes.data?.list || promoRes.data || []
    if (annoRes.code === 200) announcements.value = (annoRes.data || []).slice(0, 5)
    if (flashRes.code === 200) {
      const list = flashRes.data || []
      flashSaleProducts.value = list.filter(fs => fs.status === 'active')
      if (flashSaleProducts.value.length) {
        const endTimes = flashSaleProducts.value.map(fs => new Date(fs.endTime).getTime())
        flashSaleEndTime.value = Math.min(...endTimes)
      }
    }
  } catch {
    // 加载失败显示空状态
  } finally {
    loading.value = false
  }

  fetchRecommend()
  loadBrowseHistory()
})

async function fetchRecommend() {
  recommendLoading.value = true
  try {
    // 从所有商品中随机获取，而不是只取热销前20
    const res = await productAPI.list({ sort: 'newest', page_size: 100, page: 1 })
    if (res.code === 200) {
      const list = res.data?.list || res.data || []
      // Fisher-Yates 洗牌后取6个（一行）
      const shuffled = [...list]
      for (let i = shuffled.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]]
      }
      recommendProducts.value = shuffled.slice(0, 8)
    }
  } catch { /* ignore */ }
  finally { recommendLoading.value = false }
}

function refreshRecommend() {
  // 重新随机，不是原地打乱
  fetchRecommend()
}

async function loadBrowseHistory() {
  try {
    const key = 'browse_history'
    const ids = JSON.parse(localStorage.getItem(key) || '[]')
    if (!ids.length) return
    const results = await Promise.allSettled(
      ids.slice(0, 5).map(id => productAPI.detail(id))
    )
    browseHistory.value = results
      .filter(r => r.status === 'fulfilled' && r.value.code === 200)
      .map(r => r.value.data)
  } catch { /* ignore */ }
}

onBeforeUnmount(() => {
  if (flashTimer) clearInterval(flashTimer)
  window.removeEventListener('scroll', onScroll)
  window.removeEventListener('resize', onResize)
})
</script>

<style scoped>
.home-page {
  background: #ffffff;
  min-height: 100vh;
}

/* 三栏容器 */
.home-container {
  max-width: 2000px;
  margin: 0 auto;
  padding: 6px 6px 0;
  display: flex;
  gap: 8px;
  align-items: flex-start;
}

/* ===== 左栏 ===== */
.home-sidebar-left {
  width: 240px;
  flex-shrink: 0;
  position: sticky;
  top: 110px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  z-index: 10;
}

/* ===== 中栏 ===== */
.home-main {
  flex: 1;
  min-width: 0;
  max-width: 100%;
}

/* ===== 右栏 ===== */
.home-sidebar-right {
  width: 260px;
  flex-shrink: 0;
  position: sticky;
  top: 110px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  z-index: 10;
}

/* ===== 侧栏卡片 ===== */
.sidebar-card {
  background: rgba(255,255,255,0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: 12px;
  padding: 10px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04), 0 4px 16px rgba(0,0,0,0.04);
  border: 1px solid rgba(226,232,240,0.8);
  transition: box-shadow 0.3s ease;
}

.sidebar-card:hover {
  box-shadow: 0 1px 3px rgba(0,0,0,0.06), 0 8px 24px rgba(0,0,0,0.06);
}

.sidebar-card-title {
  font-size: 13px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0 0 6px;
  display: flex;
  align-items: center;
  gap: 6px;
  letter-spacing: 0.02em;
}

.sidebar-card-title .el-icon {
  color: var(--color-primary);
}

/* ===== 用户面板 ===== */
.user-panel {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-panel.guest { margin-bottom: 0; }

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #dc2626 0%, #ef4444 50%, #dc2626 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.3);
}

.guest-avatar {
  background: linear-gradient(135deg, #94a3b8, #64748b);
  box-shadow: 0 4px 12px rgba(100,116,139,0.25);
}

.user-info { flex: 1; min-width: 0; }

.user-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 3px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-sub {
  font-size: 12px;
  color: var(--color-text-muted);
  margin: 0;
}

.user-links {
  font-size: 13px;
  margin-top: 4px;
}

.user-links a {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}

.user-links a:hover { color: #b91c1c; }
.user-links span { color: var(--color-border); margin: 0 8px; }

.user-quick-links {
  display: flex;
  gap: 4px;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid rgba(226,232,240,0.6);
}

.quick-link {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: var(--color-text-muted);
  text-decoration: none;
  padding: 8px 4px;
  border-radius: 10px;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  background: rgba(248,250,252,0.5);
}

.quick-link:hover {
  color: var(--color-primary);
  background: #ffffff;
  transform: translateY(-2px);
}
.quick-link .el-icon { font-size: 20px; transition: transform 0.25s; }
.quick-link:hover .el-icon { transform: scale(1.15); }

/* ===== 公告栏 ===== */
.announcement-list { display: flex; flex-direction: column; }

.announcement-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 4px;
  cursor: pointer;
  border-bottom: 1px dashed rgba(226,232,240,0.5);
  transition: all 0.2s;
  border-radius: 6px;
}

.announcement-item:last-child { border-bottom: none; }

.announcement-item:hover {
  background: rgba(248,250,252,0.8);
  padding-left: 8px;
}

.anno-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ef4444, #dc2626);
  flex-shrink: 0;
  box-shadow: 0 0 0 3px rgba(239,68,68,0.12);
  animation: dotPulse 2s ease-in-out infinite;
}

@keyframes dotPulse {
  0%, 100% { box-shadow: 0 0 0 3px rgba(239,68,68,0.12); }
  50% { box-shadow: 0 0 0 6px rgba(239,68,68,0.06); }
}

.anno-text {
  font-size: 13px;
  color: var(--color-text-body);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s;
}

.announcement-item:hover .anno-text { color: var(--color-primary); }

/* ===== 排行榜 ===== */
.ranking-list { display: flex; flex-direction: column; gap: 4px; }

.ranking-item {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 4px;
  border-radius: 8px;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.ranking-item:hover {
  background: #ffffff;
  padding-left: 10px;
}

.rank-badge {
  width: 22px;
  height: 22px;
  border-radius: 7px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
  color: #fff;
  background: #94a3b8;
  flex-shrink: 0;
  box-shadow: 0 2px 6px rgba(148,163,184,0.3);
}

.rank-badge.rank-1 { background: linear-gradient(135deg, #ef4444, #dc2626); box-shadow: 0 2px 8px rgba(239,68,68,0.35); }
.rank-badge.rank-2 { background: linear-gradient(135deg, #ef4444, #dc2626); box-shadow: 0 2px 8px rgba(239,68,68,0.35); }
.rank-badge.rank-3 { background: linear-gradient(135deg, #10b981, #059669); box-shadow: 0 2px 8px rgba(16,185,129,0.35); }

.rank-thumb {
  width: 42px;
  height: 42px;
  border-radius: 8px;
  flex-shrink: 0;
  overflow: hidden;
}

.rank-thumb-fallback {
  width: 100%;
  height: 100%;
  background: #ffffff;
}

.rank-info { flex: 1; min-width: 0; }

.rank-name {
  font-size: 12px;
  font-weight: 500;
  color: var(--color-text-primary);
  margin: 0 0 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rank-price {
  font-size: 14px;
  font-weight: 700;
  color: #ef4444;
  margin: 0;
}

/* ===== 浏览历史 ===== */
.history-list { display: flex; flex-direction: column; gap: 4px; }

.history-item {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 4px;
  border-radius: 8px;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.history-item:hover {
  background: #ffffff;
  padding-left: 10px;
}

.history-thumb {
  width: 42px;
  height: 42px;
  border-radius: 8px;
  flex-shrink: 0;
  overflow: hidden;
}

.history-info { flex: 1; min-width: 0; }

.history-name {
  font-size: 12px;
  font-weight: 500;
  color: var(--color-text-primary);
  margin: 0 0 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-price {
  font-size: 14px;
  font-weight: 700;
  color: #ef4444;
  margin: 0;
}

/* ===== 横幅 ===== */
.hero-section {
  margin-bottom: 12px;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 4px 24px rgba(0,0,0,0.08);
}

.banner-slide {
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  cursor: pointer;
  position: relative;
  transition: transform 0.4s ease;
}

.banner-slide::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(0,0,0,0.35) 0%, rgba(0,0,0,0.15) 50%, rgba(0,0,0,0.3) 100%);
  transition: background 0.3s;
}

.banner-slide:hover::after {
  background: linear-gradient(135deg, rgba(0,0,0,0.25) 0%, rgba(0,0,0,0.05) 50%, rgba(0,0,0,0.2) 100%);
}

.banner-content {
  position: relative;
  z-index: 1;
  margin-left: 52px;
  color: #fff;
  animation: slideInLeft 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes slideInLeft {
  from { opacity: 0; transform: translateX(-30px); }
  to { opacity: 1; transform: translateX(0); }
}

.banner-content h2 {
  font-size: 1.8rem;
  margin: 0;
  font-weight: 800;
  letter-spacing: 0.04em;
  text-shadow: 0 3px 20px rgba(0,0,0,0.35);
}

/* ===== 分类区 ===== */
.category-section {
  margin-bottom: 16px;
}

.section-title {
  font-size: 1.2rem;
  font-weight: 900;
  color: var(--color-text-primary);
  margin: 0 0 10px;
  position: relative;
  padding-left: 16px;
  letter-spacing: 0.03em;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 1px;
  bottom: 1px;
  width: 4px;
  background: linear-gradient(180deg, #dc2626, #ef4444);
  border-radius: 3px;
}

.category-card {
  text-align: center;
  padding: 12px 8px 10px;
  background: rgba(255,255,255,0.9);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  margin-bottom: 10px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.03), 0 4px 12px rgba(0,0,0,0.03);
  border: 1px solid rgba(226,232,240,0.7);
}

.category-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 8px 32px rgba(220, 38, 38,0.12), 0 2px 8px rgba(0,0,0,0.06);
  border-color: rgba(220, 38, 38,0.25);
  background: #fff;
}

.category-card:active {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(220, 38, 38,0.08);
}

.category-icon {
  width: 44px;
  height: 44px;
  margin: 0 auto 8px;
  background: #ffffff;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
  transition: all 0.3s;
}

.category-card:hover .category-icon {
  transform: scale(1.08);
  box-shadow: 0 4px 12px rgba(220, 38, 38,0.2);
}

.category-emoji { font-size: 24px; }

.category-name {
  font-size: 12px;
  font-weight: 500;
  color: var(--color-text-body);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: color 0.2s;
}

.category-card:hover .category-name { color: var(--color-primary); }

.category-more-card {
  background: rgba(248,250,252,0.8);
  border: 1px dashed rgba(220,38,38,0.25);
}

.category-more-card:hover {
  border-color: rgba(220,38,38,0.5);
  background: #fff;
}

.category-more-icon {
  color: var(--color-text-muted);
  background: rgba(226,232,240,0.6);
}

.category-more-card:hover .category-more-icon {
  color: var(--color-primary);
  background: rgba(220,38,38,0.1);
  transform: translateX(3px);
}

.category-more-text {
  color: var(--color-text-muted);
}

.category-more-card:hover .category-more-text {
  color: var(--color-primary);
}

/* ===== 商品区 ===== */
.product-section { margin-bottom: 16px; }

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.section-more {
  font-size: 12px;
  font-weight: 500;
  color: var(--color-primary);
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  border-radius: 20px;
  transition: all 0.25s;
  background: rgba(220, 38, 38,0.06);
}

.section-more:hover {
  background: rgba(220, 38, 38,0.12);
  gap: 8px;
}

/* ===== 商品卡片 ===== */
.product-card {
  cursor: pointer;
  margin-bottom: 12px;
  border-radius: 14px;
  overflow: hidden;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(239, 68, 68, 0.2);
  background: #ffffff;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04), 0 4px 12px rgba(0,0,0,0.04);
}

.product-card :deep(.el-card__body) {
  padding: 0;
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 40px rgba(239, 68, 68, 0.1), 0 2px 8px rgba(0,0,0,0.05);
  border-color: rgba(239, 68, 68, 0.4);
}

.product-card:active {
  transform: translateY(-2px);
}

.product-card .tag {
  background: linear-gradient(135deg, #ef4444, #dc2626) !important;
  color: #fff !important;
  border: none !important;
  font-weight: 700 !important;
}

.product-image {
  position: relative;
  overflow: hidden;
  aspect-ratio: 1;
  background: #f8fafc;
}

.product-image :deep(.el-image) {
  width: 100%;
  height: 100%;
  display: block;
}

.product-image :deep(.el-image__inner) {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.product-card:hover .product-image :deep(.el-image__inner) {
  transform: scale(1.05);
}

.product-thumb-img {
  width: 100%;
  height: 100%;
}

.image-error-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #ffffff;
  color: var(--color-text-placeholder);
  font-size: 14px;
}

.product-tags {
  position: absolute;
  top: 10px;
  left: 10px;
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
  z-index: 2;
}

.product-info { padding: 10px 10px 10px; }

.product-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary);
  margin: 0 0 6px;
  line-height: 1.35;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 36px;
}

.product-price-row {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-bottom: 2px;
}

.product-price {
  font-size: 20px;
  font-weight: 800;
  color: #ef4444;
  letter-spacing: -0.03em;
  white-space: nowrap;
}

.product-original-price {
  font-size: 12px;
  color: var(--color-text-placeholder);
  text-decoration: line-through;
  white-space: nowrap;
}

.product-sales {
  font-size: 11px;
  color: var(--color-text-placeholder);
  display: block;
}

/* ===== 左侧闪购卡片 ===== */
.flash-card {
  background: linear-gradient(160deg, #b91c1c 0%, #dc2626 30%, #ef4444 70%, #f87171 100%);
  color: #fff;
  text-align: center;
  position: relative;
  overflow: hidden;
  border: none;
  box-shadow: 0 4px 20px rgba(239,68,68,0.2);
}

.flash-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse at 50% 0%, rgba(255,255,255,0.18) 0%, transparent 60%),
              radial-gradient(ellipse at 50% 100%, rgba(255,255,255,0.05) 0%, transparent 50%);
  pointer-events: none;
}

.flash-card-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-bottom: 8px;
}

.flash-card-icon {
  font-size: 22px;
  animation: flashPulse 1.5s ease-in-out infinite;
}

@keyframes flashPulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.2); opacity: 0.7; }
}

.flash-card-title {
  font-size: 1rem;
  font-weight: 800;
  letter-spacing: 0.04em;
}

.flash-card-countdown {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  margin-bottom: 8px;
}

.flash-card-countdown .cd-block {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 46px;
  background: rgba(0, 0, 0, 0.25);
  border-radius: 6px;
  font-size: 20px;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
  backdrop-filter: blur(4px);
}

.flash-card-countdown .cd-sep {
  font-size: 20px;
  font-weight: 700;
  margin: 0 3px;
  opacity: 0.8;
}

.flash-card-desc {
  font-size: 12px;
  opacity: 0.85;
  margin: 0 0 10px;
}

.flash-card-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: #fff;
  color: #ef4444;
  font-weight: 700;
  font-size: 13px;
  padding: 6px 22px;
  border-radius: 20px;
  text-decoration: none;
  transition: all 0.3s;
}

.flash-card-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 16px rgba(0,0,0,0.2);
}

/* ===== 秒杀商品区 ===== */
.flash-section-title {
  color: #ef4444;
}

.flash-price {
  color: #ef4444 !important;
  font-size: 20px !important;
  font-weight: 800 !important;
}

.flash-product-card {
  position: relative;
  border-color: rgba(239, 68, 68, 0.2) !important;
}

.flash-product-card:hover {
  border-color: rgba(239, 68, 68, 0.4) !important;
  box-shadow: 0 12px 40px rgba(239, 68, 68, 0.1), 0 2px 8px rgba(0,0,0,0.05) !important;
}

.flash-badge-tag {
  background: linear-gradient(135deg, #ef4444, #dc2626) !important;
  color: #fff !important;
  border: none !important;
  font-weight: 700 !important;
}

.flash-progress {
  margin-top: 8px;
}

.flash-progress-text {
  font-size: 11px;
  color: var(--color-text-placeholder);
  margin-top: 2px;
  display: block;
}

/* ===== 左侧推荐卡片 ===== */
.recommend-card .sidebar-card-title {
  justify-content: space-between;
}

.refresh-btn-mini {
  font-size: 16px;
  color: var(--color-primary);
  padding: 2px 4px;
  border-radius: 4px;
  transition: all 0.25s;
}

.refresh-btn-mini:hover {
  background: rgba(220, 38, 38,0.1);
}

.recommend-mini-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.recommend-mini-item {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 4px;
  border-radius: 8px;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.recommend-mini-item:hover {
  background: #ffffff;
  padding-left: 10px;
}

.recommend-thumb {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  flex-shrink: 0;
  overflow: hidden;
}

.recommend-mini-info {
  flex: 1;
  min-width: 0;
}

.recommend-mini-name {
  font-size: 12px;
  font-weight: 500;
  color: var(--color-text-primary);
  margin: 0 0 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recommend-mini-price {
  font-size: 14px;
  font-weight: 700;
  color: #ef4444;
  margin: 0;
}

/* ===== 服务保障 ===== */
.service-bar {
  display: flex;
  justify-content: space-around;
  background: rgba(255,255,255,0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: 14px;
  padding: 16px 12px;
  margin-bottom: 18px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04), 0 4px 16px rgba(0,0,0,0.04);
  border: 1px solid rgba(226,232,240,0.7);
  flex-wrap: wrap;
  gap: 10px;
}

.service-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px;
}

.service-icon {
  font-size: 24px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  border-radius: 10px;
  flex-shrink: 0;
}

.service-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  white-space: nowrap;
}

.service-text strong {
  font-size: 13px;
  color: var(--color-text-primary);
}

.service-text span {
  font-size: 11px;
  color: var(--color-text-muted);
}

/* ===== 回到顶部 ===== */
.back-to-top {
  position: fixed;
  right: 28px;
  bottom: 40px;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: var(--color-primary-gradient);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(220, 38, 38,0.35);
  z-index: 200;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.back-to-top:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 24px rgba(220, 38, 38,0.45);
}

.fade-up-enter-active,
.fade-up-leave-active {
  transition: all 0.3s ease;
}

.fade-up-enter-from,
.fade-up-leave-to {
  opacity: 0;
  transform: translateY(12px);
}

/* ===== 响应式 ===== */
@media (max-width: 1500px) {
  .home-sidebar-left { width: 210px; }
  .home-sidebar-right { width: 230px; }
  .home-container { gap: 6px; padding: 6px 6px 0; }
}

@media (max-width: 1200px) {
  .home-sidebar-left { display: none; }
  .home-sidebar-right { display: none; }
  .home-container { padding: 8px 8px 0; }
}

@media (max-width: 768px) {
  .home-container { padding: 6px 4px; gap: 0; }
  .hero-section { border-radius: 12px; }
  .banner-content { margin-left: 28px; }
  .banner-content h2 { font-size: 1.3rem; }
  .section-title { font-size: 1.1rem; }
  .category-card { border-radius: 12px; padding: 14px 6px 12px; }
  .category-icon { width: 40px; height: 40px; border-radius: 12px; }
  .product-card { border-radius: 12px; }
  .product-card:hover { transform: translateY(-4px); }
  .service-bar { flex-direction: column; align-items: center; }
  .back-to-top { right: 16px; bottom: 24px; }
}
</style>
