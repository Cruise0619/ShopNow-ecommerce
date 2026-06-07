<template>
  <div class="product-detail-page" v-loading="loading">
    <!-- 面包屑 -->
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/products' }">全部商品</el-breadcrumb-item>
      <el-breadcrumb-item v-if="product.category">{{ product.category.name }}</el-breadcrumb-item>
      <el-breadcrumb-item>{{ product.name }}</el-breadcrumb-item>
    </el-breadcrumb>

    <el-row :gutter="32" class="product-main" v-if="product.id">
      <!-- 左侧：商品图片 -->
      <el-col :xs="24" :md="10" :lg="9">
        <div class="image-gallery">
          <el-image
            :src="currentImage"
            :preview-src-list="imageList"
            fit="cover"
            class="main-image"
            preview-teleported
          />
          <div class="thumbnail-list" v-if="imageList.length > 1">
            <div
              v-for="(img, idx) in imageList"
              :key="idx"
              class="thumbnail-item"
              :class="{ active: currentImage === img }"
              @click="currentImage = img"
            >
              <el-image :src="img" fit="cover" style="width: 64px; height: 64px" />
            </div>
          </div>
        </div>
      </el-col>

      <!-- 右侧：商品信息 -->
      <el-col :xs="24" :md="14" :lg="15">
        <div class="product-info">
          <h1 class="product-name">{{ product.name }}</h1>
          <p class="product-desc" v-if="product.description">{{ product.description }}</p>

          <!-- 秒杀价格区 -->
          <div class="price-section flash-sale-section" v-if="flashSale">
            <div class="flash-sale-header">
              <el-tag type="danger" size="small" effect="dark">⚡ 限时秒杀</el-tag>
              <span class="flash-countdown">
                距结束
                <span class="cd-block-sm">{{ flashCountdown.hours }}</span>:
                <span class="cd-block-sm">{{ flashCountdown.minutes }}</span>:
                <span class="cd-block-sm">{{ flashCountdown.seconds }}</span>
              </span>
            </div>
            <div class="flash-price-row">
              <span class="current-price flash-price">¥{{ Number(flashSale.flashPrice).toFixed(2) }}</span>
              <span class="original-price">¥{{ Number(product.price).toFixed(2) }}</span>
              <el-tag type="danger" size="small">
                {{ Math.round((1 - flashSale.flashPrice / product.price) * 100) }}% OFF
              </el-tag>
            </div>
            <div class="flash-stock-info">
              已售 {{ flashSale.sold || 0 }} / {{ flashSale.stock }} 件
            </div>
          </div>

          <div class="price-section" v-else>
            <span class="current-price">¥{{ Number(product.price).toFixed(2) }}</span>
            <span v-if="product.original_price && product.original_price > product.price" class="original-price">
              ¥{{ Number(product.original_price).toFixed(2) }}
            </span>
            <el-tag v-if="product.original_price && product.original_price > product.price" type="danger" size="small" class="discount-tag">
              {{ Math.round((1 - product.price / product.original_price) * 100) }}% OFF
            </el-tag>
          </div>

          <div class="meta-row">
            <span>销量：<strong>{{ product.sales || 0 }}</strong> 件</span>
            <el-rate :model-value="product.avgRating || product.rating || 0" disabled show-score text-color="#ef4444" />
            <span>库存：<strong :style="{ color: product.stock > 0 ? '#10b981' : '#ef4444' }">
              {{ product.stock > 0 ? product.stock + '件' : '缺货' }}
            </strong></span>
          </div>

          <!-- 规格选择 -->
          <div class="specs-section" v-if="specGroups.length">
            <div v-for="spec in specGroups" :key="spec.name" class="spec-row">
              <span class="spec-label">{{ spec.name }}：</span>
              <el-radio-group v-model="selectedSpecs[spec.name]" size="small">
                <el-radio-button v-for="val in spec.values" :key="val" :value="val">
                  {{ val }}
                </el-radio-button>
              </el-radio-group>
            </div>
          </div>

          <!-- 数量 -->
          <div class="quantity-row">
            <span class="spec-label">数量：</span>
            <el-input-number
              v-model="quantity"
              :min="1"
              :max="Math.max(product.stock, 1)"
              size="large"
            />
          </div>

          <!-- 操作按钮 -->
          <div class="action-row">
            <el-button type="primary" size="large" @click="handleAddToCart" :disabled="product.stock <= 0">
              <el-icon><ShoppingCart /></el-icon> 加入购物车
            </el-button>
            <el-button type="danger" size="large" @click="handleBuyNow" :disabled="product.stock <= 0">
              立即购买
            </el-button>
            <el-button
              size="large"
              :type="isFavorited ? 'warning' : 'default'"
              @click="handleToggleFavorite"
              :loading="favLoading"
            >
              <el-icon><StarFilled v-if="isFavorited" /><Star v-else /></el-icon>
              {{ isFavorited ? '已收藏' : '收藏' }}
            </el-button>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 商品详情 / 评价 Tab -->
    <div class="detail-tabs">
      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane label="商品详情" name="detail">
          <div class="detail-content" v-html="product.detail || product.content || '<p style=\'text-align:center;color:#9ca3af;\'>暂无商品详情</p>'"></div>
        </el-tab-pane>
        <el-tab-pane label="商品评价" name="reviews">
          <div v-loading="reviewLoading">
            <!-- 评价提交表单 -->
            <div class="review-form" v-if="userStore.isLoggedIn">
              <div class="review-form-title">发表评价</div>
              <div class="review-form-body">
                <div class="review-form-rating">
                  <span class="form-label">评分</span>
                  <el-rate v-model="reviewForm.rating" />
                </div>
                <el-input
                  v-model="reviewForm.content"
                  type="textarea"
                  :rows="3"
                  placeholder="分享您的使用感受，帮助其他用户做决定..."
                  maxlength="500"
                  show-word-limit
                />
                <div class="review-form-images">
                  <el-upload
                    v-model:file-list="reviewImages"
                    :auto-upload="false"
                    list-type="picture-card"
                    accept="image/jpeg,image/png,image/gif,image/webp"
                    :limit="6"
                    :on-exceed="() => ElMessage.warning('最多上传6张图片')"
                  >
                    <el-icon size="24"><Plus /></el-icon>
                  </el-upload>
                </div>
                <div class="review-form-action">
                  <el-button type="primary" :loading="reviewSubmitting" @click="submitReview" :disabled="!reviewForm.rating || !reviewForm.content.trim()">
                    提交评价
                  </el-button>
                </div>
              </div>
            </div>
            <div class="review-summary" v-if="reviewTotal > 0">
              <span class="review-count">共 {{ reviewTotal }} 条评价</span>
            </div>
            <div class="review-list" v-if="reviews.length">
              <div v-for="r in reviews" :key="r.id" class="review-item">
                <div class="review-header">
                  <el-avatar :size="36" :src="r.user_avatar || r.User?.avatar">
                    {{ (r.user_name || r.User?.username || '匿')[0] }}
                  </el-avatar>
                  <div class="review-user">
                    <span class="review-username">{{ r.user_name || r.User?.username || '匿名用户' }}</span>
                    <el-rate :model-value="r.rating" disabled size="small" />
                  </div>
                  <span class="review-time">{{ r.createdAt }}</span>
                </div>
                <p class="review-content">{{ r.content }}</p>
                <div class="review-images" v-if="(r.images || []).length">
                  <el-image
                    v-for="(img, idx) in (r.images || [])"
                    :key="idx"
                    :src="img"
                    fit="cover"
                    style="width: 80px; height: 80px; margin-right: 8px; border-radius: 3px"
                    :preview-src-list="(r.images || [])"
                    preview-teleported
                  />
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无评价，成为第一个评价的人吧" />
            <div class="review-pagination" v-if="reviewTotal > 10">
              <el-pagination
                v-model:current-page="reviewPage"
                :page-size="10"
                :total="reviewTotal"
                layout="prev, next"
                background
                small
                @current-change="fetchReviews"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>

</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productAPI, reviewAPI, favAPI, flashsaleAPI } from '@/api'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { Plus, ShoppingCart, Star, StarFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const loading = ref(true)
const product = ref({})
const reviews = ref([])
const reviewTotal = ref(0)
const reviewPage = ref(1)
const reviewLoading = ref(false)
const reviewForm = reactive({ rating: 0, content: '' })
const reviewImages = ref([])
const reviewSubmitting = ref(false)
const activeTab = ref('detail')
const quantity = ref(1)
const isFavorited = ref(false)
const favLoading = ref(false)
const flashSale = ref(null)
const flashCountdown = ref({ hours: '00', minutes: '00', seconds: '00' })
let flashTimer = null

// 图片处理
const imageList = computed(() => {
  if (!product.value) return []
  const images = product.value.images || []
  if (images.length === 0 && product.value.coverImage) return [product.value.coverImage]
  if (images.length === 0 && product.value.image) return [product.value.image]
  return images
})
const currentImage = ref('')

// 规格处理
const specGroups = computed(() => {
  if (!product.value.specs) return []
  try {
    const specs = typeof product.value.specs === 'string'
      ? JSON.parse(product.value.specs)
      : product.value.specs
    return Array.isArray(specs) ? specs : []
  } catch {
    return []
  }
})
const selectedSpecs = reactive({})

// 初始化规格选择
function initSpecs() {
  specGroups.value.forEach(spec => {
    if (spec.values && spec.values.length) {
      selectedSpecs[spec.name] = spec.values[0]
    }
  })
}

function recordBrowseHistory(productId) {
  try {
    const key = 'browse_history'
    let list = JSON.parse(localStorage.getItem(key) || '[]')
    list = list.filter(id => id !== productId)
    list.unshift(productId)
    list = list.slice(0, 10)
    localStorage.setItem(key, JSON.stringify(list))
  } catch { /* ignore */ }
}

onMounted(async () => {
  const id = route.params.id
  await fetchProduct(id)
  await fetchReviews()
  await checkFavorite()
  await fetchFlashSale(id)
  recordBrowseHistory(id)
})

async function fetchProduct(id) {
  loading.value = true
  try {
    const res = await productAPI.detail(id)
    if (res.code === 200) {
      product.value = res.data
      currentImage.value = imageList.value[0] || ''
      initSpecs()
    }
  } catch {
    ElMessage.error('加载商品信息失败')
  } finally {
    loading.value = false
  }
}

async function fetchFlashSale(productId) {
  try {
    const res = await flashsaleAPI.list()
    if (res.code === 200) {
      const list = res.data || []
      flashSale.value = list.find(fs => fs.productId === Number(productId) && fs.status === 'active') || null
      if (flashSale.value) {
        updateFlashCountdown()
        flashTimer = setInterval(updateFlashCountdown, 1000)
      }
    }
  } catch {
    // ignore
  }
}

function updateFlashCountdown() {
  if (!flashSale.value) return
  const now = Date.now()
  const end = new Date(flashSale.value.endTime).getTime()
  const diff = Math.max(0, Math.floor((end - now) / 1000))
  flashCountdown.value = {
    hours: String(Math.floor(diff / 3600)).padStart(2, '0'),
    minutes: String(Math.floor((diff % 3600) / 60)).padStart(2, '0'),
    seconds: String(diff % 60).padStart(2, '0')
  }
}

async function fetchReviews() {
  reviewLoading.value = true
  try {
    const res = await reviewAPI.list({
      product_id: route.params.id,
      page: reviewPage.value,
      page_size: 10
    })
    if (res.code === 200) {
      reviews.value = res.data?.list || res.data || []
      reviewTotal.value = res.data?.total || 0
    }
  } catch {
    // ignore
  } finally {
    reviewLoading.value = false
  }
}

async function submitReview() {
  if (!reviewForm.rating || !reviewForm.content.trim()) {
    ElMessage.warning('请填写评分和评价内容')
    return
  }
  reviewSubmitting.value = true
  try {
    const fd = new FormData()
    fd.append('productId', route.params.id)
    fd.append('rating', reviewForm.rating)
    fd.append('content', reviewForm.content)
    reviewImages.value.forEach(file => {
      if (file.raw && file.raw instanceof File) {
        fd.append('images', file.raw)
      }
    })
    const res = await reviewAPI.create(fd)
    if (res.code === 200) {
      ElMessage.success('评价发表成功')
      reviewForm.rating = 0
      reviewForm.content = ''
      reviewImages.value = []
      reviewPage.value = 1
      fetchReviews()
    } else {
      ElMessage.error(res.message || '发表失败')
    }
  } catch {
    ElMessage.error('发表评价失败，请稍后重试')
  } finally {
    reviewSubmitting.value = false
  }
}

async function checkFavorite() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await favAPI.list()
    if (res.code === 200 && res.data) {
      const list = Array.isArray(res.data) ? res.data : res.data.list || []
      isFavorited.value = list.some(f => f.productId === Number(route.params.id) || f.product?.id === Number(route.params.id))
    }
  } catch {
    isFavorited.value = false
  }
}

async function handleToggleFavorite() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  favLoading.value = true
  try {
    const res = await favAPI.toggle(route.params.id)
    if (res.code === 200) {
      isFavorited.value = res.data?.favorited ?? !isFavorited.value
      ElMessage.success(isFavorited.value ? '已添加收藏' : '已取消收藏')
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    favLoading.value = false
  }
}

async function handleAddToCart() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    const specStr = Object.entries(selectedSpecs)
      .map(([k, v]) => `${k}:${v}`)
      .join(',')
    const res = await cartStore.addToCart({
      product_id: product.value.id,
      quantity: quantity.value,
      specs: specStr || ''
    })
    if (res.code === 200) {
      ElMessage.success('已加入购物车')
    } else {
      ElMessage.error(res.message || '添加失败')
    }
  } catch {
    ElMessage.error('添加购物车失败')
  }
}

async function handleBuyNow() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  const specStr = Object.entries(selectedSpecs)
    .map(([k, v]) => `${k}:${v}`)
    .join(',')
  router.push(`/checkout?product_id=${product.value.id}&quantity=${quantity.value}&specs=${encodeURIComponent(specStr)}`)
}

onBeforeUnmount(() => {
  if (flashTimer) clearInterval(flashTimer)
})
</script>

<style scoped>
.product-detail-page {
  max-width: 2200px;
  margin: 0 auto;
  padding: 8px 8px;
  min-height: 80vh;
}

.breadcrumb {
  margin-bottom: 16px;
  padding: 8px 0;
}

.product-main {
  margin-bottom: 24px;
}

.image-gallery {
  position: sticky;
  top: 80px;
}

.main-image {
  width: 100%;
  height: 340px;
  border-radius: 4px;
  overflow: hidden;
  background: #ffffff;
}

.thumbnail-list {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.thumbnail-item {
  border: 2px solid transparent;
  border-radius: 3px;
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.3s;
}

.thumbnail-item.active {
  border-color: var(--color-primary);
}

.product-info {
  padding-left: 8px;
}

.product-name {
  font-size: 1.3rem;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0 0 6px;
  line-height: 1.35;
}

.product-desc {
  color: var(--color-text-placeholder);
  font-size: 13px;
  margin: 0 0 10px;
  line-height: 1.45;
}

.price-section {
  background: #ffffff;
  padding: 12px 16px;
  border-radius: 4px;
  margin-bottom: 14px;
  display: flex;
  align-items: baseline;
  gap: 10px;
  border: 1px solid rgba(220, 38, 38, 0.12);
}

.current-price {
  font-size: 1.7rem;
  font-weight: 800;
  color: #ef4444;
}

.original-price {
  font-size: 14px;
  color: #9ca3af;
  text-decoration: line-through;
}

.discount-tag {
  margin-left: 4px;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 14px;
  color: var(--color-text-muted);
  font-size: 13px;
  flex-wrap: wrap;
}

.specs-section {
  margin-bottom: 14px;
}

.spec-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  flex-wrap: wrap;
  gap: 8px;
}

.spec-label {
  font-size: 14px;
  color: var(--color-text-muted);
  min-width: 48px;
}

.quantity-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.action-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.detail-tabs {
  margin-top: 20px;
}

.detail-content {
  padding: 16px 0;
  line-height: 1.8;
  max-width: 100%;
  overflow: hidden;
}

.detail-content :deep(img) {
  max-width: 100%;
  border-radius: 3px;
}

.review-form {
  background: #f8fafc;
  border: 1px solid var(--color-border-light);
  border-radius: 4px;
  padding: 16px;
  margin-bottom: 16px;
}

.review-form-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 10px;
}

.review-form-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.review-form-rating {
  display: flex;
  align-items: center;
  gap: 10px;
}

.form-label {
  font-size: 14px;
  color: var(--color-text-muted);
}

.review-form-images {
  margin-top: 4px;
}

.review-form-action {
  display: flex;
  justify-content: flex-end;
}

.review-summary {
  margin-bottom: 16px;
}

.review-count {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.review-item {
  padding-bottom: 14px;
  border-bottom: 1px solid var(--color-border-light);
}

.review-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.review-user {
  flex: 1;
}

.review-username {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary);
  display: block;
}

.review-time {
  font-size: 12px;
  color: var(--color-text-placeholder);
}

.review-content {
  font-size: 14px;
  color: var(--color-text-body);
  line-height: 1.6;
  margin: 0 0 10px;
}

.review-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.review-pagination {
  display: flex;
  justify-content: center;
  margin-top: 14px;
}

.flash-sale-section {
  background: linear-gradient(135deg, #fef2f2, #fff5f5) !important;
  border: 1px solid rgba(239, 68, 68, 0.2) !important;
  flex-direction: column;
  align-items: stretch;
  gap: 8px;
}

.flash-sale-header {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.flash-countdown {
  font-size: 13px;
  color: #ef4444;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 2px;
}

.cd-block-sm {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 28px;
  height: 24px;
  background: #ef4444;
  color: #fff;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  padding: 0 4px;
}

.flash-price-row {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.flash-stock-info {
  font-size: 12px;
  color: var(--color-text-muted);
}

@media (max-width: 768px) {
  .main-image {
    height: 260px;
  }
  .current-price {
    font-size: 1.4rem;
  }
  .action-row {
    flex-direction: column;
  }
  .action-row .el-button {
    width: 100%;
  }
}
</style>
