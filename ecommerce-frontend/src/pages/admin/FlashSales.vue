<template>
  <div class="flashsales-page">
    <el-card shadow="never" class="page-card">
      <div class="page-toolbar">
        <div class="toolbar-title">秒杀管理</div>
        <el-button type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon> 新增秒杀
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
        <el-table-column label="商品" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.product?.name || '商品#' + row.productId }}</template>
        </el-table-column>
        <el-table-column label="秒杀价" width="120" align="center">
          <template #default="{ row }">
            <span class="flash-price">¥{{ Number(row.flashPrice).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="库存" width="120" align="center">
          <template #default="{ row }">
            {{ row.sold || 0 }} / {{ row.stock }}
          </template>
        </el-table-column>
        <el-table-column label="活动时间" width="240">
          <template #default="{ row }">
            <span class="time-range">{{ row.startTime }} 至 {{ row.endTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row)" size="small" effect="light">{{ statusLabel(row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openDialog(row)">编辑</el-button>
            <el-popconfirm
              title="确定要删除该秒杀活动吗？"
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
          <el-empty description="暂无秒杀活动" />
        </template>
      </el-table>
    </el-card>

    <!-- Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑秒杀' : '新增秒杀'"
      width="560px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="秒杀商品" prop="product_id">
          <el-select v-model="form.product_id" placeholder="选择商品" filterable style="width: 100%">
            <el-option v-for="p in productOptions" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="秒杀价格" prop="flash_price">
              <el-input-number v-model="form.flash_price" :min="0.01" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="秒杀库存" prop="stock">
              <el-input-number v-model="form.stock" :min="1" :max="99999" controls-position="right" style="width: 100%" />
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
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="active">启用</el-radio>
            <el-radio value="ended">停用</el-radio>
          </el-radio-group>
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
import { adminAPI, productAPI } from '@/api'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref([])
const productOptions = ref([])
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const isEdit = ref(false)

const form = reactive({
  id: null,
  product_id: null,
  flash_price: 0,
  stock: 100,
  start_time: '',
  end_time: '',
  status: 'active'
})

const rules = {
  product_id: [{ required: true, message: '请选择商品', trigger: 'change' }],
  flash_price: [{ required: true, message: '请输入秒杀价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入秒杀库存', trigger: 'blur' }],
  start_time: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  end_time: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

function statusLabel(row) {
  const now = Date.now()
  const start = new Date(row.startTime).getTime()
  const end = new Date(row.endTime).getTime()
  if (row.status === 'ended') return '已结束'
  if (now < start) return '未开始'
  if (now > end) return '已过期'
  if (row.sold >= row.stock) return '已售罄'
  return '进行中'
}

function statusType(row) {
  const now = Date.now()
  const start = new Date(row.startTime).getTime()
  const end = new Date(row.endTime).getTime()
  if (row.status === 'ended' || now > end) return 'danger'
  if (now < start) return 'warning'
  if (row.sold >= row.stock) return 'info'
  return 'success'
}

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/admin/flashsales')
    if (res.code === 200) {
      tableData.value = res.data || []
    }
  } catch (e) {
    // handled
  } finally {
    loading.value = false
  }
}

async function loadProducts() {
  try {
    const res = await productAPI.list({ page_size: 200 })
    if (res.code === 200) {
      const list = res.data?.list || res.data?.records || res.data || []
      productOptions.value = list.map(p => ({ id: p.id, name: p.name }))
    }
  } catch (e) {
    // ignore
  }
}

function openDialog(row = null) {
  if (row) {
    isEdit.value = true
    form.id = row.id
    form.product_id = row.productId
    form.flash_price = row.flashPrice
    form.stock = row.stock
    form.start_time = row.startTime || ''
    form.end_time = row.endTime || ''
    form.status = row.status || 'active'
  } else {
    isEdit.value = false
    Object.assign(form, {
      id: null, product_id: null, flash_price: 0,
      stock: 100, start_time: '', end_time: '', status: 'active'
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
      productId: form.product_id,
      flashPrice: form.flash_price,
      stock: form.stock,
      startTime: form.start_time,
      endTime: form.end_time,
      status: form.status
    }
    let res
    if (isEdit.value) {
      res = await request.put(`/admin/flashsales/${form.id}`, data)
    } else {
      res = await request.post('/admin/flashsales', data)
    }
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '秒杀已更新' : '秒杀已创建')
      dialogVisible.value = false
      loadData()
    }
  } catch (e) {
    // handled
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(id) {
  try {
    const res = await request.delete(`/admin/flashsales/${id}`)
    if (res.code === 200) {
      ElMessage.success('秒杀已删除')
      loadData()
    }
  } catch (e) {
    // handled
  }
}

onMounted(() => { loadProducts(); loadData() })
</script>

<style scoped>
.flashsales-page {
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

.flash-price {
  font-weight: 700;
  color: #ef4444;
}

.time-range {
  font-size: 0.85rem;
  color: var(--color-text-muted);
}
</style>
