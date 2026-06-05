<template>
  <div class="order-detail-page" v-loading="loading">
    <!-- Back button -->
    <div class="page-header">
      <el-button @click="goBack" text>
        <el-icon><ArrowLeft /></el-icon> 返回订单列表
      </el-button>
    </div>

    <el-row :gutter="20" v-if="order">
      <!-- Order Info -->
      <el-col :span="16">
        <el-card shadow="never" class="detail-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">订单信息</span>
              <el-tag :type="statusType(order.status)" size="large" effect="light">
                {{ statusLabel(order.status) }}
              </el-tag>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="下单时间">{{ order.createdAt }}</el-descriptions-item>
            <el-descriptions-item label="支付方式">{{ order.paymentMethod || '—' }}</el-descriptions-item>
            <el-descriptions-item label="支付时间">{{ order.paymentTime || '—' }}</el-descriptions-item>
            <el-descriptions-item label="订单备注" :span="2">{{ order.remark || '无' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- Logistics Info -->
        <el-card shadow="never" class="detail-card" v-if="order.trackingNo">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span class="card-title">物流信息</span>
              <span style="font-size:13px;color:var(--color-primary);font-weight:500">运单号：{{ order.trackingNo }}</span>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="运单号">{{ order.trackingNo }}</el-descriptions-item>
            <el-descriptions-item label="发货时间">{{ order.shippingTime || '—' }}</el-descriptions-item>
          </el-descriptions>
          <el-timeline style="margin-top:16px">
            <el-timeline-item
              v-for="(step, idx) in adminLogisticsSteps"
              :key="idx"
              :timestamp="step.time"
              :color="idx === adminLogisticsSteps.length - 1 ? '#10b981' : '#d1d5db'"
            >
              {{ step.text }}
            </el-timeline-item>
          </el-timeline>
        </el-card>

        <!-- Address Info -->
        <el-card shadow="never" class="detail-card">
          <template #header><span class="card-title">收货信息</span></template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="收货人">{{ order.addressSnapshot?.receiver || '—' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ order.addressSnapshot?.phone || '—' }}</el-descriptions-item>
            <el-descriptions-item label="收货地址" :span="2">
              {{ order.addressSnapshot?.province || '' }}{{ order.addressSnapshot?.city || '' }}{{ order.addressSnapshot?.district || '' }}{{ order.addressSnapshot?.detail || '' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- Products -->
        <el-card shadow="never" class="detail-card">
          <template #header><span class="card-title">商品列表</span></template>
          <el-table :data="order.items || []" style="width: 100%" stripe>
            <el-table-column label="商品图片" width="80" align="center">
              <template #default="{ row }">
                <el-image :src="row.product?.coverImage || row.product?.image || 'https://placehold.co/48x48/f1f5f9/94a3b8'" style="width:48px;height:48px;border-radius:3px" fit="cover" />
              </template>
            </el-table-column>
            <el-table-column label="商品名称" min-width="200" show-overflow-tooltip>
              <template #default="{ row }">{{ row.product?.name }}</template>
            </el-table-column>
            <el-table-column label="单价" width="100" align="center">
              <template #default="{ row }">¥{{ Number(row.price).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="70" align="center" />
            <el-table-column label="小计" width="110" align="center">
              <template #default="{ row }">
                <span class="price">¥{{ (Number(row.price) * row.quantity).toFixed(2) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- Sidebar -->
      <el-col :span="8">
        <!-- Amounts -->
        <el-card shadow="never" class="detail-card">
          <template #header><span class="card-title">金额汇总</span></template>
          <div class="amount-row">
            <span>商品总额</span>
            <span>¥{{ Number(order.totalAmount || order.total || 0).toFixed(2) }}</span>
          </div>
          <div class="amount-row">
            <span>运费</span>
            <span>免运费</span>
          </div>
          <div class="amount-row total">
            <span>实付金额</span>
            <span class="price">¥{{ Number(order.paymentAmount || order.totalAmount || order.total || 0).toFixed(2) }}</span>
          </div>
        </el-card>

        <!-- Actions -->
        <el-card shadow="never" class="detail-card" v-if="order">
          <template #header><span class="card-title">操作</span></template>
          <div class="action-group">
            <el-button
              v-if="order.status === 'pending_shipment'"
              type="primary"
              style="width: 100%; margin-bottom: 10px"
              @click="handleShip"
            >
              <el-icon><Van /></el-icon> 确认发货
            </el-button>
            <el-button
              v-if="['pending_payment', 'pending_shipment'].includes(order.status)"
              type="warning"
              style="width: 100%; margin-bottom: 10px"
              @click="handleCancel"
            >
              <el-icon><Close /></el-icon> 取消订单
            </el-button>
            <el-button
              v-if="order.status === 'completed'"
              type="danger"
              style="width: 100%"
              @click="handleRefund"
            >
              <el-icon><Wallet /></el-icon> 退款处理
            </el-button>
            <el-empty v-if="!hasActions" description="暂无可执行操作" :image-size="60" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Ship Dialog -->
    <el-dialog v-model="shipDialogVisible" title="确认发货" width="450px" :close-on-click-modal="false">
      <el-form ref="shipFormRef" :model="shipForm" :rules="shipRules" label-width="80px">
        <el-form-item label="物流公司" prop="company">
          <el-input v-model="shipForm.company" placeholder="如：顺丰速运" />
        </el-form-item>
        <el-form-item label="物流单号" prop="tracking_no">
          <el-input v-model="shipForm.tracking_no" placeholder="请输入快递单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="actionLoading" @click="confirmShip">确认发货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { adminAPI } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const actionLoading = ref(false)
const order = ref(null)
const shipDialogVisible = ref(false)
const shipFormRef = ref(null)
const shipForm = ref({ company: '', tracking_no: '' })
const shipRules = {
  company: [{ required: true, message: '请输入物流公司', trigger: 'blur' }],
  tracking_no: [{ required: true, message: '请输入物流单号', trigger: 'blur' }]
}

const statusMap = {
  pending_payment: '待付款',
  pending_shipment: '待发货',
  shipped: '已发货',
  completed: '已完成',
  cancelled: '已取消',
  refunding: '退款中',
  refunded: '已退款'
}

function statusLabel(s) { return statusMap[s] || s }

function statusType(s) {
  const map = { pending_payment: 'warning', pending_shipment: 'primary', shipped: 'info', completed: 'success', cancelled: 'danger', refunding: 'warning', refunded: 'danger' }
  return map[s] || 'info'
}

const adminLogisticsSteps = computed(() => {
  if (!order.value || !order.value.trackingNo) return []
  const created = order.value.createdAt ? new Date(order.value.createdAt) : new Date()
  const shipped = order.value.shippingTime ? new Date(order.value.shippingTime) : null
  const receiveTime = order.value.receiveTime ? new Date(order.value.receiveTime) : null
  const fmt = d => {
    const m = d.getMonth() + 1; const day = d.getDate()
    const h = String(d.getHours()).padStart(2, '0'); const min = String(d.getMinutes()).padStart(2, '0')
    return `${m}-${day} ${h}:${min}`
  }
  const steps = [
    { text: '订单已创建', time: fmt(created), done: true }
  ]
  if (shipped) {
    steps.push({ text: '商家已发货', time: fmt(shipped), done: true })
  }
  if (order.value.status === 'shipped' || order.value.status === 'completed' || order.value.status === 'refunding' || order.value.status === 'refunded') {
    if (shipped) {
      const transit = new Date(shipped.getTime() + 24 * 60 * 60 * 1000)
      steps.push({ text: '快件运输中', time: fmt(transit), done: order.value.status !== 'shipped' })
    }
  }
  if (order.value.status === 'completed' || order.value.status === 'refunding' || order.value.status === 'refunded') {
    if (receiveTime) {
      steps.push({ text: '包裹已签收', time: fmt(receiveTime), done: true })
    }
  }
  return steps
})

const hasActions = computed(() => {
  if (!order.value) return false
  return ['pending_shipment', 'pending_payment', 'completed'].includes(order.value.status)
})

function goBack() { router.push('/admin/orders') }

async function loadDetail() {
  loading.value = true
  try {
    const res = await adminAPI.orderDetail(route.params.id)
    if (res.code === 200) {
      order.value = res.data
    }
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function handleShip() {
  shipForm.value = { company: '', tracking_no: '' }
  shipDialogVisible.value = true
}

async function confirmShip() {
  const valid = await shipFormRef.value.validate().catch(() => false)
  if (!valid) return
  actionLoading.value = true
  try {
    const res = await adminAPI.orderShip(order.value.id, shipForm.value)
    if (res.code === 200) {
      ElMessage.success('发货成功')
      shipDialogVisible.value = false
      loadDetail()
    }
  } catch (e) {
    // handled by interceptor
  } finally {
    actionLoading.value = false
  }
}

async function handleCancel() {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '确认取消', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    actionLoading.value = true
    const res = await adminAPI.orderCancel(order.value.id)
    if (res.code === 200) {
      ElMessage.success('订单已取消')
      loadDetail()
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch (e) {
    if (e !== 'cancel') { /* handled by interceptor */ }
  } finally {
    actionLoading.value = false
  }
}

async function handleRefund() {
  try {
    await ElMessageBox.confirm('确定要退款吗？', '确认退款', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    actionLoading.value = true
    const res = await adminAPI.orderRefund(order.value.id)
    if (res.code === 200) {
      ElMessage.success('退款已处理')
      loadDetail()
    } else {
      ElMessage.error(res.message || '退款失败')
    }
  } catch (e) {
    if (e !== 'cancel') { /* handled by interceptor */ }
  } finally {
    actionLoading.value = false
  }
}

onMounted(() => { loadDetail() })
</script>

<style scoped>
.order-detail-page {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-header {
  margin-bottom: 20px;
}

.detail-card {
  border: 1px solid var(--color-border-light);
  border-radius: 4px;
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-text-primary);
}

.price {
  color: var(--color-danger);
  font-weight: 600;
}

.amount-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  font-size: 0.95rem;
  color: var(--color-text-body);
}

.amount-row.total {
  border-top: 1px solid var(--color-border);
  padding-top: 14px;
  margin-top: 4px;
  font-weight: 700;
  font-size: 1.05rem;
  color: var(--color-text-primary);
}

.amount-row.total .price {
  font-size: 1.15rem;
}

.action-group {
  min-height: 80px;
}
</style>
