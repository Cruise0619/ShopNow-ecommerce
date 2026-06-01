<template>
  <div class="addresses-page">
    <div class="page-header">
      <h1 class="page-title">收货地址</h1>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon> 新增地址
      </el-button>
    </div>

    <div v-loading="loading" class="address-list">
      <template v-if="addresses.length">
        <div v-for="addr in addresses" :key="addr.id" class="address-card">
          <div class="address-main">
            <div class="address-header">
              <span class="address-receiver">{{ addr.receiver }}</span>
              <span class="address-phone">{{ addr.phone }}</span>
              <el-tag v-if="addr.is_default" type="danger" size="small">默认</el-tag>
            </div>
            <p class="address-full">
              {{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}
            </p>
          </div>
          <div class="address-actions">
            <el-button text type="primary" size="small" @click="openEditDialog(addr)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button
              text
              type="danger"
              size="small"
              :loading="deletingId === addr.id"
              @click="handleDelete(addr)"
            >
              <el-icon><Delete /></el-icon> 删除
            </el-button>
            <el-button
              v-if="!addr.is_default"
              text
              type="warning"
              size="small"
              :loading="settingDefaultId === addr.id"
              @click="handleSetDefault(addr)"
            >
              设为默认
            </el-button>
          </div>
        </div>
      </template>
      <el-empty v-else description="暂无收货地址">
        <el-button type="primary" @click="openAddDialog">添加新地址</el-button>
      </el-empty>
    </div>

    <!-- 新增/编辑地址弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑收货地址' : '新增收货地址'"
      width="520px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="收货人" prop="receiver">
              <el-input v-model="form.receiver" placeholder="请输入收货人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号码" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="所在地区" prop="region">
          <el-cascader
            ref="cascaderRef"
            v-model="form.region"
            :options="regionData"
            :props="{ value: 'value', label: 'label', children: 'children', checkStrictly: false }"
            placeholder="请选择省 / 市 / 区"
            style="width: 100%"
            clearable
            @change="onRegionChange"
          />
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="form.detail" placeholder="街道、门牌号、楼层等" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="form.is_default">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">
          {{ isEditing ? '保存修改' : '添加地址' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { addrAPI } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import regionData from '@/data/region'

const loading = ref(true)
const addresses = ref([])
const dialogVisible = ref(false)
const isEditing = ref(false)
const editingId = ref(null)
const saving = ref(false)
const deletingId = ref(null)
const settingDefaultId = ref(null)

const formRef = ref(null)
const cascaderRef = ref(null)

const form = reactive({
  receiver: '',
  phone: '',
  region: [],
  province: '',
  city: '',
  district: '',
  detail: '',
  is_default: false
})

const rules = {
  receiver: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  region: [{ required: true, message: '请选择所在地区', trigger: 'change' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

onMounted(() => {
  fetchAddresses()
})

async function fetchAddresses() {
  loading.value = true
  try {
    const res = await addrAPI.list()
    if (res.code === 200) {
      addresses.value = res.data?.list || res.data || []
    }
  } catch {
    addresses.value = []
  } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.assign(form, {
    receiver: '',
    phone: '',
    region: [],
    province: '',
    city: '',
    district: '',
    detail: '',
    is_default: false
  })
}

function onRegionChange(values) {
  if (values && values.length === 3) {
    const checked = cascaderRef.value?.getCheckedNodes()
    if (checked && checked.length) {
      const labels = checked[0].pathLabels
      form.province = labels[0]
      form.city = labels[1]
      form.district = labels[2]
    }
  } else {
    form.province = ''
    form.city = ''
    form.district = ''
  }
}

function findRegionValues(province, city, district) {
  for (const p of regionData) {
    if (p.label === province) {
      for (const c of p.children) {
        if (c.label === city) {
          for (const d of c.children) {
            if (d.label === district) {
              return [p.value, c.value, d.value]
            }
          }
        }
      }
    }
  }
  return []
}

function openAddDialog() {
  isEditing.value = false
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

async function openEditDialog(addr) {
  isEditing.value = true
  editingId.value = addr.id
  const regionValues = findRegionValues(addr.province, addr.city, addr.district)
  Object.assign(form, {
    receiver: addr.receiver || '',
    phone: addr.phone || '',
    region: regionValues,
    province: addr.province || '',
    city: addr.city || '',
    district: addr.district || '',
    detail: addr.detail || '',
    is_default: addr.is_default || false
  })
  dialogVisible.value = true
  await nextTick()
  formRef.value?.clearValidate()
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (!form.province || !form.city || !form.district) {
    ElMessage.warning('请完整选择省/市/区')
    return
  }

  saving.value = true
  try {
    let res
    if (isEditing.value) {
      res = await addrAPI.update(editingId.value, form)
    } else {
      res = await addrAPI.create(form)
    }

    if (res.code === 200) {
      ElMessage.success(isEditing.value ? '地址更新成功' : '地址添加成功')
      dialogVisible.value = false
      await fetchAddresses()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

async function handleDelete(addr) {
  try {
    await ElMessageBox.confirm('确定要删除该地址吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    deletingId.value = addr.id
    const res = await addrAPI.remove(addr.id)
    if (res.code === 200) {
      ElMessage.success('地址已删除')
      addresses.value = addresses.value.filter(a => a.id !== addr.id)
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch {
    // 取消
  } finally {
    deletingId.value = null
  }
}

async function handleSetDefault(addr) {
  settingDefaultId.value = addr.id
  try {
    const res = await addrAPI.setDefault(addr.id)
    if (res.code === 200) {
      ElMessage.success('已设为默认地址')
      addresses.value.forEach(a => {
        a.is_default = a.id === addr.id
      })
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  } finally {
    settingDefaultId.value = null
  }
}
</script>

<style scoped>
.addresses-page {
  max-width: 800px;
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

.address-list {
  min-height: 160px;
}

.address-card {
  background: var(--gradient-card);
  border: 1px solid var(--color-border-light);
  border-radius: 12px;
  padding: 14px;
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  transition: box-shadow 0.3s;
  flex-wrap: wrap;
  gap: 10px;
}

.address-card:hover {
  box-shadow: var(--shadow-md);
  border-color: var(--color-border);
}

.address-main {
  flex: 1;
  min-width: 200px;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.address-receiver {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.address-phone {
  font-size: 14px;
  color: var(--color-text-muted);
}

.address-full {
  margin: 0;
  font-size: 14px;
  color: var(--color-text-body);
  line-height: 1.5;
}

.address-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}
</style>
