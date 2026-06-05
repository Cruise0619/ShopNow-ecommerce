<template>
  <div class="checkout-page" v-loading="loading">
    <h1 class="page-title">确认订单</h1>

    <el-row :gutter="24">
      <!-- 左侧：地址 & 支付 -->
      <el-col :xs="24" :lg="16">
        <!-- 收货地址 -->
        <el-card class="section-card" shadow="never">
          <template #header>
            <div class="section-header">
              <span class="section-title"><el-icon><Location /></el-icon> 收货地址</span>
              <el-button type="primary" text size="small" @click="showAddressDialog = true">
                <el-icon><Plus /></el-icon> 新增地址
              </el-button>
            </div>
          </template>
          <el-radio-group v-model="selectedAddressId" class="address-radio-group" v-if="addresses.length">
            <div
              v-for="addr in addresses"
              :key="addr.id"
              class="address-card"
              :class="{ selected: selectedAddressId === addr.id }"
              @click="selectedAddressId = addr.id"
            >
              <el-radio :value="addr.id" class="address-radio">
                <div class="address-info">
                  <span class="address-receiver">{{ addr.receiver }}</span>
                  <span class="address-phone">{{ addr.phone }}</span>
                  <el-tag v-if="addr.is_default" size="small" type="danger">默认</el-tag>
                </div>
                <p class="address-detail">{{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}</p>
              </el-radio>
            </div>
          </el-radio-group>
          <el-empty v-else description="暂无收货地址，请添加">
            <el-button type="primary" @click="showAddressDialog = true">添加地址</el-button>
          </el-empty>
        </el-card>

        <!-- 支付方式 -->
        <el-card class="section-card" shadow="never">
          <template #header>
            <div class="section-header">
              <span class="section-title"><el-icon><CreditCard /></el-icon> 支付方式</span>
            </div>
          </template>
          <el-radio-group v-model="paymentMethod" class="payment-radio-group">
            <el-radio-button value="alipay">
              <el-icon><Money /></el-icon> 支付宝
            </el-radio-button>
            <el-radio-button value="wechat">
              <el-icon><ChatDotRound /></el-icon> 微信支付
            </el-radio-button>
            <el-radio-button value="bank">
              <el-icon><CreditCard /></el-icon> 银行卡
            </el-radio-button>
          </el-radio-group>
        </el-card>

        <!-- 备注 -->
        <el-card class="section-card" shadow="never">
          <template #header>
            <div class="section-header">
              <span class="section-title"><el-icon><Edit /></el-icon> 订单备注</span>
            </div>
          </template>
          <el-input
            v-model="remark"
            type="textarea"
            :rows="3"
            placeholder="如有特殊要求请在此备注（选填）"
            maxlength="200"
            show-word-limit
          />
        </el-card>
      </el-col>

      <!-- 右侧：订单摘要 -->
      <el-col :xs="24" :lg="8">
        <div class="order-summary-wrapper">
          <el-card class="order-summary-card" shadow="never">
            <template #header>
              <span class="summary-title">订单摘要</span>
            </template>

            <!-- 商品列表 -->
            <div class="summary-items">
              <div v-for="item in cartItems" :key="item.id" class="summary-item">
                <el-image
                  :src="item.product?.coverImage || item.product?.image"
                  fit="cover"
                  style="width: 60px; height: 60px; border-radius: 8px; flex-shrink: 0"
                />
                <div class="summary-item-info">
                  <p class="summary-item-name">{{ item.product?.name }}</p>
                  <p class="summary-item-specs" v-if="item.specs">{{ item.specs }}</p>
                </div>
                <div class="summary-item-price">
                  <span>¥{{ (Number(item.product?.price || 0) * item.quantity).toFixed(2) }}</span>
                  <span class="summary-item-qty">x{{ item.quantity }}</span>
                </div>
              </div>
            </div>

            <!-- 优惠券 -->
            <div class="coupon-row">
              <span class="coupon-label">优惠券：</span>
              <el-select v-model="selectedCouponId" placeholder="选择优惠券" clearable size="small" style="flex:1">
                <el-option
                  v-for="c in coupons"
                  :key="c.id"
                  :label="`${c.name} (减¥${Number(c.discount || c.value).toFixed(2)})`"
                  :value="c.id"
                />
              </el-select>
            </div>

            <!-- 费用明细 -->
            <div class="amount-row">
              <span>商品金额</span>
              <span>¥{{ subtotal.toFixed(2) }}</span>
            </div>
            <div class="amount-row" v-if="discount > 0">
              <span>优惠</span>
              <span class="discount">-¥{{ discount.toFixed(2) }}</span>
            </div>
            <div class="amount-row">
              <span>运费</span>
              <span>免运费</span>
            </div>
            <el-divider />
            <div class="amount-row total-row">
              <span>应付金额</span>
              <span class="final-amount">¥{{ finalAmount.toFixed(2) }}</span>
            </div>

            <el-button
              type="primary"
              size="large"
              class="submit-order-btn"
              :loading="submitting"
              :disabled="!selectedAddressId"
              @click="handleSubmitOrder"
            >
              提交订单
            </el-button>
          </el-card>
        </div>
      </el-col>
    </el-row>

    <!-- 支付二维码弹窗 -->
    <el-dialog
      v-model="showQrDialog"
      :title="qrDialogTitle"
      width="440px"
      center
      :close-on-click-modal="false"
      destroy-on-close
    >
      <div class="qr-dialog-body">
        <div class="qr-pay-method-badge" :class="'qr-badge--' + paymentMethod">
          <span class="qr-badge-icon">{{ qrPayMethodIcon }}</span>
          <span>{{ qrPayMethodLabel }}</span>
        </div>
        <div class="qr-code-wrapper">
          <img :src="qrDataUrl" alt="支付二维码" class="qr-image" v-if="qrDataUrl" />
          <div class="qr-placeholder" v-else>
            <el-icon class="qr-loading-icon"><Loading /></el-icon>
            <span>生成中...</span>
          </div>
        </div>
        <p class="qr-hint">请使用手机扫描二维码完成支付</p>
        <div class="qr-info">
          <span>应付金额</span>
          <span class="qr-amount">&yen;{{ qrPaymentAmount.toFixed(2) }}</span>
        </div>
        <div class="qr-actions">
          <el-button type="primary" size="small" @click="copyPayUrl">复制支付链接</el-button>
          <el-button size="small" @click="goToOrdersFromQr">查看订单</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 新增地址弹窗 -->
    <el-dialog
      v-model="showAddressDialog"
      title="新增收货地址"
      width="520px"
      destroy-on-close
    >
      <el-form ref="addressFormRef" :model="addressForm" :rules="addressRules" label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="收货人" prop="receiver">
              <el-input v-model="addressForm.receiver" placeholder="请输入收货人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号码" prop="phone">
              <el-input v-model="addressForm.phone" placeholder="请输入手机号码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="所在地区" prop="region">
          <el-cascader
            ref="checkoutCascaderRef"
            v-model="addressForm.region"
            :options="regionData"
            :props="{ value: 'value', label: 'label', children: 'children', checkStrictly: false }"
            placeholder="请选择省 / 市 / 区"
            style="width: 100%"
            clearable
            @change="onAddressRegionChange"
          />
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="addressForm.detail" placeholder="街道、门牌号等" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="addressForm.is_default">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddressDialog = false">取消</el-button>
        <el-button type="primary" :loading="savingAddress" @click="handleSaveAddress">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { addrAPI, couponAPI, orderAPI, productAPI } from '@/api'
import { ElMessage } from 'element-plus'
import { Location, Plus, CreditCard, Money, ChatDotRound, Edit } from '@element-plus/icons-vue'
import { toDataURL } from 'qrcode'
import { getPaymentBaseUrl } from '@/utils/payment'
import regionData from '@/data/region'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

const loading = ref(true)
const submitting = ref(false)
const savingAddress = ref(false)

const addresses = ref([])
const selectedAddressId = ref(null)
const coupons = ref([])
const selectedCouponId = ref(null)
const paymentMethod = ref('alipay')
const remark = ref('')

const showAddressDialog = ref(false)
const addressFormRef = ref(null)

const checkoutCascaderRef = ref(null)

// QR code payment dialog
const showQrDialog = ref(false)
const qrDataUrl = ref('')
const qrPayToken = ref('')
const qrPaymentAmount = ref(0)
const qrLoading = ref(false)

// Direct buy support
const isDirectBuy = computed(() => !!route.query.product_id)
const directProduct = ref(null)
const directQuantity = ref(Number(route.query.quantity) || 1)
const directSpecs = ref(decodeURIComponent(route.query.specs || ''))

const addressForm = reactive({
  receiver: '',
  phone: '',
  region: [],
  province: '',
  city: '',
  district: '',
  detail: '',
  is_default: false
})

const addressRules = {
  receiver: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  region: [{ required: true, message: '请选择所在地区', trigger: 'change' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

function onAddressRegionChange(values) {
  if (values && values.length === 3) {
    const checked = checkoutCascaderRef.value?.getCheckedNodes()
    if (checked && checked.length) {
      const labels = checked[0].pathLabels
      addressForm.province = labels[0]
      addressForm.city = labels[1]
      addressForm.district = labels[2]
    }
  } else {
    addressForm.province = ''
    addressForm.city = ''
    addressForm.district = ''
  }
}

const cartIds = computed(() => {
  const ids = route.query.cart_ids
  if (!ids) return []
  return String(ids).split(',').map(Number).filter(Boolean)
})

const cartItems = computed(() => {
  if (isDirectBuy.value && directProduct.value) {
    return [{
      id: 'direct',
      productId: directProduct.value.id,
      product: directProduct.value,
      quantity: directQuantity.value,
      specs: directSpecs.value,
      selected: true
    }]
  }
  return cartStore.items.filter(i => cartIds.value.includes(i.id))
})

const subtotal = computed(() => {
  if (isDirectBuy.value && directProduct.value) {
    return Number(directProduct.value.price || 0) * directQuantity.value
  }
  return cartItems.value.reduce((s, i) => s + Number(i.product?.price || 0) * i.quantity, 0)
})

const discount = computed(() => {
  if (!selectedCouponId.value) return 0
  const coupon = coupons.value.find(c => c.id === selectedCouponId.value)
  return coupon ? Number(coupon.discount || coupon.value || 0) : 0
})

const finalAmount = computed(() => {
  return Math.max(subtotal.value - discount.value, 0)
})

const qrDialogTitle = computed(() => {
  const map = { alipay: '支付宝扫码支付', wechat: '微信扫码支付', bank: '银行卡支付' }
  return map[paymentMethod.value] || '扫码支付'
})

const qrPayMethodIcon = computed(() => {
  const map = { alipay: '💙', wechat: '💚', bank: '💳' }
  return map[paymentMethod.value] || '💰'
})

const qrPayMethodLabel = computed(() => {
  const map = { alipay: '支付宝', wechat: '微信支付', bank: '银行卡' }
  return map[paymentMethod.value] || paymentMethod.value
})

const qrBaseUrl = ref(window.location.origin)

function copyPayUrl() {
  const url = qrBaseUrl.value + '/pay/' + qrPayToken.value
  navigator.clipboard.writeText(url).then(() => {
    ElMessage.success('支付链接已复制')
  }).catch(() => {
    ElMessage.info('链接：' + url)
  })
}

function goToOrdersFromQr() {
  showQrDialog.value = false
  router.push('/orders')
}

onMounted(async () => {
  loading.value = true
  try {
    const tasks = [addrAPI.list(), couponAPI.list()]
    if (!isDirectBuy.value) {
      tasks.unshift(cartStore.fetchCart())
    } else {
      const res = await productAPI.detail(route.query.product_id)
      if (res.code === 200) directProduct.value = res.data
    }
    const results = await Promise.all(tasks)
    const addrRes = isDirectBuy.value ? results[0] : results[1]
    const couponRes = isDirectBuy.value ? results[1] : results[2]
    if (addrRes.code === 200) {
      addresses.value = addrRes.data?.list || addrRes.data || []
      const defaultAddr = addresses.value.find(a => a.is_default)
      if (defaultAddr) selectedAddressId.value = defaultAddr.id
      else if (addresses.value.length) selectedAddressId.value = addresses.value[0].id
    }
    if (couponRes.code === 200) {
      coupons.value = couponRes.data?.list || couponRes.data || []
    }
  } finally {
    loading.value = false
  }
})

async function handleSaveAddress() {
  const valid = await addressFormRef.value.validate().catch(() => false)
  if (!valid) return

  savingAddress.value = true
  try {
    const res = await addrAPI.create(addressForm)
    if (res.code === 200) {
      ElMessage.success('地址添加成功')
      showAddressDialog.value = false
      // Refresh addresses
      const addrRes = await addrAPI.list()
      if (addrRes.code === 200) {
        addresses.value = addrRes.data?.list || addrRes.data || []
        if (res.data?.id) selectedAddressId.value = res.data.id
      }
      // Reset form
      Object.assign(addressForm, { receiver: '', phone: '', region: [], province: '', city: '', district: '', detail: '', is_default: false })
    } else {
      ElMessage.error(res.message || '添加失败')
    }
  } catch {
    ElMessage.error('添加地址失败')
  } finally {
    savingAddress.value = false
  }
}

async function handleSubmitOrder() {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  if (isDirectBuy.value && !directProduct.value) {
    ElMessage.warning('商品信息加载失败，请返回重试')
    return
  }

  submitting.value = true
  try {
    const body = {
      address_id: selectedAddressId.value,
      payment_method: paymentMethod.value,
      remark: remark.value,
      coupon_id: selectedCouponId.value || undefined
    }
    if (isDirectBuy.value) {
      body.product_id = Number(route.query.product_id)
      body.quantity = directQuantity.value
      body.specs = directSpecs.value || undefined
    } else {
      body.cart_ids = cartItems.value.map(item => item.id)
    }
    const res = await orderAPI.create(body)

    if (res.code === 200) {
      const data = res.data
      if (!isDirectBuy.value) {
        await cartStore.removeBatch(cartItems.value.map(i => i.id))
      }
      qrPayToken.value = data.payToken
      qrPaymentAmount.value = Number(data.paymentAmount || 0)
      qrBaseUrl.value = await getPaymentBaseUrl()
      const payUrl = qrBaseUrl.value + '/pay/' + data.payToken
      qrDataUrl.value = await toDataURL(payUrl, { width: 256, margin: 2, color: { dark: '#000', light: '#fff' } })
      showQrDialog.value = true
    } else {
      ElMessage.error(res.message || '下单失败')
    }
  } catch {
    ElMessage.error('下单失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.checkout-page {
  max-width: 1800px;
  margin: 0 auto;
  padding: 14px 14px;
  min-height: 60vh;
}

.page-title {
  font-size: 1.3rem;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0 0 16px;
}

.section-card {
  margin-bottom: 14px;
  border-radius: 12px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  display: flex;
  align-items: center;
  gap: 6px;
}

.address-radio-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.address-card {
  border: 2px solid var(--color-border-light);
  border-radius: 10px;
  padding: 10px 14px;
  cursor: pointer;
  transition: border-color 0.3s;
  background: var(--gradient-card);
}

.address-card:hover,
.address-card.selected {
  border-color: var(--color-primary);
  background: #ffffff;
}

.address-radio {
  width: 100%;
}

.address-radio :deep(.el-radio__label) {
  width: 100%;
}

.address-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.address-receiver {
  font-weight: 600;
  color: var(--color-text-primary);
}

.address-phone {
  color: var(--color-text-muted);
  font-size: 14px;
}

.address-detail {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 13px;
  line-height: 1.5;
  padding-left: 24px;
}

.payment-radio-group {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.order-summary-wrapper {
  position: sticky;
  top: 80px;
}

.order-summary-card {
  border-radius: 12px;
}

.summary-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.summary-items {
  max-height: 320px;
  overflow-y: auto;
  margin-bottom: 12px;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid var(--color-border-light);
}

.summary-item-info {
  flex: 1;
  min-width: 0;
}

.summary-item-name {
  font-size: 13px;
  color: var(--color-text-primary);
  margin: 0 0 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.summary-item-specs {
  font-size: 12px;
  color: var(--color-text-placeholder);
  margin: 0;
}

.summary-item-price {
  text-align: right;
  font-size: 14px;
  font-weight: 700;
  color: #ef4444;
}

.summary-item-qty {
  display: block;
  font-size: 12px;
  color: var(--color-text-placeholder);
  font-weight: 400;
}

.coupon-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.coupon-label {
  font-size: 14px;
  color: var(--color-text-muted);
  white-space: nowrap;
}

.amount-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  font-size: 14px;
  color: var(--color-text-muted);
}

.discount {
  color: var(--color-success);
  font-weight: 500;
}

.total-row {
  font-weight: 600;
  font-size: 15px;
  color: var(--color-text-primary);
}

.final-amount {
  font-size: 1.15rem;
  font-weight: 800;
  color: #ef4444;
}

.submit-order-btn {
  width: 100%;
  height: 42px;
  font-size: 15px;
  border-radius: 10px;
  background: var(--color-primary-gradient);
  border: none;
  margin-top: 12px;
  box-shadow: 0 4px 14px rgba(220, 38, 38, 0.3);
}

.submit-order-btn:hover {
  background: linear-gradient(135deg, #b91c1c, #dc2626);
  box-shadow: 0 6px 20px rgba(220, 38, 38, 0.4);
}

/* QR Dialog */
.qr-dialog-body {
  text-align: center;
  padding: 8px 0;
}

.qr-pay-method-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 16px;
}

.qr-badge--alipay {
  background: #e6f4ff;
  color: #1677ff;
}

.qr-badge--wechat {
  background: #e6f9f0;
  color: #07c160;
}

.qr-badge--bank {
  background: #fffbe6;
  color: #d48806;
}

.qr-badge-icon {
  font-size: 16px;
}

.qr-code-wrapper {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
}

.qr-image {
  display: block;
  width: 220px;
  height: 220px;
}

.qr-placeholder {
  width: 220px;
  height: 220px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
  font-size: 14px;
  gap: 8px;
}

.qr-hint {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 16px;
}

.qr-info {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  color: #6b7280;
  margin-bottom: 20px;
}

.qr-amount {
  font-size: 24px;
  font-weight: 800;
  color: #ef4444;
}

.qr-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}

@media (max-width: 768px) {
  .order-summary-wrapper {
    position: static;
  }
}
</style>
