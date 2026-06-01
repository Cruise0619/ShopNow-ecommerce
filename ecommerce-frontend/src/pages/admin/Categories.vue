<template>
  <div class="categories-page">
    <el-card shadow="never" class="page-card">
      <div class="page-toolbar">
        <div class="toolbar-title">分类管理</div>
        <el-button type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon> 新增分类
        </el-button>
      </div>

      <el-table
        :data="tableData"
        v-loading="loading"
        row-key="id"
        style="width: 100%"
        stripe
        default-expand-all
        :header-cell-style="{ background: '#ffffff', color: '#475569', fontWeight: 600 }"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="分类名称" min-width="180" />
        <el-table-column label="父级分类" min-width="150">
          <template #default="{ row }">
            {{ getParentName(row.parentId) || '顶级分类' }}
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column prop="productCount" label="商品数" width="80" align="center" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openDialog(row)">编辑</el-button>
            <el-popconfirm
              title="确定要删除该分类吗？"
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
          <el-empty description="暂无分类数据" />
        </template>
      </el-table>
    </el-card>

    <!-- Add/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑分类' : '新增分类'"
      width="520px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="父级分类" prop="parentId">
          <el-select v-model="form.parentId" placeholder="请选择（留空为顶级）" clearable style="width: 100%">
            <el-option
              v-for="cat in flatCategories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
              :disabled="cat.id === form.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" controls-position="right" style="width: 160px" />
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
import { ref, reactive, computed, onMounted } from 'vue'
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
  parentId: null,
  sortOrder: 0
})

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

// Build id→name lookup from the tree
const categoryNameMap = computed(() => {
  const map = {}
  function walk(list) {
    list.forEach(item => {
      map[item.id] = item.name
      if (item.children && item.children.length) walk(item.children)
    })
  }
  walk(tableData.value)
  return map
})

function getParentName(parentId) {
  if (!parentId) return null
  return categoryNameMap.value[parentId] || null
}

// Flatten the tree for parent select
const flatCategories = computed(() => {
  const result = []
  function walk(list, prefix = '') {
    list.forEach(item => {
      result.push({ id: item.id, name: prefix + item.name })
      if (item.children && item.children.length) {
        walk(item.children, prefix + '├─ ')
      }
    })
  }
  walk(tableData.value)
  return result
})

async function loadData() {
  loading.value = true
  try {
    const res = await adminAPI.categories()
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
    form.name = row.name
    form.parentId = row.parentId || null
    form.sortOrder = row.sortOrder || 0
  } else {
    isEdit.value = false
    form.id = null
    form.name = ''
    form.parentId = null
    form.sortOrder = 0
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
      parentId: form.parentId || null,
      sortOrder: form.sortOrder
    }
    let res
    if (isEdit.value) {
      res = await adminAPI.categoryUpdate(form.id, data)
    } else {
      res = await adminAPI.categoryCreate(data)
    }
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '分类已更新' : '分类已创建')
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
    const res = await adminAPI.categoryDelete(id)
    if (res.code === 200) {
      ElMessage.success('分类已删除')
      loadData()
    }
  } catch (e) {
    // handled by interceptor
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.categories-page {
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
</style>
