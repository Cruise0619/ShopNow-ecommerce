<template>
  <div class="products-page">
    <el-row :gutter="20">
      <!-- 左侧分类树 -->
      <el-col :xs="0" :md="5">
        <div class="category-sidebar">
          <h3 class="sidebar-title">商品分类</h3>
          <el-menu
            :default-active="activeCategoryId"
            class="category-menu"
            @select="handleCategorySelect"
          >
            <el-menu-item index="">
              <el-icon><Grid /></el-icon>
              <span>全部分类</span>
            </el-menu-item>
            <el-menu-item
              v-for="cat in categories"
              :key="cat.id"
              :index="String(cat.id)"
            >
              <span>{{ cat.name }}</span>
            </el-menu-item>
          </el-menu>
        </div>
      </el-col>

      <!-- 主内容区 -->
      <el-col :xs="24" :md="19">
        <!-- 筛选栏 -->
        <div class="filter-bar">
          <div class="filter-left">
            <span class="filter-label">排序：</span>
            <el-select
              v-model="currentSort"
              size="default"
              style="width: 200px"
              @change="handleSortChange"
            >
              <el-option label="综合排序" value="default" />
              <el-option label="销量优先" value="sales" />
              <el-option label="好评率优先" value="rating" />
              <el-option label="最新上架" value="newest" />
              <el-option label="价格从低到高" value="price_asc" />
              <el-option label="价格从高到低" value="price_desc" />
            </el-select>
            <span class="filter-gap"></span>
            <span class="filter-label">筛选：</span>
            <el-select
              v-model="quickFilters"
              multiple
              collapse-tags
              collapse-tags-tooltip
              placeholder="选择筛选条件"
              size="default"
              style="width: 280px"
            >
              <el-option label="包邮" value="free_shipping" />
              <el-option label="退货包运费" value="free_return" />
              <el-option label="7天无理由退货" value="seven_day" />
              <el-option label="正品保证" value="authentic" />
              <el-option label="闪电发货" value="fast_ship" />
              <el-option label="品质优选" value="premium" />
            </el-select>
          </div>
          <div class="filter-right">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索商品"
              :prefix-icon="Search"
              size="default"
              clearable
              style="width: 240px"
              @keyup.enter="handleSearch"
              @clear="handleSearch"
            />
          </div>
        </div>

        <!-- 商品列表 -->
        <div v-loading="loading" class="product-grid-wrapper">
          <el-row :gutter="16" v-if="products.length">
            <el-col
              v-for="p in products"
              :key="p.id"
              :xs="12"
              :sm="8"
              :md="6"
              :lg="6"
            >
              <el-card shadow="hover" class="product-card" @click="$router.push(`/products/${p.id}`)">
                <div class="product-image">
                  <el-image
                    :src="p.coverImage || p.image"
                    fit="cover"
                    lazy
                    class="product-thumb-img"
                  >
                    <template #error>
                      <div class="image-slot"><el-icon><Picture /></el-icon></div>
                    </template>
                  </el-image>
                  <div class="product-tags">
                    <el-tag v-if="flashSaleProductIds.has(p.id)" size="small" type="danger" effect="dark">秒杀</el-tag>
                    <el-tag v-if="p.isHot" size="small" type="danger">热卖</el-tag>
                    <el-tag v-if="p.isNew" size="small" type="success">新品</el-tag>
                    <el-tag v-if="hasTag(p, 'free_shipping')" size="small" class="service-tag">包邮</el-tag>
                    <el-tag v-if="hasTag(p, 'seven_day')" size="small" class="service-tag">7天无理由</el-tag>
                  </div>
                </div>
                <div class="product-info">
                  <p class="product-name">{{ p.name }}</p>
                  <div class="product-price-row">
                    <span class="product-price">¥{{ Number(p.price).toFixed(2) }}</span>
                    <span v-if="p.originalPrice && p.originalPrice > p.price" class="product-original-price">
                      ¥{{ Number(p.originalPrice).toFixed(2) }}
                    </span>
                  </div>
                  <span class="product-sales">已售 {{ p.sales || 0 }}</span>
                </div>
              </el-card>
            </el-col>
          </el-row>
          <el-empty v-else description="暂无商品，请尝试其他分类或关键词" />
        </div>

        <!-- 分页 -->
        <div class="pagination-wrapper" v-if="total > pageSize">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next, total"
            background
            @current-change="handlePageChange"
          />
        </div>
      </el-col>
    </el-row>
  </div>

  <ChatWidget />
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productAPI, adminAPI, flashsaleAPI } from '@/api'
import ChatWidget from '@/components/ChatWidget.vue'
import { Search, Grid, Picture } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const categories = ref([])
const products = ref([])
const total = ref(0)
const pageSize = ref(12)

const currentSort = ref('sales')
const currentPage = ref(1)
const searchKeyword = ref('')
const activeCategoryId = ref('')
const quickFilters = ref([])
const flashSaleProductIds = ref(new Set())

function hasTag(p, tag) {
  return p.tags && Array.isArray(p.tags) && p.tags.includes(tag)
}

// 从 query 读取参数
function initFromQuery() {
  activeCategoryId.value = route.query.category_id || ''
  searchKeyword.value = route.query.keyword || ''
  currentSort.value = route.query.sort || 'sales'
  currentPage.value = Number(route.query.page || 1)
}

initFromQuery()

onMounted(async () => {
  await Promise.all([fetchCategories(), fetchFlashSales()])
  await fetchProducts()
})

async function fetchFlashSales() {
  try {
    const res = await flashsaleAPI.list()
    if (res.code === 200) {
      const list = res.data || []
      flashSaleProductIds.value = new Set(list.filter(fs => fs.status === 'active').map(fs => fs.productId))
    }
  } catch {
    // ignore
  }
}

async function fetchCategories() {
  try {
    const res = await adminAPI.categories()
    if (res.code === 200) {
      const all = res.data || []
      categories.value = all.filter(c => !c.parentId)
    }
  } catch {
    // ignore
  }
}

async function fetchProducts() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      page_size: pageSize.value,
      sort: currentSort.value
    }
    if (activeCategoryId.value) params.category_id = activeCategoryId.value
    if (searchKeyword.value.trim()) params.keyword = searchKeyword.value.trim()
    if (quickFilters.value.length) params.tags = quickFilters.value.join(',')

    const res = await productAPI.list(params)
    if (res.code === 200) {
      products.value = res.data?.list || res.data || []
      total.value = res.data?.total || 0
    } else {
      products.value = []
    }
  } catch {
    products.value = []
  } finally {
    loading.value = false
  }
}

function handleCategorySelect(index) {
  activeCategoryId.value = index
  currentPage.value = 1
  updateQuery()
  fetchProducts()
}

function handleSortChange() {
  currentPage.value = 1
  updateQuery()
  fetchProducts()
}

function handleSearch() {
  currentPage.value = 1
  updateQuery()
  fetchProducts()
}

function handlePageChange() {
  updateQuery()
  fetchProducts()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function updateQuery() {
  const query = {}
  if (activeCategoryId.value) query.category_id = activeCategoryId.value
  if (searchKeyword.value.trim()) query.keyword = searchKeyword.value.trim()
  if (currentSort.value !== 'sales') query.sort = currentSort.value
  if (currentPage.value > 1) query.page = currentPage.value
  router.replace({ query })
}

// 筛选条件变化时重新获取
watch(quickFilters, () => {
  currentPage.value = 1
  fetchProducts()
})

// 如果路由query变化（浏览器前进后退），重新获取
watch(
  () => route.query,
  () => {
    initFromQuery()
    fetchProducts()
  }
)
</script>

<style scoped>
.products-page {
  max-width: 1800px;
  margin: 0 auto;
  padding: 14px 12px;
  min-height: calc(100vh - 130px);
}

.category-sidebar {
  background: rgba(255,255,255,0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: 14px;
  padding: 14px 0;
  position: sticky;
  top: 80px;
  max-height: calc(100vh - 130px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04), 0 4px 16px rgba(0,0,0,0.04);
  border: 1px solid rgba(226,232,240,0.7);
}

.sidebar-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--color-text-primary);
  padding: 0 16px 12px;
  border-bottom: 1px solid rgba(226,232,240,0.6);
  margin: 0 0 8px;
  letter-spacing: 0.02em;
}

.category-sidebar:hover {
  box-shadow: 0 1px 3px rgba(0,0,0,0.06), 0 8px 24px rgba(0,0,0,0.06);
}

.category-menu {
  border: none;
  background: transparent;
  flex: 1;
  overflow-y: auto;
  scrollbar-width: none;
}
.category-menu::-webkit-scrollbar { display: none; }

.category-menu :deep(.el-menu-item) {
  height: 44px;
  line-height: 44px;
  font-size: 15px;
  color: var(--color-text-body);
  border-radius: 8px;
  margin: 2px 8px;
  transition: all 0.2s ease;
}

.category-menu :deep(.el-menu-item:hover) {
  background: #ffffff;
  color: var(--color-primary);
}

.category-menu :deep(.el-menu-item.is-active) {
  background: #ffffff;
  color: var(--color-primary);
  font-weight: 600;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255,255,255,0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  padding: 14px 18px;
  border-radius: 14px;
  margin-bottom: 14px;
  flex-wrap: wrap;
  gap: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04), 0 4px 16px rgba(0,0,0,0.04);
  border: 1px solid rgba(226,232,240,0.7);
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 0;
}

.filter-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
  white-space: nowrap;
  margin-right: 8px;
}

.filter-gap {
  display: inline-block;
  width: 32px;
}

.filter-right :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: none;
  border: 1px solid rgba(226,232,240,0.8);
  transition: all 0.25s;
  font-size: 15px;
}

.filter-right :deep(.el-input__wrapper:hover) {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(220, 38, 38,0.1);
}

.filter-left :deep(.el-select .el-input__wrapper) {
  border-radius: 10px;
  font-size: 15px;
}

.product-grid-wrapper {
  min-height: 400px;
}

.product-card {
  cursor: pointer;
  margin-bottom: 14px;
  border-radius: 14px;
  overflow: hidden;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(239, 68, 68, 0.2);
  background: rgba(255,255,255,0.9);
  box-shadow: 0 1px 3px rgba(0,0,0,0.03), 0 4px 12px rgba(0,0,0,0.03);
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

/* 统一标签为红色渐变风格 */
.product-card :deep(.el-tag--danger),
.product-card :deep(.el-tag--success) {
  background: linear-gradient(135deg, #ef4444, #dc2626) !important;
  color: #fff !important;
  border: none !important;
  font-weight: 700 !important;
}

.product-image {
  position: relative;
  overflow: hidden;
  aspect-ratio: 4 / 3;
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

.image-slot {
  width: 100%;
  height: 100%;
  aspect-ratio: 4 / 3;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-placeholder);
  font-size: 40px;
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

.service-tag {
  background: linear-gradient(135deg, #dc2626, #ef4444) !important;
  color: #fff !important;
  border: none !important;
  font-size: 11px !important;
}

.product-info {
  padding: 10px 12px 12px;
}

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
  min-height: 38px;
}

.product-price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 4px;
}

.product-price {
  font-size: 19px;
  font-weight: 800;
  color: #ef4444;
  letter-spacing: -0.02em;
  white-space: nowrap;
}

.product-original-price {
  font-size: 13px;
  color: var(--color-text-placeholder);
  text-decoration: line-through;
  white-space: nowrap;
}

.product-sales {
  font-size: 12px;
  color: var(--color-text-placeholder);
  display: block;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding: 14px 0;
}

@media (max-width: 992px) {
  .category-sidebar { display: none; }
}

@media (max-width: 768px) {
  .products-page { padding: 12px 8px; }
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
    padding: 12px 14px;
    border-radius: 12px;
  }
  .filter-left { overflow-x: auto; padding-bottom: 4px; }
  .product-card { border-radius: 12px; margin-bottom: 12px; }
  .product-card:hover { transform: translateY(-4px); }
}
</style>
