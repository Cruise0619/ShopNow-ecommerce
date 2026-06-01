<template>
  <div class="dashboard-page">
    <!-- Stat Cards -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6" v-for="card in statCards" :key="card.label">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-card-inner">
            <div class="stat-icon" :style="{ background: card.bg }">
              <el-icon :size="28" :color="card.color"><component :is="card.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
          </div>
          <div class="stat-trend" v-if="card.trend">
            <span :class="card.trend > 0 ? 'trend-up' : 'trend-down'">
              {{ card.trend > 0 ? '↑' : '↓' }} {{ Math.abs(card.trend) }}%
            </span>
            <span class="trend-label">较昨日</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Charts Row -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header><span class="card-title">近 7 天销售趋势</span></template>
          <div ref="salesChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header><span class="card-title">订单状态分布</span></template>
          <div ref="statusChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Top Products -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header><span class="card-title">热销商品 Top 10</span></template>
          <div ref="productChartRef" class="chart-box chart-box-bar"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { adminAPI } from '@/api'
import { ElMessage } from 'element-plus'

const salesChartRef = ref(null)
const statusChartRef = ref(null)
const productChartRef = ref(null)

let salesChart = null
let statusChart = null
let productChart = null

const statCards = reactive([
  { label: '用户总数', value: '0', icon: 'User', color: '#dc2626', bg: '#fef2f2', trend: 0 },
  { label: '订单总数', value: '0', icon: 'Document', color: '#10b981', bg: '#ecfdf5', trend: 0 },
  { label: '总销售额', value: '¥0.00', icon: 'Money', color: '#ef4444', bg: '#fef2f2', trend: 0 },
  { label: '今日订单', value: '0', icon: 'Clock', color: '#ef4444', bg: '#fef2f2', trend: 0 },
])

function formatMoney(v) {
  if (v == null) return '¥0.00'
  return '¥' + Number(v).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

function initSalesChart(data) {
  if (!salesChartRef.value) return
  if (salesChart) salesChart.dispose()
  salesChart = echarts.init(salesChartRef.value)
  const dates = data?.map(d => d.date) || ['暂无数据']
  const values = data?.map(d => d.amount) || [0]
  salesChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: dates, boundaryGap: false, axisLine: { lineStyle: { color: '#cbd5e1' } } },
    yAxis: { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: '#ffffff' } } },
    series: [{
      data: values,
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: 'rgba(220, 38, 38,0.25)' },
        { offset: 1, color: 'rgba(220, 38, 38,0.02)' }
      ]) },
      itemStyle: { color: '#dc2626' },
      lineStyle: { color: '#dc2626', width: 3 }
    }]
  })
}

function initStatusChart(data) {
  if (!statusChartRef.value) return
  if (statusChart) statusChart.dispose()
  statusChart = echarts.init(statusChartRef.value)
  const items = data?.length ? data : [{ name: '暂无数据', value: 1 }]
  statusChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: '0%', textStyle: { fontSize: 11 } },
    series: [{
      type: 'pie',
      radius: ['45%', '75%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 3 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: items
    }]
  })
}

function initProductChart(data) {
  if (!productChartRef.value) return
  if (productChart) productChart.dispose()
  productChart = echarts.init(productChartRef.value)
  const names = data?.map(d => d.name) || ['暂无数据']
  const values = data?.map(d => d.sales) || [0]
  productChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '8%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: '#ffffff' } } },
    yAxis: { type: 'category', data: names, axisLine: { lineStyle: { color: '#cbd5e1' } }, inverse: true },
    series: [{
      data: values,
      type: 'bar',
      barWidth: 20,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#ef4444' },
          { offset: 1, color: '#dc2626' }
        ]),
        borderRadius: [0, 6, 6, 0]
      }
    }]
  })
}

function handleResize() {
  salesChart?.resize()
  statusChart?.resize()
  productChart?.resize()
}

async function loadDashboard() {
  try {
    const res = await adminAPI.dashboard()
    if (res.code === 200) {
      const d = res.data
      statCards[0].value = String(d.userCount ?? 0)
      statCards[1].value = String(d.orderCount ?? 0)
      statCards[2].value = formatMoney(d.totalSales)
      statCards[3].value = String(d.todayOrders ?? 0)
      // trends not yet provided by backend — show daily comparison
      statCards[0].trend = 0
      statCards[1].trend = 0
      statCards[2].trend = 0
      statCards[3].trend = 0
      // charts
      initSalesChart(d.dailySales || [])
      initStatusChart(d.orderStatus || [])
      initProductChart(d.topProducts || [])
    }
  } catch (e) {
    // handled by interceptor
  }
}

onMounted(() => {
  loadDashboard()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  salesChart?.dispose()
  statusChart?.dispose()
  productChart?.dispose()
})
</script>

<style scoped>
.dashboard-page {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 12px;
  transition: transform 0.2s;
  border: 1px solid var(--color-border-light);
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-card-inner {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 1.6rem;
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 0.85rem;
  color: var(--color-text-placeholder);
  margin-top: 2px;
}

.stat-trend {
  margin-top: 12px;
  font-size: 0.8rem;
}

.trend-up {
  color: var(--color-success);
  font-weight: 600;
}

.trend-down {
  color: var(--color-danger);
  font-weight: 600;
}

.trend-label {
  color: var(--color-text-placeholder);
  margin-left: 4px;
}

.chart-row {
  margin-bottom: 20px;
}

.card-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-text-primary);
}

.chart-box {
  width: 100%;
  height: 320px;
}

.chart-box-bar {
  height: 380px;
}
</style>
