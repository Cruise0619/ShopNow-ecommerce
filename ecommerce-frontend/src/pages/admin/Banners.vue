<template>
  <div class="banners-page">
    <el-card shadow="never" class="page-card">
      <div class="page-toolbar">
        <div class="toolbar-title">轮播图管理</div>
        <el-button type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon> 新增轮播
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
        <el-table-column prop="title" label="标题" min-width="150" />
        <el-table-column label="图片" width="140" align="center">
          <template #default="{ row }">
            <el-image
              :src="row.imageUrl"
              style="width: 100px; height: 56px; border-radius: 3px;"
              fit="cover"
              lazy
              :preview-src-list="[row.imageUrl]"
            />
          </template>
        </el-table-column>
        <el-table-column prop="linkUrl" label="链接" min-width="200" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'on' ? 'success' : 'info'" size="small" effect="light">
              {{ row.status === 'on' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openDialog(row)">编辑</el-button>
            <el-popconfirm
              title="确定要删除该轮播图吗？"
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
          <el-empty description="暂无轮播图数据" />
        </template>
      </el-table>
    </el-card>

    <!-- Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑轮播' : '新增轮播'"
      width="520px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入轮播标题" />
        </el-form-item>
        <el-form-item label="图片" prop="image_url">
          <el-upload
            class="banner-upload"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            accept="image/*"
          >
            <template v-if="form.image_url">
              <el-image :src="form.image_url" style="width:200px;height:100px;border-radius:3px" fit="cover" />
              <div class="upload-overlay">
                <el-icon><Edit /></el-icon>
                <span>点击更换图片</span>
              </div>
            </template>
            <template v-else>
              <el-icon class="upload-icon"><Plus /></el-icon>
              <div class="upload-text">点击上传图片</div>
              <div class="upload-hint">支持 jpg/png 格式，建议尺寸 1200×480</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="关联商品">
          <el-select v-model="form.product_id" placeholder="选择跳转商品（可选）" clearable filterable style="width: 100%">
            <el-option v-for="p in productOptions" :key="p.id" :label="p.name + ' (¥' + Number(p.price).toFixed(2) + ')'" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="链接地址">
          <el-input v-model="form.link_url" placeholder="自定义链接（优先级低于关联商品）" />
        </el-form-item>
        <el-form-item label="排序" prop="sort_order">
          <el-input-number v-model="form.sort_order" :min="0" :max="999" controls-position="right" style="width: 160px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" active-value="on" inactive-value="off" active-text="启用" inactive-text="禁用" />
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
const uploadUrl = '/api/admin/banners/upload-image'
const uploadHeaders = { Authorization: `Bearer ${localStorage.getItem('token') || ''}` }

const productOptions = ref([])

const form = reactive({
  id: null,
  title: '',
  image_url: '',
  link_url: '',
  product_id: null,
  sort_order: 0,
  status: 'on'
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  image_url: [{ required: true, message: '请上传图片', trigger: 'change' }],
  sort_order: [{ required: true, message: '请输入排序', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await adminAPI.banners()
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
    form.title = row.title || ''
    form.image_url = row.imageUrl || ''
    form.link_url = row.linkUrl || ''
    form.product_id = row.productId || null
    form.sort_order = row.sortOrder || 0
    form.status = row.status ?? 'on'
  } else {
    isEdit.value = false
    Object.assign(form, { id: null, title: '', image_url: '', link_url: '', product_id: null, sort_order: 0, status: 'on' })
  }
  dialogVisible.value = true
}

function beforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB！')
    return false
  }
  return true
}

function handleUploadSuccess(response) {
  if (response.code === 200 && response.data) {
    form.image_url = response.data.url
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.error || '上传失败')
  }
}

function handleUploadError() {
  ElMessage.error('图片上传失败，请重试')
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const data = {
      title: form.title,
      imageUrl: form.image_url,
      linkUrl: form.link_url,
      productId: form.product_id || null,
      sortOrder: form.sort_order,
      status: form.status
    }
    let res
    if (isEdit.value) {
      res = await adminAPI.bannerUpdate(form.id, data)
    } else {
      res = await adminAPI.bannerCreate(data)
    }
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '轮播已更新' : '轮播已创建')
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
    const res = await adminAPI.bannerDelete(id)
    if (res.code === 200) {
      ElMessage.success('轮播已删除')
      loadData()
    }
  } catch (e) {
    // handled by interceptor
  }
}

async function loadProducts() {
  try {
    const res = await adminAPI.products({ page: 1, page_size: 9999 })
    if (res.code === 200) {
      productOptions.value = res.data?.list || res.data || []
    }
  } catch { /* ignore */ }
}

onMounted(() => { loadData(); loadProducts() })
</script>

<style scoped>
.banners-page {
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

.banner-upload {
  width: 100%;
}

.banner-upload :deep(.el-upload) {
  border: 2px dashed var(--color-border);
  border-radius: 4px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 100%;
  min-height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: border-color 0.3s, background 0.3s;
}

.banner-upload :deep(.el-upload:hover) {
  border-color: var(--color-primary, #dc2626);
  background: rgba(220, 38, 38, 0.04);
}

.upload-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  font-size: 0.85rem;
  opacity: 0;
  transition: opacity 0.3s;
  border-radius: 3px;
}

.banner-upload :deep(.el-upload):hover .upload-overlay {
  opacity: 1;
}

.upload-icon {
  font-size: 1.8rem;
  color: var(--color-text-placeholder);
}

.upload-text {
  font-size: 0.9rem;
  color: var(--color-text-secondary);
  margin-top: 4px;
}

.upload-hint {
  font-size: 0.75rem;
  color: var(--color-text-placeholder);
  margin-top: 4px;
}
</style>
