<template>
  <div class="cart-page">
    <div class="cart-container" v-if="cartStore.items.length">
      <!-- 购物车表格 -->
      <div class="cart-table-wrapper">
        <el-table
          :data="cartStore.items"
          style="width: 100%"
          v-loading="loading"
          row-key="id"
        >
          <el-table-column width="55" align="center">
            <template #default="{ row }">
              <el-checkbox
                :model-value="row.selected"
                @change="() => handleToggleSelect(row)"
              />
            </template>
          </el-table-column>
          <el-table-column label="商品信息" min-width="300">
            <template #default="{ row }">
              <div class="cart-product" @click="$router.push(`/products/${row.productId}`)">
                <el-image
                  :src="row.product?.coverImage || row.product?.image || row.image"
                  fit="cover"
                  lazy
                  style="width: 80px; height: 80px; border-radius: 8px; flex-shrink: 0"
                />
                <div class="cart-product-info">
                  <p class="cart-product-name">{{ row.product?.name || row.name }}</p>
                  <p class="cart-product-specs" v-if="row.skuSpec">{{ row.skuSpec }}</p>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="140" align="center">
            <template #default="{ row }">
              <span class="unit-price">¥{{ Number(row.product?.price || row.price || 0).toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="数量" width="160" align="center">
            <template #default="{ row }">
              <el-input-number
                v-model="row.quantity"
                :min="1"
                :max="row.product?.stock || 9999"
                size="small"
                @change="(val) => handleQuantityChange(row, val)"
              />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="140" align="center">
            <template #default="{ row }">
              <span class="subtotal">¥{{ (Number(row.product?.price || row.price || 0) * row.quantity).toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center">
            <template #default="{ row }">
              <el-button type="danger" text size="small" @click="handleDelete(row)">
                <el-icon><Delete /></el-icon> 删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 底部操作栏 -->
      <div class="cart-bottom-bar">
        <div class="bottom-left">
          <el-checkbox
            v-model="selectAllChecked"
            :indeterminate="isIndeterminate"
            @change="handleSelectAll"
          >
            全选 ({{ cartStore.totalCount }}件)
          </el-checkbox>
          <el-button text type="danger" @click="handleDeleteSelected" :disabled="!selectedIds.length">
            删除选中
          </el-button>
        </div>
        <div class="bottom-right">
          <span class="total-label">
            已选 <strong>{{ selectedCount }}</strong> 件，合计：
          </span>
          <span class="total-amount">¥{{ cartStore.totalAmount.toFixed(2) }}</span>
          <el-button
            type="primary"
            size="large"
            :disabled="!selectedIds.length"
            @click="handleCheckout"
            class="checkout-btn"
          >
            去结算
          </el-button>
        </div>
      </div>
    </div>

    <!-- 空购物车 -->
    <div class="empty-cart" v-else-if="!loading">
      <el-empty description="购物车是空的，快去挑选好物吧">
        <el-button type="primary" size="large" @click="$router.push('/products')">
          去购物
        </el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'

const router = useRouter()
const cartStore = useCartStore()
const loading = ref(true)

const selectAllChecked = ref(false)
const isIndeterminate = ref(false)

const selectedIds = computed(() => {
  return cartStore.items.filter(i => i.selected).map(i => i.id)
})

const selectedCount = computed(() => {
  return cartStore.items.filter(i => i.selected).reduce((s, i) => s + i.quantity, 0)
})

onMounted(async () => {
  await fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    await cartStore.fetchCart()
    updateSelectAllState()
  } finally {
    loading.value = false
  }
}

function updateSelectAllState() {
  const items = cartStore.items
  if (!items.length) {
    selectAllChecked.value = false
    isIndeterminate.value = false
    return
  }
  const selectedAll = items.every(i => i.selected)
  const selectedSome = items.some(i => i.selected)
  selectAllChecked.value = selectedAll
  isIndeterminate.value = selectedSome && !selectedAll
}

async function handleToggleSelect(row) {
  await cartStore.toggleSelect(row.id)
  updateSelectAllState()
}

async function handleSelectAll(val) {
  await cartStore.selectAll(val)
  updateSelectAllState()
}

async function handleQuantityChange(row, val) {
  if (val < 1) val = 1
  await cartStore.updateQuantity(row.id, val)
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cartStore.removeItem(row.id)
    ElMessage.success('已删除')
    updateSelectAllState()
  } catch {
    // 取消删除
  }
}

async function handleDeleteSelected() {
  if (!selectedIds.value.length) return
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 件商品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cartStore.removeBatch(selectedIds.value)
    ElMessage.success('已删除选中商品')
    updateSelectAllState()
  } catch {
    // 取消删除
  }
}

function handleCheckout() {
  const ids = selectedIds.value.join(',')
  router.push(`/checkout?cart_ids=${ids}`)
}
</script>

<style scoped>
.cart-page {
  max-width: 1800px;
  margin: 0 auto;
  padding: 14px 14px;
  min-height: 60vh;
}

.cart-container {
  background: var(--gradient-card);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: var(--shadow-xs);
  border: 1px solid var(--color-border-light);
}

.cart-table-wrapper {
  padding: 0;
}

.cart-product {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.cart-product-info {
  flex: 1;
  min-width: 0;
}

.cart-product-name {
  font-size: 14px;
  color: var(--color-text-primary);
  margin: 0 0 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.cart-product-specs {
  font-size: 12px;
  color: var(--color-text-placeholder);
  margin: 0;
}

.unit-price {
  color: var(--color-text-primary);
  font-weight: 500;
}

.subtotal {
  font-weight: 600;
  color: #ef4444;
  font-size: 15px;
}

.cart-bottom-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 18px;
  border-top: 1px solid var(--color-border-light);
  background: #ffffff;
  flex-wrap: wrap;
  gap: 12px;
}

.bottom-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.bottom-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.total-label {
  font-size: 14px;
  color: var(--color-text-muted);
}

.total-label strong {
  color: var(--color-text-primary);
}

.total-amount {
  font-size: 1.3rem;
  font-weight: 800;
  color: #ef4444;
}

.checkout-btn {
  height: 42px;
  padding: 0 28px;
  font-size: 15px;
  border-radius: 8px;
  background: var(--color-primary-gradient);
  border: none;
  box-shadow: 0 4px 14px rgba(220, 38, 38, 0.3);
}

.checkout-btn:hover {
  background: linear-gradient(135deg, #b91c1c, #dc2626);
  box-shadow: 0 6px 20px rgba(220, 38, 38, 0.4);
}

.empty-cart {
  padding: 56px 0;
}

@media (max-width: 768px) {
  .cart-bottom-bar {
    flex-direction: column;
    align-items: stretch;
  }
  .bottom-right {
    justify-content: space-between;
  }
}
</style>
