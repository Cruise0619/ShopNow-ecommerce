<template>
  <div class="orders-page">
    <h1 class="page-title">我的订单</h1>

    <el-tabs v-model="activeStatus" @tab-change="handleTabChange" type="card">
      <el-tab-pane label="全部订单" name="" />
      <el-tab-pane label="待付款" name="pending_payment" />
      <el-tab-pane label="待发货" name="pending_shipment" />
      <el-tab-pane label="待收货" name="shipped" />
      <el-tab-pane label="已完成" name="completed" />
      <el-tab-pane label="已取消" name="cancelled" />
    </el-tabs>

    <div v-loading="loading" class="order-list-wrapper">
      <template v-if="orders.length">
        <div v-for="order in orders" :key="order.id" class="order-card">
          <!-- 订单头部 -->
          <div class="order-header">
            <div class="order-header-left">
              <span class="order-no">订单号：{{ order.orderNo }}</span>
              <span class="order-time">{{ order.createdAt }}</span>
            </div>
            <el-tag :type="statusTagType(order.status)" size="small">
              {{ statusText(order.status) }}
            </el-tag>
          </div>

          <!-- 订单商品 -->
          <div class="order-items">
            <div
              v-for="item in (order.items || [])"
              :key="item.id"
              class="order-item"
              @click="$router.push(`/products/${item.productId}`)"
            >
              <el-image
                :src="item.product?.coverImage || item.product?.image || item.image"
                fit="cover"
                style="width: 72px; height: 72px; border-radius: 3px; flex-shrink: 0"
              />
              <div class="order-item-info">
                <p class="order-item-name">{{ item.product?.name || item.name }}</p>
                <p class="order-item-specs" v-if="item.specs">{{ item.specs }}</p>
              </div>
              <div class="order-item-price">
                <span>¥{{ Number(item.product?.price || item.price || 0).toFixed(2) }}</span>
                <span class="order-item-qty">x{{ item.quantity }}</span>
              </div>
            </div>
          </div>

          <!-- 订单底部 -->
          <div class="order-footer">
            <span class="order-total">
              共 {{ (order.items || []).reduce((s, i) => s + i.quantity, 0) }} 件商品，
              合计：<strong>¥{{ Number(order.totalAmount || order.total || 0).toFixed(2) }}</strong>
            </span>
            <div class="order-actions">
              <el-button
                v-if="order.status === 'pending_payment'"
                type="primary"
                size="small"
                @click="handlePay(order)"
              >
                立即付款
              </el-button>
              <el-button
                v-if="order.status === 'pending_payment' || order.status === 'pending_shipment'"
                type="warning"
                size="small"
                plain
                @click="handleCancel(order)"
              >
                取消订单
              </el-button>
              <el-button
                v-if="order.status === 'shipped'"
                type="success"
                size="small"
                @click="handleConfirm(order)"
              >
                确认收货
              </el-button>
              <el-button
                v-if="order.status === 'completed'"
                type="primary"
                size="small"
                plain
                @click="$router.push(`/products/${order.items?.[0]?.productId}`)"
              >
                评价
              </el-button>
              <el-button
                v-if="order.status === 'completed'"
                type="danger"
                size="small"
                plain
                @click="handleRefund(order)"
              >
                申请退款
              </el-button>
              <el-button
                v-if="(order.status === 'shipped' || order.status === 'completed') && order.trackingNo"
                type="primary"
                size="small"
                plain
                @click="openLogistics(order)"
              >
                <el-icon><Van /></el-icon> 查看物流
              </el-button>
              <el-button
                size="small"
                plain
                @click="$router.push(`/orders/${order.id}`)"
              >
                查看详情
              </el-button>
            </div>
          </div>
        </div>
      </template>
      <el-empty v-else description="暂无订单">
        <el-button type="primary" @click="$router.push('/products')">去购物</el-button>
      </el-empty>
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

    <!-- 物流追踪弹窗 -->
    <el-dialog v-model="logisticsVisible" title="物流追踪" width="500px" :close-on-click-modal="false">
      <template v-if="logisticsOrder">
        <div class="logistics-header">
          <span class="logistics-label">运单号：</span>
          <span class="logistics-no">{{ logisticsOrder.trackingNo }}</span>
        </div>
        <el-timeline style="margin-top: 16px">
          <el-timeline-item
            v-for="(step, idx) in logisticsSteps"
            :key="idx"
            :timestamp="step.time"
            :color="idx === logisticsSteps.length - 1 ? '#10b981' : '#d1d5db'"
          >
            {{ step.text }}
          </el-timeline-item>
        </el-timeline>
      </template>
      <template #footer>
        <el-button @click="logisticsVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 支付二维码弹窗 -->
    <el-dialog v-model="payDialogVisible" title="扫码支付" width="380px" :close-on-click-modal="false" center>
      <template v-if="payingOrder">
        <div class="pay-dialog-body">
          <div class="pay-amount-info">
            <span class="pay-label">支付金额</span>
            <span class="pay-amount">¥{{ Number(payingOrder.paymentAmount || payingOrder.totalAmount || payingOrder.total || 0).toFixed(2) }}</span>
          </div>
          <div class="pay-method-tag">
            <span>{{ payingOrder.paymentMethod === 'wechat' ? '微信支付' : '支付宝' }}</span>
          </div>
          <div class="qr-code-wrapper">
            <svg class="qr-code" viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
              <rect x="0" y="0" width="200" height="200" rx="8" fill="#fff" />
              <rect x="12" y="12" width="52" height="52" rx="8" fill="none" stroke="#1e293b" stroke-width="8" />
              <rect x="24" y="24" width="28" height="28" rx="4" fill="#1e293b" />
              <rect x="136" y="12" width="52" height="52" rx="8" fill="none" stroke="#1e293b" stroke-width="8" />
              <rect x="148" y="24" width="28" height="28" rx="4" fill="#1e293b" />
              <rect x="12" y="136" width="52" height="52" rx="8" fill="none" stroke="#1e293b" stroke-width="8" />
              <rect x="24" y="148" width="28" height="28" rx="4" fill="#1e293b" />
              <g fill="#1e293b">
                <rect x="72" y="16" width="8" height="8" rx="1" /><rect x="84" y="16" width="8" height="8" rx="1" />
                <rect x="96" y="16" width="8" height="8" rx="1" /><rect x="112" y="16" width="8" height="8" rx="1" />
                <rect x="124" y="16" width="8" height="8" rx="1" /><rect x="72" y="28" width="8" height="8" rx="1" />
                <rect x="96" y="28" width="8" height="8" rx="1" /><rect x="120" y="28" width="8" height="8" rx="1" />
                <rect x="72" y="40" width="8" height="8" rx="1" /><rect x="84" y="40" width="8" height="8" rx="1" />
                <rect x="108" y="40" width="8" height="8" rx="1" /><rect x="120" y="40" width="8" height="8" rx="1" />
                <rect x="16" y="72" width="8" height="8" rx="1" /><rect x="28" y="72" width="8" height="8" rx="1" />
                <rect x="52" y="72" width="8" height="8" rx="1" /><rect x="96" y="72" width="8" height="8" rx="1" />
                <rect x="120" y="72" width="8" height="8" rx="1" /><rect x="164" y="72" width="8" height="8" rx="1" />
                <rect x="176" y="72" width="8" height="8" rx="1" /><rect x="52" y="84" width="8" height="8" rx="1" />
                <rect x="76" y="84" width="8" height="8" rx="1" /><rect x="132" y="84" width="8" height="8" rx="1" />
                <rect x="152" y="84" width="8" height="8" rx="1" /><rect x="16" y="96" width="8" height="8" rx="1" />
                <rect x="64" y="96" width="8" height="8" rx="1" /><rect x="100" y="96" width="8" height="8" rx="1" />
                <rect x="124" y="96" width="8" height="8" rx="1" /><rect x="172" y="96" width="8" height="8" rx="1" />
                <rect x="28" y="108" width="8" height="8" rx="1" /><rect x="64" y="108" width="8" height="8" rx="1" />
                <rect x="88" y="108" width="8" height="8" rx="1" /><rect x="112" y="108" width="8" height="8" rx="1" />
                <rect x="160" y="108" width="8" height="8" rx="1" /><rect x="16" y="120" width="8" height="8" rx="1" />
                <rect x="52" y="120" width="8" height="8" rx="1" /><rect x="100" y="120" width="8" height="8" rx="1" />
                <rect x="148" y="120" width="8" height="8" rx="1" /><rect x="172" y="120" width="8" height="8" rx="1" />
                <rect x="16" y="140" width="8" height="8" rx="1" /><rect x="28" y="140" width="8" height="8" rx="1" />
                <rect x="52" y="140" width="8" height="8" rx="1" /><rect x="96" y="140" width="8" height="8" rx="1" />
                <rect x="120" y="140" width="8" height="8" rx="1" /><rect x="140" y="140" width="8" height="8" rx="1" />
                <rect x="164" y="140" width="8" height="8" rx="1" /><rect x="176" y="140" width="8" height="8" rx="1" />
                <rect x="16" y="152" width="8" height="8" rx="1" /><rect x="40" y="152" width="8" height="8" rx="1" />
                <rect x="84" y="152" width="8" height="8" rx="1" /><rect x="108" y="152" width="8" height="8" rx="1" />
                <rect x="132" y="152" width="8" height="8" rx="1" /><rect x="156" y="152" width="8" height="8" rx="1" />
                <rect x="52" y="164" width="8" height="8" rx="1" /><rect x="72" y="164" width="8" height="8" rx="1" />
                <rect x="96" y="164" width="8" height="8" rx="1" /><rect x="128" y="164" width="8" height="8" rx="1" />
                <rect x="152" y="164" width="8" height="8" rx="1" /><rect x="16" y="176" width="8" height="8" rx="1" />
                <rect x="64" y="176" width="8" height="8" rx="1" /><rect x="88" y="176" width="8" height="8" rx="1" />
                <rect x="112" y="176" width="8" height="8" rx="1" /><rect x="136" y="176" width="8" height="8" rx="1" />
                <rect x="160" y="176" width="8" height="8" rx="1" />
              </g>
              <rect x="84" y="84" width="32" height="32" rx="6" fill="#dc2626" />
              <text x="100" y="105" text-anchor="middle" fill="#fff" font-size="16" font-weight="700">支</text>
            </svg>
          </div>
          <div class="pay-hint-row">
            <span>请使用{{ payingOrder.paymentMethod === 'wechat' ? '微信' : '支付宝' }}扫描二维码支付</span>
          </div>
        </div>
      </template>
      <template #footer>
        <el-button @click="payDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="payLoading" @click="confirmPay">
          模拟扫码支付
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { orderAPI } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Van } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const orders = ref([])
const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)
const activeStatus = ref('')

const statusMap = {
  pending_payment: { text: '待付款', type: 'warning' },
  pending_shipment: { text: '待发货', type: 'primary' },
  shipped: { text: '待收货', type: 'info' },
  completed: { text: '已完成', type: 'success' },
  cancelled: { text: '已取消', type: 'danger' },
  refunding: { text: '退款中', type: 'warning' },
  refunded: { text: '已退款', type: 'danger' }
}

function statusText(status) {
  return statusMap[status]?.text || status || '未知'
}

function statusTagType(status) {
  return statusMap[status]?.type || 'info'
}

onMounted(() => {
  fetchOrders()
})

async function fetchOrders() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      page_size: pageSize.value
    }
    if (activeStatus.value) params.status = activeStatus.value

    const res = await orderAPI.list(params)
    if (res.code === 200) {
      orders.value = res.data?.list || res.data || []
      total.value = res.data?.total || 0
    }
  } catch {
    orders.value = []
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  currentPage.value = 1
  fetchOrders()
}

function handlePageChange() {
  fetchOrders()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const payDialogVisible = ref(false)
const payingOrder = ref(null)
const payLoading = ref(false)

function handlePay(order) {
  payingOrder.value = order
  payDialogVisible.value = true
}

async function confirmPay() {
  if (!payingOrder.value) return
  payLoading.value = true
  try {
    const res = await orderAPI.pay(payingOrder.value.id)
    if (res.code === 200) {
      payDialogVisible.value = false
      ElMessage.success('支付成功')
      fetchOrders()
    } else {
      ElMessage.error(res.message || '支付失败')
    }
  } catch {
    ElMessage.error('支付失败，请稍后重试')
  } finally {
    payLoading.value = false
  }
}

async function handleCancel(order) {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '取消订单', {
      confirmButtonText: '确定取消',
      cancelButtonText: '返回',
      type: 'warning'
    })
    const res = await orderAPI.cancel(order.id)
    if (res.code === 200) {
      ElMessage.success('订单已取消')
      fetchOrders()
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch {
    // 取消操作
  }
}

async function handleConfirm(order) {
  try {
    await ElMessageBox.confirm('确认已收到商品吗？', '确认收货', {
      confirmButtonText: '确认收货',
      cancelButtonText: '再等等',
      type: 'info'
    })
    const res = await orderAPI.confirm(order.id)
    if (res.code === 200) {
      ElMessage.success('已确认收货')
      fetchOrders()
    } else {
      ElMessage.error(res.message || '确认失败')
    }
  } catch {
    // 取消确认
  }
}

async function handleRefund(order) {
  try {
    await ElMessageBox.confirm('确定要申请退款吗？退款后商品库存将恢复。', '申请退款', {
      confirmButtonText: '确认退款',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await orderAPI.refund(order.id)
    if (res.code === 200) {
      ElMessage.success('退款申请已提交')
      fetchOrders()
    } else {
      ElMessage.error(res.message || '退款失败')
    }
  } catch {
    // 取消操作
  }
}

const logisticsVisible = ref(false)
const logisticsOrder = ref(null)
const logisticsSteps = ref([])

function openLogistics(order) {
  logisticsOrder.value = order
  const created = order.createdAt ? new Date(order.createdAt) : new Date()
  const shipped = order.shippingTime ? new Date(order.shippingTime) : null
  const receiveTime = order.receiveTime ? new Date(order.receiveTime) : null
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
  if (order.status === 'shipped' || order.status === 'completed' || order.status === 'refunding' || order.status === 'refunded') {
    if (shipped) {
      const transit = new Date(shipped.getTime() + 24 * 60 * 60 * 1000)
      steps.push({ text: '快件运输中', time: fmt(transit), done: order.status !== 'shipped' })
    }
  }
  if (order.status === 'completed' || order.status === 'refunding' || order.status === 'refunded') {
    if (receiveTime) {
      steps.push({ text: '包裹已签收', time: fmt(receiveTime), done: true })
    }
  }
  logisticsSteps.value = steps
  logisticsVisible.value = true
}
</script>

<style scoped>
.orders-page {
  max-width: 2200px;
  margin: 0 auto;
  padding: 8px 8px;
  min-height: calc(100vh - 130px);
}

.page-title {
  font-size: 1.3rem;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0 0 14px;
}

.order-list-wrapper {
  min-height: 300px;
}

.order-card {
  background: var(--gradient-card);
  border-radius: 4px;
  margin-bottom: 12px;
  overflow: hidden;
  transition: box-shadow 0.3s;
  border: 1px solid var(--color-border-light);
}

.order-card:hover {
  box-shadow: var(--shadow-md);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  background: #ffffff;
  border-bottom: 1px solid var(--color-border-light);
}

.order-header-left {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: var(--color-text-muted);
}

.order-no {
  color: var(--color-text-primary);
}

.order-items {
  padding: 0 16px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid var(--color-border-light);
  cursor: pointer;
}

.order-item:last-child {
  border-bottom: none;
}

.order-item-info {
  flex: 1;
  min-width: 0;
}

.order-item-name {
  font-size: 16px;
  color: var(--color-text-primary);
  margin: 0 0 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-item-specs {
  font-size: 13px;
  color: var(--color-text-placeholder);
  margin: 0;
}

.order-item-price {
  text-align: right;
  font-size: 16px;
  color: var(--color-text-primary);
  font-weight: 500;
}

.order-item-qty {
  display: block;
  font-size: 13px;
  color: var(--color-text-placeholder);
  font-weight: 400;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-top: 1px solid var(--color-border-light);
  flex-wrap: wrap;
  gap: 10px;
}

.order-total {
  font-size: 15px;
  color: var(--color-text-muted);
}

.order-total strong {
  font-size: 18px;
  font-weight: 800;
  color: #ef4444;
}

.order-actions {
  display: flex;
  gap: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .order-footer {
    flex-direction: column;
    align-items: flex-end;
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
  border-radius: 4px;
  font-size: 13px;
  font-weight: 600;
}

.qr-code-wrapper {
  width: 200px;
  height: 200px;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
  border: 3px solid #dc2626;
}

.qr-code {
  width: 100%;
  height: 100%;
  display: block;
}

.pay-hint-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--color-text-muted);
}

.logistics-header {
  padding: 8px 0;
  font-size: 15px;
}

.logistics-label {
  color: var(--color-text-muted);
}

.logistics-no {
  color: var(--color-primary);
  font-weight: 600;
  letter-spacing: 0.5px;
}
</style>
