<template>
  <div class="product-images-page">
    <div class="page-toolbar">
      <h2 class="toolbar-title">批量上传商品图片</h2>
      <div class="toolbar-right">
        <span class="total-count">共 {{ products.length }} 件商品</span>
        <el-input
          v-model="searchKeyword"
          placeholder="搜索商品名称或分类"
          clearable
          :prefix-icon="Search"
          class="search-input"
        />
      </div>
    </div>

    <div v-loading="loading" class="product-grid">
      <div
        v-for="product in filteredProducts"
        :key="product.id"
        class="product-card"
        :class="{ 'drag-over': dragOverId === product.id }"
        @dragover.prevent="onDragOver(product)"
        @dragleave="onDragLeave(product)"
        @drop.prevent="onDrop(product, $event)"
      >
        <div class="card-cover">
          <el-image
            :src="imgFullUrl(product.coverImage || product.image)"
            fit="cover"
            class="cover-img"
          >
            <template #error>
              <div class="placeholder-img">
                <el-icon :size="36"><PictureFilled /></el-icon>
                <span>暂无图片</span>
              </div>
            </template>
          </el-image>
          <el-badge
            v-if="product.images && product.images.length"
            :value="product.images.length"
            class="image-badge"
            type="primary"
          />
        </div>

        <div class="card-body">
          <div class="product-name" :title="product.name">{{ product.name }}</div>
          <div class="product-meta">
            <el-tag size="small" type="warning" effect="light">
              {{ product.category?.name || '未分类' }}
            </el-tag>
            <span class="price">¥{{ Number(product.price).toFixed(2) }}</span>
          </div>
        </div>

        <div v-if="uploadStatus[product.id] === 'uploading'" class="card-overlay">
          <el-icon class="is-loading" :size="28"><Loading /></el-icon>
          <span>上传中...</span>
        </div>
        <div v-else-if="uploadStatus[product.id] === 'success'" class="card-overlay success">
          <el-icon :size="28"><CircleCheckFilled /></el-icon>
          <span>上传成功</span>
        </div>
        <div v-else-if="uploadStatus[product.id] === 'error'" class="card-overlay error">
          <el-icon :size="28"><CircleCloseFilled /></el-icon>
          <span>上传失败</span>
        </div>

        <div v-if="dragOverId === product.id" class="drop-hint">
          拖放到此商品
        </div>
      </div>
    </div>

    <el-empty
      v-if="!loading && filteredProducts.length === 0"
      description="暂无商品数据"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { adminAPI } from '@/api'
import { ElMessage, ElNotification } from 'element-plus'
import {
  PictureFilled,
  Loading,
  CircleCheckFilled,
  CircleCloseFilled,
  Search,
} from '@element-plus/icons-vue'

const API_BASE = ''
const loading = ref(false)
const products = ref([])
const searchKeyword = ref('')
const dragOverId = ref(null)
const uploadStatus = reactive({})

const filteredProducts = computed(() => {
  if (!searchKeyword.value.trim()) return products.value
  const kw = searchKeyword.value.toLowerCase()
  return products.value.filter(
    (p) =>
      p.name?.toLowerCase().includes(kw) ||
      p.category?.name?.toLowerCase().includes(kw)
  )
})

function imgFullUrl(path) {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return `${API_BASE}${path.startsWith('/') ? '' : '/'}${path}`
}

function onDragOver(product) {
  dragOverId.value = product.id
}

function onDragLeave(product) {
  setTimeout(() => {
    if (dragOverId.value === product.id) {
      dragOverId.value = null
    }
  }, 100)
}

async function onDrop(product, event) {
  dragOverId.value = null
  const files = event.dataTransfer?.files
  if (!files || files.length === 0) return

  const validFiles = []
  for (const file of files) {
    const ext = file.name.split('.').pop().toLowerCase()
    if (!['jpg', 'jpeg', 'png', 'gif', 'webp'].includes(ext)) {
      ElMessage.warning(`"${file.name}" 不是支持的图片格式，已跳过`)
      continue
    }
    if (file.size > 5 * 1024 * 1024) {
      ElMessage.warning(`"${file.name}" 超过 5MB 大小限制，已跳过`)
      continue
    }
    validFiles.push(file)
  }

  if (validFiles.length === 0) return

  const fd = new FormData()
  validFiles.forEach((f) => fd.append('images', f))

  uploadStatus[product.id] = 'uploading'

  try {
    const res = await adminAPI.productUploadImages(product.id, fd)
    if (res.code === 200) {
      uploadStatus[product.id] = 'success'
      if (res.data?.images) {
        product.images = res.data.images
      }
      ElNotification({
        type: 'success',
        title: '上传成功',
        message: `已为 "${product.name}" 上传 ${validFiles.length} 张图片`,
        duration: 3000,
      })
    } else {
      uploadStatus[product.id] = 'error'
      ElMessage.error(res.message || '上传失败')
    }
  } catch (e) {
    uploadStatus[product.id] = 'error'
    ElMessage.error('上传失败，请检查网络连接')
  }

  setTimeout(() => {
    uploadStatus[product.id] = null
  }, 3000)
}

async function loadProducts() {
  loading.value = true
  try {
    const res = await adminAPI.products({ page: 1, page_size: 9999 })
    if (res.code === 200) {
      products.value = res.data?.list || res.data || []
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.product-images-page {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 12px;
}

.toolbar-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--color-text-primary);
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.total-count {
  font-size: 13px;
  color: var(--color-text-muted);
  white-space: nowrap;
}

.search-input {
  width: 240px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.product-card {
  position: relative;
  background: var(--color-surface);
  border: 2px dashed var(--color-border);
  border-radius: 4px;
  overflow: hidden;
  cursor: default;
  transition: border-color 0.2s, box-shadow 0.2s, transform 0.2s;
}

.product-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.product-card.drag-over {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.15);
  transform: scale(1.02);
}

.card-cover {
  position: relative;
  background: #f8fafc;
  overflow: hidden;
}

.cover-img {
  width: 100%;
  height: 160px;
}

.placeholder-img {
  width: 100%;
  height: 160px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--color-text-placeholder);
  font-size: 13px;
  gap: 8px;
  background: #f1f5f9;
}

.image-badge {
  position: absolute;
  top: 8px;
  right: 8px;
}

.card-body {
  padding: 12px 14px;
}

.product-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 8px;
}

.product-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.price {
  color: var(--color-danger);
  font-weight: 600;
  font-size: 14px;
}

.card-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.94);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-primary);
  z-index: 10;
}

.card-overlay.success {
  color: var(--color-success);
}

.card-overlay.error {
  color: var(--color-danger);
}

.drop-hint {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: var(--color-primary);
  color: #fff;
  text-align: center;
  padding: 8px;
  font-size: 13px;
  font-weight: 500;
}
</style>
