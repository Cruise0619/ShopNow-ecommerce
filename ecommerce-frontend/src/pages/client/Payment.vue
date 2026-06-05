<template>
  <div class="payment-page">
    <!-- Loading -->
    <div class="payment-state" v-if="state === 'loading'">
      <div class="payment-spinner"></div>
      <p class="payment-hint">加载订单信息...</p>
    </div>

    <!-- Error -->
    <div class="payment-state" v-else-if="state === 'error'">
      <div class="payment-icon error-icon">&#10007;</div>
      <h2 class="payment-title">{{ errorMsg }}</h2>
      <p class="payment-desc">请返回商城重新下单</p>
      <button class="pay-btn pay-btn-secondary" @click="goHome">返回首页</button>
    </div>

    <!-- Order Info -->
    <div class="payment-container" v-else-if="state === 'info' || state === 'paying'">
      <div class="pay-header" :class="'pay-header--' + order.paymentMethod">
        <div class="pay-method-icon">{{ payMethodIcon }}</div>
        <div class="pay-brand">{{ payMethodLabel }}</div>
        <div class="pay-order-no">订单号：{{ order.orderNo }}</div>
      </div>

      <div class="pay-body">
        <div class="pay-items">
          <div class="pay-item" v-for="(item, idx) in order.items" :key="idx">
            <img
              :src="getItemImage(item)"
              class="pay-item-img"
              @error="e => e.target.src = '/placeholder.png'"
            />
            <div class="pay-item-info">
              <p class="pay-item-name">{{ item.name }}</p>
              <p class="pay-item-specs" v-if="item.specs">{{ item.specs }}</p>
            </div>
            <div class="pay-item-right">
              <span class="pay-item-price">&yen;{{ Number(item.price || 0).toFixed(2) }}</span>
              <span class="pay-item-qty">x{{ item.quantity }}</span>
            </div>
          </div>
        </div>

        <div class="pay-amounts">
          <div class="pay-amount-row">
            <span>商品小计</span>
            <span>&yen;{{ Number(order.totalAmount || 0).toFixed(2) }}</span>
          </div>
          <div class="pay-amount-row" v-if="Number(order.discountAmount || 0) > 0">
            <span>优惠</span>
            <span class="pay-discount">-&yen;{{ Number(order.discountAmount || 0).toFixed(2) }}</span>
          </div>
          <div class="pay-amount-row pay-amount-total">
            <span>应付金额</span>
            <span class="pay-final">&yen;{{ Number(order.paymentAmount || 0).toFixed(2) }}</span>
          </div>
        </div>

        <div class="pay-method">
          <span class="pay-method-label">支付方式</span>
          <span class="pay-method-value">{{ payMethodLabel }}</span>
        </div>
      </div>

      <div class="pay-footer">
        <button
          class="pay-btn pay-btn-primary"
          :class="'pay-btn--' + order.paymentMethod"
          :disabled="state === 'paying'"
          @click="handlePay"
        >
          <span v-if="state === 'paying'" class="pay-btn-spinner"></span>
          {{ state === 'paying' ? '支付中...' : `确认支付 ¥${Number(order.paymentAmount || 0).toFixed(2)}` }}
        </button>
      </div>
    </div>

    <!-- Success -->
    <div class="payment-state" v-else-if="state === 'success'">
      <div class="payment-icon success-icon">&#10003;</div>
      <h2 class="payment-title">支付成功</h2>
      <p class="payment-desc">订单 {{ order.orderNo }} 已支付</p>
      <button class="pay-btn pay-btn-primary" @click="goOrders">查看订单</button>
      <button class="pay-btn pay-btn-text" @click="goHome">返回首页</button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { publicAPI } from '@/api'

const route = useRoute()
const router = useRouter()

const state = ref('loading')
const errorMsg = ref('')

const order = reactive({
  orderNo: '',
  totalAmount: 0,
  discountAmount: 0,
  paymentAmount: 0,
  paymentMethod: 'alipay',
  items: [],
})

const payMethodLabel = computed(() => {
  const map = { alipay: '支付宝', wechat: '微信支付', bank: '银行卡' }
  return map[order.paymentMethod] || order.paymentMethod
})

const payMethodIcon = computed(() => {
  const map = { alipay: '💙', wechat: '💚', bank: '💳' }
  return map[order.paymentMethod] || '💰'
})

function getItemImage(item) {
  if (!item.images) return ''
  if (Array.isArray(item.images)) return item.images[0] || ''
  if (typeof item.images === 'string') {
    try {
      const arr = JSON.parse(item.images)
      return arr[0] || ''
    } catch {
      return item.images
    }
  }
  return ''
}

async function loadOrder() {
  try {
    const res = await publicAPI.getPaymentInfo(route.params.payToken)
    if (res.code === 200 && res.data) {
      Object.assign(order, res.data)
      state.value = 'info'
    } else {
      state.value = 'error'
      errorMsg.value = res.message || '订单不存在'
    }
  } catch {
    state.value = 'error'
    errorMsg.value = '网络异常，请重试'
  }
}

async function handlePay() {
  state.value = 'paying'
  try {
    const res = await publicAPI.executePay(route.params.payToken)
    if (res.code === 200) {
      state.value = 'success'
    } else {
      state.value = 'info'
      alert(res.message || '支付失败')
    }
  } catch {
    state.value = 'info'
    alert('支付失败，请重试')
  }
}

function goHome() {
  router.push('/')
}

function goOrders() {
  const token = localStorage.getItem('token')
  if (token) {
    router.push('/orders')
  } else {
    router.push('/login')
  }
}

onMounted(loadOrder)
</script>

<style scoped>
.payment-page {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* State views */
.payment-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px 16px;
  text-align: center;
}

.payment-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #e5e7eb;
  border-top-color: #dc2626;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.payment-hint {
  color: #9ca3af;
  font-size: 14px;
  margin: 0;
}

.payment-icon {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  margin-bottom: 16px;
}

.error-icon {
  background: #fee2e2;
  color: #ef4444;
}

.success-icon {
  background: #d1fae5;
  color: #10b981;
}

.payment-title {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 8px;
}

.payment-desc {
  font-size: 14px;
  color: #9ca3af;
  margin: 0 0 24px;
}

/* Container layout */
.payment-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  max-width: 480px;
  width: 100%;
  margin: 0 auto;
}

.pay-header {
  background: #fff;
  padding: 14px 16px;
  text-align: center;
  border-bottom: 1px solid #f3f4f6;
}

.pay-brand {
  font-size: 22px;
  font-weight: 800;
  color: #dc2626;
  margin-bottom: 4px;
}

.pay-order-no {
  font-size: 12px;
  color: #9ca3af;
}

.pay-body {
  flex: 1;
  background: #fff;
  padding: 16px;
  overflow-y: auto;
}

.pay-items {
  margin-bottom: 16px;
}

.pay-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
}

.pay-item-img {
  width: 64px;
  height: 64px;
  border-radius: 3px;
  object-fit: cover;
  background: #f3f4f6;
  flex-shrink: 0;
}

.pay-item-info {
  flex: 1;
  min-width: 0;
}

.pay-item-name {
  font-size: 15px;
  font-weight: 500;
  color: #1f2937;
  margin: 0 0 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pay-item-specs {
  font-size: 12px;
  color: #9ca3af;
  margin: 0;
}

.pay-item-right {
  text-align: right;
  flex-shrink: 0;
}

.pay-item-price {
  display: block;
  font-size: 15px;
  font-weight: 700;
  color: #1f2937;
}

.pay-item-qty {
  font-size: 12px;
  color: #9ca3af;
}

/* Amounts */
.pay-amounts {
  background: #f9fafb;
  border-radius: 4px;
  padding: 14px 16px;
  margin-bottom: 16px;
}

.pay-amount-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  font-size: 14px;
  color: #6b7280;
}

.pay-discount {
  color: #10b981;
  font-weight: 500;
}

.pay-amount-total {
  border-top: 1px solid #e5e7eb;
  margin-top: 6px;
  padding-top: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.pay-final {
  font-size: 22px;
  font-weight: 800;
  color: #ef4444;
}

/* Payment method */
.pay-method {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
}

.pay-method-label {
  font-size: 14px;
  color: #6b7280;
}

.pay-method-value {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}

/* Footer */
.pay-footer {
  background: #fff;
  padding: 16px;
  border-top: 1px solid #f3f4f6;
}

.pay-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 50px;
  border: none;
  border-radius: 4px;
  font-size: 17px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.pay-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.pay-btn-primary {
  background: linear-gradient(135deg, #dc2626, #b91c1c);
  color: #fff;
  box-shadow: 0 4px 14px rgba(220, 38, 38, 0.35);
}

.pay-btn-primary.pay-btn--alipay {
  background: linear-gradient(135deg, #1677ff, #0958d9);
  box-shadow: 0 4px 14px rgba(22, 119, 255, 0.35);
}

.pay-btn-primary.pay-btn--wechat {
  background: linear-gradient(135deg, #07c160, #06ad56);
  box-shadow: 0 4px 14px rgba(7, 193, 96, 0.35);
}

.pay-btn-primary.pay-btn--bank {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  box-shadow: 0 4px 14px rgba(245, 158, 11, 0.35);
}

.pay-btn-primary:not(:disabled):active {
  transform: scale(0.98);
}

/* Payment method header colors */
.pay-header--alipay {
  background: linear-gradient(160deg, #1677ff, #0958d9);
  color: #fff;
}

.pay-header--wechat {
  background: linear-gradient(160deg, #07c160, #06ad56);
  color: #fff;
}

.pay-header--bank {
  background: linear-gradient(160deg, #f59e0b, #d97706);
  color: #fff;
}

.pay-header--alipay .pay-brand,
.pay-header--wechat .pay-brand,
.pay-header--bank .pay-brand {
  color: #fff;
}

.pay-header--alipay .pay-order-no,
.pay-header--wechat .pay-order-no,
.pay-header--bank .pay-order-no {
  color: rgba(255,255,255,0.7);
}

.pay-method-icon {
  font-size: 36px;
  margin-bottom: 4px;
}

.pay-btn-secondary {
  background: #fff;
  color: #6b7280;
  border: 1px solid #e5e7eb;
  max-width: 240px;
}

.pay-btn-text {
  background: transparent;
  color: #9ca3af;
  font-weight: 400;
  font-size: 14px;
  height: auto;
  margin-top: 8px;
  max-width: 240px;
}

.pay-btn-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
  margin-right: 8px;
}

@media (min-width: 480px) {
  .payment-page {
    background: #e5e7eb;
    padding: 40px 0;
  }
  .payment-container {
    border-radius: 4px;
    overflow: hidden;
    box-shadow: 0 8px 30px rgba(0,0,0,0.12);
    max-height: calc(100vh - 80px);
  }
}
</style>
