<template>
  <div class="announcements-page">
    <el-card shadow="never" class="page-card">
      <div class="page-toolbar">
        <div class="toolbar-title">公告管理</div>
        <el-button type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon> 新增公告
        </el-button>
      </div>

      <el-table
        :data="tableData"
        v-loading="loading"
        style="width: 100%"
        stripe
        :header-cell-style="{ background: '#ffffff', color: '#475569', fontWeight: 600 }"
      >
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column label="内容" min-width="300" show-overflow-tooltip>
          <template #default="{ row }">
            {{ truncateText(row.content, 80) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small" effect="light">
              {{ row.status === 1 ? '发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openDialog(row)">编辑</el-button>
            <el-popconfirm
              title="确定要删除该公告吗？"
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
          <el-empty description="暂无公告数据" />
        </template>
      </el-table>
    </el-card>

    <!-- Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑公告' : '新增公告'"
      width="600px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入公告内容" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="发布" inactive-text="草稿" />
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
  title: '',
  content: '',
  status: 1
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

function truncateText(text, max) {
  if (!text) return ''
  return text.length > max ? text.slice(0, max) + '...' : text
}

async function loadData() {
  loading.value = true
  try {
    const res = await adminAPI.announcements()
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
    form.content = row.content || ''
    form.status = row.status ?? 1
  } else {
    isEdit.value = false
    Object.assign(form, { id: null, title: '', content: '', status: 1 })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const data = { title: form.title, content: form.content, status: form.status }
    let res
    if (isEdit.value) {
      res = await adminAPI.annoUpdate(form.id, data)
    } else {
      res = await adminAPI.annoCreate(data)
    }
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '公告已更新' : '公告已创建')
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
    const res = await adminAPI.annoDelete(id)
    if (res.code === 200) {
      ElMessage.success('公告已删除')
      loadData()
    }
  } catch (e) {
    // handled by interceptor
  }
}

onMounted(() => { loadData() })
</script>

<style scoped>
.announcements-page {
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
