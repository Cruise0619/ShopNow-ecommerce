<template>
  <div class="coupons-page">
    <el-card shadow="never" class="page-card">
      <div class="page-toolbar">
        <div class="toolbar-title">优惠券管理</div>
        <el-button type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon> 新增优惠券
        </el-button>
      </div>

      <el-table
        :data="tableData"
        v-loading="loading"
        style="width: 100%"
        stripe
        :header-cell-style="{ background: '#ffffff', color: '#475569', fontWeight: 600 }"
      >
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column prop="name" label="名称" min-width="160" show-overflow-tooltip />
        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.type === 'fixed' ? 'primary' : 'warning'" size="small" effect="light">
              {{ row.type === 'fixed' ? '满减' : '折扣' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="优惠值" width="110" align="center">
          <template #default="{ row }">
            <span class="coupon-value">
              {{ row.type === 'percent' ? row.value + '%' : '¥' + Number(row.value).toFixed(0) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="最低消费" width="110" align="center">
          <template #default="{ row }">
            {{ row.minAmount ? '¥' + Number(row.minAmount).toFixed(0) : '无限制' }}
          </template>
        </el-table-column>
        <el-table-column label="有效期" width="220">
          <template #default="{ row }">
            <span class="time-range">{{ row.startTime }} 至 {{ row.endTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="使用/发放" width="100" align="center">
          <template #default="{ row }">
            {{ row.used || 0 }} / {{ row.total || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getCouponStatus(row).type" size="small" effect="light">
              {{ getCouponStatus(row).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openDialog(row)">编辑</el-button>
            <el-popconfirm
              title="确定要删除该优惠券吗？"
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
          <el-empty description="暂无优惠券数据" />
        </template>
      </el-table>
    </el-card>

    <!-- Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑优惠券' : '新增优惠券'"
      width="560px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="优惠券名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入优惠券名称" />
        </el-form-item>
        <el-form-item label="优惠类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio value="fixed">满减券</el-radio>
            <el-radio value="percent">折扣券</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item :label="form.type === 'fixed' ? '减免金额' : '折扣(%)'" prop="value">
              <el-input-number
                v-model="form.value"
                :min="0"
                :max="form.type === 'percent' ? 100 : 99999"
                :precision="form.type === 'fixed' ? 2 : 0"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最低消费" prop="min_amount">
              <el-input-number v-model="form.min_amount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="start_time">
              <el-date-picker
                v-model="form.start_time"
                type="datetime"
                placeholder="选择开始时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="end_time">
              <el-date-picker
                v-model="form.end_time"
                type="datetime"
                placeholder="选择结束时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="发放总数" prop="total_count">
          <el-input-number v-model="form.total_count" :min="1" :max="99999" controls-position="right" style="width: 180px" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminAPI } from '@/api'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const isEdit = ref(false)

const form = reactive({
  id: null,
  name: '',
  type: 'fixed',
  value: 0,
  min_amount: 0,
  start_time: '',
  end_time: '',
  total_count: 100
})

const rules = {
  name: [{ required: true, message: '请输入优惠券名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择优惠类型', trigger: 'change' }],
  value: [{ required: true, message: '请输入优惠值', trigger: 'blur' }],
  start_time: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  end_time: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  total_count: [{ required: true, message: '请输入发放总数', trigger: 'blur' }],
  min_amount: [{ required: true, message: '请输入最低消费', trigger: 'blur' }]
}

function getCouponStatus(row) {
  const now = Date.now()
  const start = new Date(row.startTime).getTime()
  const end = new Date(row.endTime).getTime()
  if (row.used >= row.total) return { label: '已领完', type: 'info' }
  if (now < start) return { label: '未开始', type: 'warning' }
  if (now > end) return { label: '已过期', type: 'danger' }
  return { label: '进行中', type: 'success' }
}

async function loadData() {
  loading.value = true
  try {
    const res = await adminAPI.coupons()
    if (res.code === 200) {
      tableData.value = res.data || []
    }
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function openDialog(row = null) {
  if (row) {
    isEdit.value = true
    form.id = row.id
    form.name = row.name || ''
    form.type = row.type || 'fixed'
    form.value = row.value || 0
    form.min_amount = row.minAmount || 0
    form.start_time = row.startTime || ''
    form.end_time = row.endTime || ''
    form.total_count = row.total || 100
  } else {
    isEdit.value = false
    Object.assign(form, {
      id: null, name: '', type: 'fixed', value: 0,
      min_amount: 0, start_time: '', end_time: '', total_count: 100
    })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const data = {
      name: form.name,
      type: form.type,
      value: form.value,
      minAmount: form.min_amount,
      startTime: form.start_time,
      endTime: form.end_time,
      total: form.total_count
    }
    let res
    if (isEdit.value) {
      res = await adminAPI.couponUpdate(form.id, data)
    } else {
      res = await adminAPI.couponCreate(data)
    }
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '优惠券已更新' : '优惠券已创建')
      dialogVisible.value = false
      loadData()
    }
  } catch (e) {
    // handled by interceptor
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(id) {
  try {
    const res = await adminAPI.couponDelete(id)
    if (res.code === 200) {
      ElMessage.success('优惠券已删除')
      loadData()
    }
  } catch (e) {
    // handled by interceptor
  }
}

onMounted(() => { loadData() })
</script>

<style scoped>
.coupons-page {
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
}

.toolbar-title {
  font-size: 1.15rem;
  font-weight: 600;
  color: var(--color-text-primary);
}

.coupon-value {
  font-weight: 700;
  color: var(--color-primary);
}

.time-range {
  font-size: 0.85rem;
  color: var(--color-text-muted);
}
</style>
