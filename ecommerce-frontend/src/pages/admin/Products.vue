<template>
  <div class="products-page">
    <el-card shadow="never" class="page-card">
      <!-- Toolbar -->
      <div class="page-toolbar">
        <div class="toolbar-title">商品管理</div>
        <div style="display: flex; gap: 12px;">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索商品名称"
            clearable
            prefix-icon="Search"
            style="width: 220px"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
          <el-select v-model="filterCategory" placeholder="商品分类" clearable style="width: 150px" @change="handleSearch">
            <el-option v-for="cat in categoryOptions" :key="cat.value" :label="cat.label" :value="cat.value" />
          </el-select>
          <el-select v-model="filterStatus" placeholder="商品状态" clearable style="width: 120px" @change="handleSearch">
            <el-option label="上架" value="on" />
            <el-option label="下架" value="off" />
          </el-select>
          <el-button type="primary" @click="openDialog()">
            <el-icon><Plus /></el-icon> 新增商品
          </el-button>
          <el-button type="success" @click="openImportDialog">
            <el-icon><Upload /></el-icon> 导入 Excel
          </el-button>
        </div>
      </div>

      <!-- Table -->
      <el-table
        :data="tableData"
        v-loading="loading"
        style="width: 100%"
        stripe
        :header-cell-style="{ background: '#ffffff', color: '#475569', fontWeight: 600 }"
      >
        <el-table-column type="index" label="#" width="60" align="center" :index="(idx) => (currentPage - 1) * pageSize + idx + 1" />
        <el-table-column label="商品图片" width="90" align="center">
          <template #default="{ row }">
            <el-image
              :src="getImageSrc(row)"
              style="width: 56px; height: 56px; border-radius: 3px;"
              fit="cover"
              lazy
            >
              <template #error>
                <div class="image-slot" style="width:56px;height:56px;background:#ffffff;border-radius:3px;display:flex;align-items:center;justify-content:center;font-size:12px;color:#94a3b8">无图</div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="分类" width="100" align="center">
          <template #default="{ row }">
            {{ row.category?.name || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="价格" width="120" align="center">
          <template #default="{ row }">
            <span class="price">¥{{ Number(row.price).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" align="center" />
        <el-table-column prop="sales" label="销量" width="80" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'on' ? 'success' : 'info'" size="small" effect="light">
              {{ row.status === 'on' ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openDialog(row)">编辑</el-button>
            <el-popconfirm
              title="确定要删除该商品吗？"
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
          <el-empty description="暂无商品数据" />
        </template>
      </el-table>

      <!-- Pagination -->
      <div class="page-pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @current-change="loadData"
          @size-change="loadData"
        />
      </div>
    </el-card>

    <!-- Add/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑商品' : '新增商品'"
      width="700px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入商品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属分类" prop="category_id">
              <el-select v-model="form.category_id" placeholder="请选择分类" style="width: 100%">
                <el-option v-for="cat in categoryOptions" :key="cat.value" :label="cat.label" :value="cat.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="售价" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="原价">
              <el-input-number v-model="form.original_price" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="form.stock" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="商品图片">
          <div class="upload-wrapper">
            <el-upload
              ref="uploadRef"
              v-model:file-list="fileList"
              :auto-upload="false"
              list-type="picture-card"
              accept="image/jpeg,image/png,image/gif,image/webp"
              multiple
              :limit="10"
              :on-change="onFileChange"
              :on-remove="onFileRemove"
              :on-exceed="onExceed"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">
              仅支持 JPG/PNG/GIF/WebP 格式，单张不超过 5MB，最多 10 张
            </div>
          </div>
          <!-- 已有图片预览（编辑时） -->
          <div v-if="isEdit && existingImages.length" class="existing-images">
            <p class="existing-label">当前已保存的图片（存储路径）：</p>
            <div v-for="(img, idx) in existingImages" :key="idx" class="existing-img-item">
              <el-image :src="imgFullUrl(img)" style="width:100px;height:100px;border-radius:3px" fit="cover" />
              <span class="img-path">{{ img }}</span>
              <el-button type="danger" size="small" circle :icon="Delete" @click="removeExistingImage(idx)" />
            </div>
          </div>
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入商品描述" />
        </el-form-item>
        <el-form-item label="商品详情">
          <el-input v-model="form.detail" type="textarea" :rows="4" placeholder="请输入商品详情（支持 HTML）" />
        </el-form-item>
        <el-form-item label="商品标签">
          <el-checkbox v-model="form.is_new" label="新品" />
          <el-checkbox v-model="form.is_hot" label="热销" />
          <el-checkbox v-model="form.is_promotion" label="促销" />
        </el-form-item>
        <el-form-item label="筛选标签">
          <el-checkbox-group v-model="form.tags">
            <el-checkbox label="free_shipping">包邮</el-checkbox>
            <el-checkbox label="free_return">退货包运费</el-checkbox>
            <el-checkbox label="seven_day">7天无理由退货</el-checkbox>
            <el-checkbox label="authentic">正品保证</el-checkbox>
            <el-checkbox label="fast_ship">闪电发货</el-checkbox>
            <el-checkbox label="premium">品质优选</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="上架" inactive-text="下架" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存商品</el-button>
      </template>
    </el-dialog>

    <!-- 导入商品 Dialog -->
    <el-dialog v-model="importDialogVisible" title="导入商品" width="500px" :close-on-click-modal="false">
      <div class="import-steps">
        <p>1. 下载商品导入模板，按模板格式填写商品数据</p>
        <el-button type="primary" link @click="downloadTemplate">
          <el-icon><Download /></el-icon> 下载导入模板
        </el-button>
        <p style="margin-top: 16px">2. 上传填写好的 Excel 文件</p>
        <el-upload
          ref="importUploadRef"
          v-model:file-list="importFileList"
          :auto-upload="false"
          accept=".xlsx,.xls"
          :limit="1"
          :on-exceed="() => ElMessage.warning('每次只能上传一个文件')"
          drag
        >
          <el-icon size="40"><UploadFilled /></el-icon>
          <div style="margin-top: 8px;font-size:14px;color:#94a3b8">拖拽文件到此处，或点击选择</div>
        </el-upload>
      </div>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="importLoading" :disabled="importFileList.length === 0" @click="confirmImport">
          确认导入
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminAPI } from '@/api'
import { ElMessage } from 'element-plus'
import { Plus, Delete, Upload, Download, UploadFilled } from '@element-plus/icons-vue'
import request from '@/utils/request'

const API_BASE = ''
const loading = ref(false)
const tableData = ref([])
const searchKeyword = ref('')
const filterCategory = ref(null)
const filterStatus = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const categoryOptions = ref([])

const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const uploadRef = ref(null)
const isEdit = ref(false)
const fileList = ref([])
const existingImages = ref([])

const form = reactive({
  id: null,
  name: '',
  category_id: null,
  price: 0,
  original_price: 0,
  stock: 0,
  description: '',
  detail: '',
  is_new: false,
  is_hot: false,
  is_promotion: false,
  tags: [],
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  category_id: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入售价', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

function getImageSrc(row) {
  if (row.images && Array.isArray(row.images) && row.images.length) {
    return imgFullUrl(row.images[0])
  }
  if (row.image) return imgFullUrl(row.image)
  return ''
}

function imgFullUrl(path) {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return `${API_BASE}${path.startsWith('/') ? '' : '/'}${path}`
}

function onFileChange(file, fileListNew) {
  fileList.value = fileListNew
}

function onFileRemove(file, fileListNew) {
  fileList.value = fileListNew
}

function onExceed() {
  ElMessage.warning('最多只能上传 10 张图片')
}

function removeExistingImage(idx) {
  existingImages.value.splice(idx, 1)
}

function handleSearch() {
  currentPage.value = 1
  loadData()
}

async function loadCategories() {
  try {
    const res = await adminAPI.categories()
    if (res.code === 200) {
      const result = []
      function walk(list, prefix = '') {
        list.forEach(cat => {
          result.push({ value: cat.id, label: prefix + cat.name })
          if (cat.children?.length) walk(cat.children, prefix + '  ')
        })
      }
      walk(res.data || [])
      categoryOptions.value = result
    }
  } catch (e) { /* */ }
}

async function loadData() {
  loading.value = true
  try {
    const res = await adminAPI.products({
      page: currentPage.value,
      page_size: pageSize.value,
      keyword: searchKeyword.value || undefined,
      category_id: filterCategory.value || undefined,
      status: filterStatus.value !== null && filterStatus.value !== '' ? filterStatus.value : undefined,
      sort: 'id',
      order: 'desc'
    })
    if (res.code === 200) {
      tableData.value = res.data?.list || res.data || []
      total.value = res.data?.total || 0
    }
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function openDialog(row = null) {
  fileList.value = []
  existingImages.value = []
  if (row) {
    isEdit.value = true
    Object.assign(form, {
      id: row.id,
      name: row.name || '',
      category_id: row.category_id || null,
      price: Number(row.price) || 0,
      original_price: Number(row.original_price) || 0,
      stock: Number(row.stock) || 0,
      description: row.description || '',
      detail: row.detail || '',
      is_new: !!row.is_new,
      is_hot: !!row.is_hot,
      is_promotion: !!row.is_promotion,
      tags: Array.isArray(row.tags) ? [...row.tags] : [],
      status: row.status === 'on' ? 1 : 0
    })
    // 回显已有图片
    if (row.images && Array.isArray(row.images)) {
      existingImages.value = [...row.images]
    }
  } else {
    isEdit.value = false
    Object.assign(form, {
      id: null,
      name: '',
      category_id: null,
      price: 0,
      original_price: 0,
      stock: 0,
      description: '',
      detail: '',
      is_new: false,
      is_hot: false,
      is_promotion: false,
      tags: [],
      status: 1
    })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  // 检查是否有图片（新增时必须有，编辑时可以有已保存的）
  if (!isEdit.value && !fileList.value.length) {
    ElMessage.warning('请至少上传一张商品图片')
    return
  }

  submitLoading.value = true
  try {
    const fd = new FormData()
    fd.append('name', form.name)
    if (form.category_id != null) fd.append('category_id', form.category_id)
    fd.append('price', form.price)
    if (form.original_price) fd.append('original_price', form.original_price)
    fd.append('stock', form.stock)
    if (form.description) fd.append('description', form.description)
    if (form.detail) fd.append('detail', form.detail)
    fd.append('is_new', form.is_new ? '1' : '0')
    fd.append('is_hot', form.is_hot ? '1' : '0')
    fd.append('is_promotion', form.is_promotion ? '1' : '0')
    fd.append('tags', JSON.stringify(form.tags || []))
    fd.append('status', form.status ? 'on' : 'off')
    // 追加新上传的图片文件
    let fileCount = 0
    fileList.value.forEach(file => {
      if (file.raw && file.raw instanceof File) {
        fd.append('images', file.raw)
        fileCount++
      }
    })
    // 编辑时：始终发送 keep_images（包含保留的已有图片）
    if (isEdit.value) {
      fd.append('keep_images', JSON.stringify(existingImages.value))
    }

    let res
    if (isEdit.value) {
      res = await adminAPI.productUpdate(form.id, fd)
    } else {
      res = await adminAPI.productCreate(fd)
    }
    if (res.code === 200) {
      ElMessage.success({
        message: isEdit.value ? '商品已更新' : '商品已创建',
        duration: 3000
      })
      // 提示存储路径
      if (res.data?.images?.length) {
        const paths = res.data.images.map(p => `  ${p}`).join('\n')
        ElMessage({
          type: 'success',
          message: `图片已保存到：\n${paths}`,
          duration: 5000
        })
      }
      dialogVisible.value = false
      loadData()
    }
  } catch (e) {
    if (e.code === 'ECONNABORTED') {
      ElMessage.error('上传超时，请压缩图片或减少图片数量后重试')
    } else if (!e.response) {
      ElMessage.error('无法连接服务器，请确认后端已启动（端口8080）')
    }
    // other errors handled by interceptor
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(id) {
  try {
    const res = await adminAPI.productDelete(id)
    if (res.code === 200) {
      ElMessage.success('商品已删除')
      loadData()
    }
  } catch (e) {
    // handled by interceptor
  }
}

// ---- Import ----
const importDialogVisible = ref(false)
const importFileList = ref([])
const importLoading = ref(false)
const importUploadRef = ref(null)

function openImportDialog() {
  importFileList.value = []
  importDialogVisible.value = true
}

async function downloadTemplate() {
  try {
    const response = await request.get('/admin/products/template', { responseType: 'blob' })
    const url = window.URL.createObjectURL(new Blob([response]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'product_template.xlsx')
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
    ElMessage.success('模板下载成功')
  } catch {
    ElMessage.error('模板下载失败')
  }
}

async function confirmImport() {
  if (importFileList.value.length === 0) {
    ElMessage.warning('请选择文件')
    return
  }
  importLoading.value = true
  try {
    const fd = new FormData()
    const file = importFileList.value[0].raw
    fd.append('file', file)
    const res = await request.post('/admin/products/import', fd)
    if (res.code === 200) {
      const r = res.data
      ElMessage.success(`导入完成：成功 ${r.success} 条，失败 ${r.fail} 条`)
      importDialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.message || '导入失败')
    }
  } catch {
    ElMessage.error('导入失败，请检查文件格式')
  } finally {
    importLoading.value = false
  }
}

onMounted(() => {
  loadCategories()
  loadData()
})
</script>

<style scoped>
.products-page {
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
  flex-wrap: wrap;
  gap: 12px;
}

.toolbar-title {
  font-size: 1.15rem;
  font-weight: 600;
  color: var(--color-text-primary);
}

.price {
  color: var(--color-danger);
  font-weight: 600;
}

.page-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.upload-wrapper {
  width: 100%;
}

.upload-tip {
  font-size: 12px;
  color: var(--color-text-placeholder);
  margin-top: 6px;
}

.existing-images {
  margin-top: 16px;
  padding: 12px;
  background: var(--color-border-light);
  border-radius: 3px;
}

.existing-label {
  font-size: 13px;
  color: var(--color-text-body);
  margin: 0 0 10px;
  font-weight: 500;
}

.existing-img-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.img-path {
  font-size: 12px;
  color: var(--color-primary);
  font-family: monospace;
  word-break: break-all;
}

.import-steps p {
  margin: 6px 0;
  font-size: 14px;
  color: var(--color-text-body);
}
</style>
