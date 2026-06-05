<template>
  <div class="order-detail-page" v-loading="loading">
    <div class="page-header">
      <h1 class="page-title">订单详情</h1>
      <el-button text @click="$router.push('/orders')">
        <el-icon><ArrowLeft /></el-icon> 返回订单列表
      </el-button>
    </div>

    <template v-if="order.id">
      <!-- 订单状态 -->
      <el-card class="detail-card status-card" shadow="never">
        <div class="status-content">
          <div class="status-icon-wrapper">
            <el-icon size="40" :class="'status-icon status-' + order.status">
              <Clock v-if="order.status === 'pending_payment' || order.status === 'pending_shipment'" />
              <Van v-else-if="order.status === 'shipped'" />
              <CircleCheck v-else-if="order.status === 'completed'" />
              <CircleClose v-else />
            </el-icon>
          </div>
          <div class="status-text-wrapper">
            <h2 class="status-text">{{ statusText(order.status) }}</h2>
            <p class="status-desc">{{ statusDesc(order.status) }}</p>
          </div>
        </div>
      </el-card>

      <!-- 物流追踪 -->
      <el-card class="detail-card" shadow="never" v-if="order.trackingNo">
        <template #header>
          <div class="card-header-row">
            <span class="card-title">物流追踪</span>
            <span class="tracking-no">运单号：{{ order.trackingNo }}</span>
          </div>
        </template>
        <el-timeline>
          <el-timeline-item
            v-for="(step, idx) in logisticsSteps"
            :key="idx"
            :timestamp="step.time"
            :color="idx === logisticsSteps.length - 1 ? '#10b981' : '#d1d5db'"
          >
            {{ step.text }}
          </el-timeline-item>
        </el-timeline>
      </el-card>

      <!-- 收货地址 -->
      <el-card class="detail-card" shadow="never">
        <template #header><span class="card-title">收货信息</span></template>
        <div class="address-info" v-if="order.addressSnapshot">
          <p>
            <strong>{{ order.addressSnapshot.receiver }}</strong>
            {{ order.addressSnapshot.phone }}
          </p>
          <p class="address-full">
            {{ order.addressSnapshot.province }}
            {{ order.addressSnapshot.city }}
            {{ order.addressSnapshot.district }}
            {{ order.addressSnapshot.detail }}
          </p>
        </div>
        <p v-else class="no-data">地址信息不可用</p>
      </el-card>

      <!-- 商品列表 -->
      <el-card class="detail-card" shadow="never">
        <template #header><span class="card-title">商品信息</span></template>
        <el-table :data="order.items || []" style="width: 100%">
          <el-table-column label="商品" min-width="260">
            <template #default="{ row }">
              <div class="table-product" @click="$router.push(`/products/${row.productId}`)">
                <el-image
                  :src="row.product?.coverImage || row.product?.image || row.image"
                  fit="cover"
                  style="width: 64px; height: 64px; border-radius: 8px"
                />
                <div class="table-product-info">
                  <p class="table-product-name">{{ row.product?.name || row.name }}</p>
                  <p class="table-product-specs" v-if="row.specs">{{ row.specs }}</p>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="120" align="center">
            <template #default="{ row }">
              ¥{{ Number(row.product?.price || row.price || 0).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="数量" width="80" align="center">
            <template #default="{ row }">{{ row.quantity }}</template>
          </el-table-column>
          <el-table-column label="小计" width="120" align="center">
            <template #default="{ row }">
              <span class="subtotal-cell">
                ¥{{ (Number(row.product?.price || row.price || 0) * row.quantity).toFixed(2) }}
              </span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 订单信息 -->
      <el-card class="detail-card" shadow="never">
        <template #header><span class="card-title">订单信息</span></template>
        <div class="order-info-grid">
          <div class="info-item">
            <span class="info-label">订单编号</span>
            <span class="info-value">{{ order.orderNo }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">创建时间</span>
            <span class="info-value">{{ order.createdAt }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">支付方式</span>
            <span class="info-value">{{ paymentMethodText(order.paymentMethod) }}</span>
          </div>
          <div class="info-item" v-if="order.remark">
            <span class="info-label">订单备注</span>
            <span class="info-value">{{ order.remark }}</span>
          </div>
          <div class="info-item" v-if="order.trackingNo">
            <span class="info-label">物流单号</span>
            <span class="info-value tracking">{{ order.trackingNo }}</span>
          </div>
        </div>
      </el-card>

      <!-- 费用明细 -->
      <el-card class="detail-card" shadow="never">
        <template #header><span class="card-title">费用明细</span></template>
        <div class="amount-list">
          <div class="amount-item">
            <span>商品金额</span>
            <span>¥{{ Number(order.totalAmount || order.total || 0).toFixed(2) }}</span>
          </div>
          <div class="amount-item" v-if="(order.discountAmount || order.discount) && (order.discountAmount || order.discount) > 0">
            <span>优惠金额</span>
            <span class="discount-txt">-¥{{ Number(order.discountAmount || order.discount).toFixed(2) }}</span>
          </div>
          <div class="amount-item">
            <span>运费</span>
            <span>免运费</span>
          </div>
          <el-divider />
          <div class="amount-item total">
            <span>实付金额</span>
            <span class="final">¥{{ Number(order.paymentAmount || order.totalAmount || order.total || 0).toFixed(2) }}</span>
          </div>
        </div>
      </el-card>

      <!-- 操作按钮 -->
      <div class="action-bar" v-if="order.status !== 'cancelled'">
        <el-button
          v-if="order.status === 'pending_payment'"
          type="primary"
          size="large"
          @click="handlePay"
        >
          立即付款
        </el-button>
        <el-button
          v-if="order.status === 'pending_payment' || order.status === 'pending_shipment'"
          type="warning"
          size="large"
          plain
          @click="handleCancel"
        >
          取消订单
        </el-button>
        <el-button
          v-if="order.status === 'shipped'"
          type="success"
          size="large"
          @click="handleConfirm"
        >
          确认收货
        </el-button>
        <el-button
          v-if="order.status === 'completed'"
          type="danger"
          size="large"
          plain
          @click="handleRefund"
        >
          申请退款
        </el-button>
      </div>
    </template>

    <el-empty v-else description="订单不存在" />

    <!-- 支付二维码弹窗 -->
    <el-dialog v-model="showPayDialog" title="扫码支付" width="380px" :close-on-click-modal="false" center>
      <div class="pay-dialog-body">
        <div class="pay-amount-info">
          <span class="pay-label">支付金额</span>
          <span class="pay-amount">¥{{ Number(order.paymentAmount || order.totalAmount || order.total || 0).toFixed(2) }}</span>
        </div>
        <div class="pay-method-tag">
          <span>{{ order.paymentMethod === 'wechat' ? '微信支付' : '支付宝' }}</span>
        </div>
        <div class="qr-code-wrapper">
          <img :src="qrDataUrl" alt="支付二维码" class="qr-code" v-if="qrDataUrl" />
          <div class="qr-placeholder" v-else>
            <el-icon class="qr-loading-icon" :size="32"><Loading /></el-icon>
            <span>二维码加载中...</span>
          </div>
        </div>
        <div class="pay-hint-row">
          <el-icon class="pay-hint-icon"><Cellphone /></el-icon>
          <span>请使用{{ order.paymentMethod === 'wechat' ? '微信' : '支付宝' }}扫描二维码支付</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="showPayDialog = false">取 消</el-button>
        <el-button type="primary" :loading="payLoading" @click="confirmPay">
          模拟扫码支付
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderAPI } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Cellphone, Clock, Van, CircleCheck, CircleClose, Loading } from '@element-plus/icons-vue'
import { toDataURL } from 'qrcode'
import { getPaymentBaseUrl } from '@/utils/payment'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const order = ref({})

const statusMap = {
  pending_payment: { text: '待付款', desc: '请尽快完成支付，超时订单将自动取消', type: 'warning' },
  pending_shipment: { text: '待发货', desc: '商家正在为您准备商品，请耐心等待', type: 'primary' },
  shipped: { text: '待收货', desc: '商品正在运输途中，请注意查收', type: 'info' },
  completed: { text: '已完成', desc: '订单已完成，感谢您的购买', type: 'success' },
  cancelled: { text: '已取消', desc: '该订单已取消', type: 'danger' },
  refunding: { text: '退款中', desc: '退款正在处理中', type: 'warning' },
  refunded: { text: '已退款', desc: '退款已完成', type: 'danger' }
}

const logisticsSteps = ref([])

function generateLogisticsSteps(orderData) {
  const trackingNo = orderData.trackingNo
  if (!trackingNo) { logisticsSteps.value = []; return }
  const created = orderData.createdAt ? new Date(orderData.createdAt) : new Date()
  const shipped = orderData.shippingTime ? new Date(orderData.shippingTime) : null
  const receiveTime = orderData.receiveTime ? new Date(orderData.receiveTime) : null
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
  const s = orderData.status
  if (s === 'shipped' || s === 'completed' || s === 'refunding' || s === 'refunded') {
    if (shipped) {
      const transit = new Date(shipped.getTime() + 24 * 60 * 60 * 1000)
      steps.push({ text: '快件运输中', time: fmt(transit), done: s !== 'shipped' })
    }
  }
  if (s === 'completed' || s === 'refunding' || s === 'refunded') {
    if (receiveTime) {
      steps.push({ text: '包裹已签收', time: fmt(receiveTime), done: true })
    }
  }
  logisticsSteps.value = steps
}

const paymentMethodMap = {
  alipay: '支付宝',
  wechat: '微信支付',
  bank: '银行卡'
}

function statusText(status) {
  return statusMap[status]?.text || status || '未知'
}

function statusDesc(status) {
  return statusMap[status]?.desc || ''
}

function paymentMethodText(method) {
  return paymentMethodMap[method] || method || '未选择'
}

onMounted(async () => {
  const id = route.params.id
  await fetchOrder(id)
})

async function fetchOrder(id) {
  loading.value = true
  try {
    const res = await orderAPI.detail(id)
    if (res.code === 200) {
      order.value = res.data
      generateLogisticsSteps(res.data)
    }
  } catch {
    ElMessage.error('加载订单信息失败')
  } finally {
    loading.value = false
  }
}

const showPayDialog = ref(false)
const payLoading = ref(false)
const qrDataUrl = ref('')

async function handlePay() {
  showPayDialog.value = true
  qrDataUrl.value = ''
  if (order.value.payToken) {
    const baseUrl = await getPaymentBaseUrl()
    const payUrl = baseUrl + '/pay/' + order.value.payToken
    qrDataUrl.value = await toDataURL(payUrl, { width: 256, margin: 2, color: { dark: '#000', light: '#fff' } })
  }
}

async function confirmPay() {
  payLoading.value = true
  try {
    const res = await orderAPI.pay(order.value.id)
    if (res.code === 200) {
      showPayDialog.value = false
      ElMessage.success('支付成功')
      fetchOrder(order.value.id)
    } else {
      ElMessage.error(res.message || '支付失败')
    }
  } catch {
    ElMessage.error('支付失败，请稍后重试')
  } finally {
    payLoading.value = false
  }
}

async function handleCancel() {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '取消订单', {
      confirmButtonText: '确定取消',
      cancelButtonText: '返回',
      type: 'warning'
    })
    const res = await orderAPI.cancel(order.value.id)
    if (res.code === 200) {
      ElMessage.success('订单已取消')
      fetchOrder(order.value.id)
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch {
    // 取消
  }
}

async function handleRefund() {
  try {
    await ElMessageBox.confirm('确定要申请退款吗？退款将在审核后处理。', '申请退款', {
      confirmButtonText: '确定申请',
      cancelButtonText: '再想想',
      type: 'warning'
    })
    const res = await orderAPI.refund(order.value.id)
    if (res.code === 200) {
      ElMessage.success('退款申请已提交')
      fetchOrder(order.value.id)
    } else {
      ElMessage.error(res.message || '申请失败')
    }
  } catch {
    // 取消
  }
}

async function handleConfirm() {
  try {
    await ElMessageBox.confirm('确认已收到商品吗？', '确认收货', {
      confirmButtonText: '确认收货',
      cancelButtonText: '再等等',
      type: 'info'
    })
    const res = await orderAPI.confirm(order.value.id)
    if (res.code === 200) {
      ElMessage.success('已确认收货')
      fetchOrder(order.value.id)
    } else {
      ElMessage.error(res.message || '确认失败')
    }
  } catch {
    // 取消
  }
}
</script>

<style scoped>
.order-detail-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 14px 14px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-title {
  font-size: 1.3rem;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0;
}

.detail-card {
  margin-bottom: 20px;
  border-radius: 12px;
  border: 1px solid var(--color-border-light);
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.card-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tracking-no {
  font-size: 13px;
  color: var(--color-primary);
  font-weight: 500;
}

.status-card {
  background: #ffffff;
}

.status-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.status-icon.status-pending_payment { color: #ef4444; }
.status-icon.status-pending_shipment { color: #dc2626; }
.status-icon.status-shipped { color: #3b82f6; }
.status-icon.status-completed { color: #10b981; }
.status-icon.status-cancelled { color: #ef4444; }

.status-text {
  font-size: 1.3rem;
  font-weight: 700;
  margin: 0 0 4px;
  color: var(--color-text-primary);
}

.status-desc {
  font-size: 14px;
  color: var(--color-text-muted);
  margin: 0;
}

.address-info p {
  margin: 0 0 4px;
  color: var(--color-text-body);
  font-size: 15px;
}

.no-data {
  color: var(--color-text-placeholder);
  font-size: 14px;
}

.table-product {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.table-product-info {
  min-width: 0;
}

.table-product-name {
  font-size: 14px;
  color: var(--color-text-primary);
  margin: 0 0 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.table-product-specs {
  font-size: 12px;
  color: var(--color-text-placeholder);
  margin: 0;
}

.subtotal-cell {
  font-weight: 700;
  color: #ef4444;
}

.order-info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.info-item {
  display: flex;
  gap: 12px;
  font-size: 14px;
}

.info-label {
  color: var(--color-text-placeholder);
  min-width: 72px;
}

.info-value {
  color: var(--color-text-primary);
}

.info-value.tracking {
  color: var(--color-primary);
  font-weight: 500;
}

.amount-list {
  padding: 0;
}

.amount-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
  color: var(--color-text-muted);
}

.amount-item.total {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.amount-item .final {
  font-size: 1.2rem;
  font-weight: 800;
  color: #ef4444;
  font-weight: 700;
}

.discount-txt {
  color: var(--color-success);
  font-weight: 500;
}

.action-bar {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 0;
}

@media (max-width: 768px) {
  .order-info-grid {
    grid-template-columns: 1fr;
  }
}

/* ===== 支付弹窗 ===== */
.pay-dialog-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 8px 0;
}

.pay-amount-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.pay-label {
  font-size: 14px;
  color: var(--color-text-muted);
}

.pay-amount {
  font-size: 2rem;
  font-weight: 800;
  color: #ef4444;
}

.pay-method-tag {
  background: linear-gradient(135deg, #dc2626, #ef4444);
  color: #fff;
  padding: 4px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
}

.qr-code-wrapper {
  width: 200px;
  height: 200px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
  border: 3px solid #dc2626;
}

.qr-code {
  width: 100%;
  height: 100%;
  display: block;
}

.qr-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--color-text-placeholder);
  font-size: 13px;
}

.pay-hint-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--color-text-muted);
}

.pay-hint-icon {
  font-size: 18px;
  color: var(--color-primary);
  animation: payPulse 1.5s ease-in-out infinite;
}

@keyframes payPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>
