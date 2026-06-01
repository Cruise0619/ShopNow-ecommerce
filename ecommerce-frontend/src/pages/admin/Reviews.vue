<template>
  <div class="reviews-page">
    <el-card shadow="never" class="page-card">
      <div class="page-toolbar">
        <div class="toolbar-title">评价管理</div>
        <el-input
          v-model="filterProductId"
          placeholder="按商品 ID 筛选"
          clearable
          prefix-icon="Search"
          style="width: 240px"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
      </div>

      <el-table
        :data="tableData"
        v-loading="loading"
        style="width: 100%"
        stripe
        :header-cell-style="{ background: '#ffffff', color: '#475569', fontWeight: 600 }"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="用户" width="120" />
        <el-table-column prop="productId" label="商品 ID" width="100" align="center" />
        <el-table-column label="评分" width="170" align="center">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled show-score text-color="#ef4444" />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="图片" width="100" align="center">
          <template #default="{ row }">
            <div class="review-images" v-if="row.images && row.images.length">
              <el-image
                v-for="(img, i) in row.images.slice(0, 3)"
                :key="i"
                :src="img"
                style="width:32px;height:32px;border-radius:6px;margin:0 2px"
                fit="cover"
                :preview-src-list="row.images"
              />
            </div>
            <span v-else class="no-data">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="评价时间" width="170" />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-popconfirm
              title="确定要删除该评价吗？"
              confirm-button-text="确定"
              cancel-button-text="取消"
              @confirm="handleDelete(row.id)"
            >
              <template #reference>
                <el-button type="danger" size="small" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无评价数据" />
        </template>
      </el-table>

      <div class="page-pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @current-change="loadData"
          @size-change="loadData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminAPI } from '@/api'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const filterProductId = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

function handleSearch() {
  currentPage.value = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const res = await adminAPI.reviews({
      page: currentPage.value,
      limit: pageSize.value,
      product_id: filterProductId.value || undefined
    })
    if (res.code === 200) {
      tableData.value = res.data?.list || res.data || []
      total.value = res.meta?.total || res.data?.total || 0
    }
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

async function handleDelete(id) {
  try {
    const res = await adminAPI.reviewDelete(id)
    if (res.code === 200) {
      ElMessage.success('评价已删除')
      loadData()
    }
  } catch (e) {
    // handled by interceptor
  }
}

onMounted(() => { loadData() })
</script>

<style scoped>
.reviews-page {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-card {
  border-radius: 12px;
  border: 1px solid var(--color-border-light);
}

.page-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.toolbar-title {
  font-size: 1.15rem;
  font-weight: 600;
  color: var(--color-text-primary);
}

.page-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.review-images {
  display: flex;
  justify-content: center;
}

.no-data {
  color: var(--color-text-placeholder);
  font-size: 0.85rem;
}
</style>
