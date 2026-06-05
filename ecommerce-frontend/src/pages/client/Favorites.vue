<template>
  <div class="favorites-page">
    <h1 class="page-title">我的收藏</h1>

    <el-row :gutter="16" v-loading="loading">
      <template v-if="favorites.length">
        <el-col
          v-for="fav in favorites"
          :key="fav.id"
          :xs="12"
          :sm="6"
          :md="4"
          :lg="4"
        >
          <el-card shadow="hover" class="favorite-card">
            <div class="fav-image" @click="goProduct(fav)">
              <el-image
                :src="fav.product?.coverImage || fav.product?.image || fav.image"
                fit="cover"
                lazy
                style="width: 100%; height: 100%"
              >
                <template #error>
                  <div class="image-slot"><el-icon size="36"><Picture /></el-icon></div>
                </template>
              </el-image>
            </div>
            <div class="fav-info" @click="goProduct(fav)">
              <p class="fav-name">{{ fav.product?.name || fav.name }}</p>
              <p class="fav-price">¥{{ Number(fav.product?.price || fav.price || 0).toFixed(2) }}</p>
            </div>
            <div class="fav-actions">
              <el-button
                type="danger"
                text
                size="small"
                :loading="removingId === fav.id"
                @click.stop="handleRemove(fav)"
              >
                <el-icon><Delete /></el-icon> 取消收藏
              </el-button>
              <el-button
                type="primary"
                text
                size="small"
                @click.stop="goProduct(fav)"
              >
                查看详情
              </el-button>
            </div>
          </el-card>
        </el-col>
      </template>
      <el-col :span="24" v-else>
        <el-empty description="暂无收藏的商品">
          <el-button type="primary" @click="$router.push('/products')">去逛逛</el-button>
        </el-empty>
      </el-col>
    </el-row>

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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { favAPI } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Picture } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(true)
const favorites = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const removingId = ref(null)

onMounted(() => {
  fetchFavorites()
})

async function fetchFavorites() {
  loading.value = true
  try {
    const res = await favAPI.list()
    if (res.code === 200) {
      const list = res.data?.list || res.data || []
      favorites.value = list
      total.value = res.data?.total || list.length
    }
  } catch {
    favorites.value = []
  } finally {
    loading.value = false
  }
}

function handlePageChange() {
  fetchFavorites()
}

function goProduct(fav) {
  const id = fav.productId || fav.product?.id
  if (id) router.push(`/products/${id}`)
}

async function handleRemove(fav) {
  try {
    await ElMessageBox.confirm('确定要取消收藏该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    removingId.value = fav.id
    const productId = fav.productId || fav.product?.id
    const res = await favAPI.toggle(productId)
    if (res.code === 200) {
      ElMessage.success('已取消收藏')
      favorites.value = favorites.value.filter(f => f.id !== fav.id)
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {
    // 取消操作
  } finally {
    removingId.value = null
  }
}
</script>

<style scoped>
.favorites-page {
  max-width: 2200px;
  margin: 0 auto;
  padding: 8px 8px;
  min-height: 60vh;
}

.page-title {
  font-size: 1.3rem;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0 0 14px;
}

.favorite-card {
  margin-bottom: 12px;
  border-radius: 14px;
  overflow: hidden;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(239, 68, 68, 0.2);
  background: #ffffff;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04), 0 4px 12px rgba(0,0,0,0.04);
}

.favorite-card :deep(.el-card__body) {
  padding: 12px;
}

.favorite-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 40px rgba(239, 68, 68, 0.1), 0 2px 8px rgba(0,0,0,0.05);
  border-color: rgba(239, 68, 68, 0.4);
}

.fav-image {
  cursor: pointer;
  overflow: hidden;
  border-radius: 8px;
  margin-bottom: 4px;
  aspect-ratio: 4 / 3;
  background: #f8fafc;
}

.fav-image :deep(.el-image) {
  width: 100%;
  height: 100%;
  display: block;
}

.fav-image :deep(.el-image__inner) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-slot {
  width: 100%;
  height: 100%;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-placeholder);
}

.fav-info {
  cursor: pointer;
  padding: 4px 0;
}

.fav-name {
  font-size: 14px;
  color: var(--color-text-primary);
  margin: 0 0 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 40px;
  line-height: 1.4;
}

.fav-price {
  font-size: 18px;
  font-weight: 800;
  color: #ef4444;
  margin: 0;
}

.fav-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 8px;
  border-top: 1px solid var(--color-border-light);
  margin-top: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}
</style>
