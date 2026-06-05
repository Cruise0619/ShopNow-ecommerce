<template>
  <div class="orders-page">
    <el-card shadow="never" class="page-card">
      <!-- Toolbar -->
      <div class="page-toolbar">
        <div class="toolbar-title">订单管理</div>
        <div style="display: flex; gap: 12px;">
          <el-input
            v-model="searchOrderNo"
            placeholder="搜索订单号"
            clearable
            prefix-icon="Search"
            style="width: 220px"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
          <el-select v-model="filterStatus" placeholder="订单状态" clearable style="width: 150px" @change="handleSearch">
            <el-option label="待付款" value="pending_payment" />
            <el-option label="待发货" value="pending_shipment" />
            <el-option label="已发货" value="shipped" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
            <el-option label="退款中" value="refunding" />
          </el-select>
          <el-date-picker
            v-model="filterDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 260px"
            @change="handleSearch"
          />
          <el-button type="success" @click="exportExcel">
            <el-icon><Download /></el-icon> 导出 Excel
          </el-button>
        </div>
      </div>

      <!-- Table -->
      <el-table
        :data="tableData"
        v-loading="loading"
        style="width: 100%"
        stripe
        :header-cell-style="{ background: '#ffffff', color: '#475569', fontWeight: 600 }"
      >
        <el-table-column type="index" label="#" width="60" align="center" :index="(idx) => (currentPage - 1) * pageSize + idx + 1" />
        <el-table-column prop="orderNo" label="订单号" min-width="200" show-overflow-tooltip />
        <el-table-column prop="userId" label="用户 ID" width="90" align="center" />
        <el-table-column label="订单金额" min-width="140" align="center">
          <template #default="{ row }">
            <span class="price">¥{{ Number(row.totalAmount || row.total).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small" effect="light">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="shippingCompany" label="物流公司" width="110" align="center">
          <template #default="{ row }">{{ row.shippingCompany || '-' }}</template>
        </el-table-column>
        <el-table-column prop="trackingNo" label="物流单号" width="180" align="center" show-overflow-tooltip>
          <template #default="{ row }">{{ row.trackingNo || '-' }}</template>
        </el-table-column>
        <el-table-column label="商品数" width="90" align="center">
          <template #default="{ row }">{{ row.items?.length || 0 }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="下单时间" min-width="180" />
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="viewDetail(row.id)">详情</el-button>
            <template v-if="row.status === 'pending_shipment'">
              <el-button type="success" size="small" link @click="handleShip(row)">发货</el-button>
            </template>
            <template v-if="['pending_payment', 'pending_shipment'].includes(row.status)">
              <el-popconfirm title="确定取消该订单？" confirm-button-text="确定" cancel-button-text="取消" @confirm="handleCancel(row.id)">
                <template #reference>
                  <el-button type="warning" size="small" link>取消</el-button>
                </template>
              </el-popconfirm>
            </template>
            <template v-if="['completed'].includes(row.status)">
              <el-popconfirm title="确定退款？" confirm-button-text="确定" cancel-button-text="取消" @confirm="handleRefund(row.id)">
                <template #reference>
                  <el-button type="danger" size="small" link>退款</el-button>
                </template>
              </el-popconfirm>
            </template>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无订单数据" />
        </template>
      </el-table>

      <!-- Ship Dialog -->
      <el-dialog v-model="shipDialogVisible" title="确认发货" width="450px" :close-on-click-modal="false">
        <el-form ref="shipFormRef" :model="shipForm" :rules="shipRules" label-width="80px">
          <el-form-item label="物流公司" prop="company">
            <el-select v-model="shipForm.company" placeholder="请选择物流公司" style="width: 100%">
              <el-option
                v-for="c in courierOptions"
                :key="c"
                :label="c"
                :value="c"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="物流单号" prop="tracking_no">
            <div style="display: flex; gap: 8px;">
              <el-input v-model="shipForm.tracking_no" placeholder="自动生成或手动输入" style="flex: 1" />
              <el-button @click="shipForm.tracking_no = genTrackingNo()">随机生成</el-button>
            </div>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="shipDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="shipLoading" @click="confirmShip">确认发货</el-button>
        </template>
      </el-dialog>

      <!-- Pagination -->
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
import { useRouter } from 'vue-router'
import { adminAPI } from '@/api'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const searchOrderNo = ref('')
const filterStatus = ref('')
const filterDateRange = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const statusMap = {
  pending_payment: '待付款',
  pending_shipment: '待发货',
  shipped: '已发货',
  completed: '已完成',
  cancelled: '已取消',
  refunding: '退款中',
  refunded: '已退款'
}

function statusLabel(s) {
  return statusMap[s] || s
}

function statusType(s) {
  const map = {
    pending_payment: 'warning',
    pending_shipment: 'primary',
    shipped: 'info',
    completed: 'success',
    cancelled: 'danger',
    refunding: 'warning',
    refunded: 'danger'
  }
  return map[s] || 'info'
}

function handleSearch() {
  currentPage.value = 1
  loadData()
}

function viewDetail(id) {
  router.push(`/admin/orders/${id}`)
}

async function loadData() {
  loading.value = true
  try {
    const res = await adminAPI.orders({
      page: currentPage.value,
      limit: pageSize.value,
      order_no: searchOrderNo.value || undefined,
      status: filterStatus.value || undefined,
      start_date: filterDateRange.value?.[0] || undefined,
      end_date: filterDateRange.value?.[1] || undefined,
      sort: 'id',
      order: 'desc'
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

const courierOptions = [
  '顺丰速运', '中通快递', '圆通速递', '韵达快递',
  '申通快递', '极兔速递', '京东物流', '德邦快递', 'EMS'
]

const courierCodes = {
  '顺丰速运': 'SF', '中通快递': 'ZTO', '圆通速递': 'YTO',
  '韵达快递': 'YD', '申通快递': 'STO', '极兔速递': 'JT',
  '京东物流': 'JD', '德邦快递': 'DB', 'EMS': 'EMS'
}

function genTrackingNo() {
  const code = courierCodes[shipForm.value.company] || 'EXP'
  const ts = String(Date.now()).slice(-8)
  const rand = String(Math.floor(Math.random() * 1000000)).padStart(6, '0')
  return `${code}${ts}${rand}`
}

const shipDialogVisible = ref(false)
const shipLoading = ref(false)
const shipFormRef = ref(null)
const shipForm = ref({ company: '', tracking_no: '' })
const shippingOrderId = ref(null)
const shipRules = {
  company: [{ required: true, message: '请选择物流公司', trigger: 'change' }],
  tracking_no: [{ required: true, message: '请输入物流单号', trigger: 'blur' }]
}

function handleShip(row) {
  shipForm.value = { company: '', tracking_no: '' }
  shippingOrderId.value = row.id
  shipDialogVisible.value = true
}

async function confirmShip() {
  const valid = await shipFormRef.value.validate().catch(() => false)
  if (!valid) return
  shipLoading.value = true
  try {
    const res = await adminAPI.orderShip(shippingOrderId.value, shipForm.value)
    if (res.code === 200) {
      ElMessage.success('已发货')
      shipDialogVisible.value = false
      loadData()
    }
  } catch (e) {
    // handled by interceptor
  } finally {
    shipLoading.value = false
  }
}

async function handleCancel(id) {
  try {
    const res = await adminAPI.orderCancel(id)
    if (res.code === 200) {
      ElMessage.success('订单已取消')
      loadData()
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch (e) {
    // handled by interceptor
  }
}

async function handleRefund(id) {
  try {
    const res = await adminAPI.orderRefund(id)
    if (res.code === 200) {
      ElMessage.success('退款已处理')
      loadData()
    } else {
      ElMessage.error(res.message || '退款失败')
    }
  } catch (e) {
    // handled by interceptor
  }
}

async function exportExcel() {
  try {
    const response = await request.get('/admin/orders/export', { responseType: 'blob' })
    const url = window.URL.createObjectURL(new Blob([response]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `orders_${Date.now()}.xlsx`)
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.orders-page {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-card {
  border-radius: 4px;
  border: 1px solid var(--color-border-light);
}

.page-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.toolbar-title {
  font-size: 1.15rem;
  font-weight: 600;
  color: var(--color-text-primary);
}

.price {
  color: var(--color-danger);
  font-weight: 600;
}

.page-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
