<template>
  <div class="users-page">
    <el-card shadow="never" class="page-card">
      <div class="page-toolbar">
        <div class="toolbar-title">用户管理</div>
        <div class="toolbar-actions">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索用户名 / 邮箱 / 手机号"
            clearable
            prefix-icon="Search"
            style="width: 300px"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
          <el-button type="primary" @click="openCreate">
            <el-icon><Plus /></el-icon> 新增用户
          </el-button>
        </div>
      </div>

      <el-table
        :data="tableData"
        v-loading="loading"
        style="width: 100%"
        stripe
        :header-cell-style="{ background: '#ffffff', color: '#475569', fontWeight: 600 }"
      >
        <el-table-column type="index" label="#" width="60" align="center" :index="(idx) => (currentPage - 1) * pageSize + idx + 1" />
        <el-table-column prop="uid" label="UID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" min-width="110" />
        <el-table-column prop="email" label="邮箱" min-width="170" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" width="135" />
        <el-table-column label="角色" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.role === 'admin' ? 'warning' : 'info'" size="small" effect="light">
              {{ row.role === 'admin' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="170" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="(row.status === 'active' || row.status === '1') ? 'success' : 'danger'" size="small" effect="light">
              {{ (row.status === 'active' || row.status === '1') ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openDetail(row)">
              <el-icon><View /></el-icon> 详情
            </el-button>
            <el-button type="success" size="small" link @click="openEdit(row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无用户数据" />
        </template>
      </el-table>

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

    <!-- 新增用户 -->
    <el-dialog v-model="createVisible" title="新增用户" width="480px" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="createForm.username" placeholder="请输入用户名" maxlength="30" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="createForm.password" type="password" placeholder="请输入密码" show-password maxlength="30" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="createForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="createForm.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="createLoading" @click="submitCreate">确定</el-button>
      </template>
    </el-dialog>

    <!-- 编辑用户 -->
    <el-dialog v-model="editVisible" title="编辑用户" width="480px" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editForm.username" placeholder="请输入用户名" maxlength="30" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="editForm.status" active-value="active" inactive-value="disabled" active-text="正常" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 用户详情 -->
    <el-dialog v-model="detailVisible" title="用户详情" width="520px" :close-on-click-modal="false">
      <el-descriptions :column="2" border v-if="detailUser">
        <el-descriptions-item label="UID">{{ detailUser.uid || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ detailUser.username }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ detailUser.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detailUser.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag :type="detailUser.role === 'admin' ? 'warning' : 'info'" size="small">
            {{ detailUser.role === 'admin' ? '管理员' : '普通用户' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="(detailUser.status === 'active' || detailUser.status === '1') ? 'success' : 'danger'" size="small">
            {{ (detailUser.status === 'active' || detailUser.status === '1') ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ detailUser.createdAt || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailUser.updatedAt || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminAPI } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, View } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 新增
const createVisible = ref(false)
const createLoading = ref(false)
const createFormRef = ref(null)
const createForm = reactive({ username: '', password: '', email: '', phone: '' })
const createRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }]
}

// 编辑
const editVisible = ref(false)
const editLoading = ref(false)
const editFormRef = ref(null)
const editForm = reactive({ id: null, username: '', email: '', phone: '', status: 'active' })
const editRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }]
}

// 详情
const detailVisible = ref(false)
const detailUser = ref(null)

function handleSearch() {
  currentPage.value = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const res = await adminAPI.users({
      page: currentPage.value,
      page_size: pageSize.value,
      keyword: searchKeyword.value || undefined,
      sort: 'id',
      order: 'desc'
    })
    if (res.code === 200) {
      tableData.value = res.data?.list || res.data || []
      total.value = res.data?.total || 0
    }
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function openCreate() {
  createForm.username = ''
  createForm.password = ''
  createForm.email = ''
  createForm.phone = ''
  createVisible.value = true
}

async function submitCreate() {
  const valid = await createFormRef.value?.validate().catch(() => false)
  if (!valid) return
  createLoading.value = true
  try {
    const res = await adminAPI.userCreate({ ...createForm })
    if (res.code === 200) {
      ElMessage.success('新增用户成功')
      createVisible.value = false
      loadData()
    }
  } catch {
    // handled by interceptor
  } finally {
    createLoading.value = false
  }
}

function openEdit(row) {
  editForm.id = row.id
  editForm.username = row.username
  editForm.email = row.email || ''
  editForm.phone = row.phone || ''
  editForm.status = (row.status === 'active' || row.status === '1') ? 'active' : 'disabled'
  editVisible.value = true
}

async function submitEdit() {
  const valid = await editFormRef.value?.validate().catch(() => false)
  if (!valid) return
  editLoading.value = true
  try {
    const res = await adminAPI.userUpdate(editForm.id, {
      username: editForm.username,
      email: editForm.email,
      phone: editForm.phone,
      status: editForm.status
    })
    if (res.code === 200) {
      ElMessage.success('更新成功')
      editVisible.value = false
      loadData()
    }
  } catch {
    // handled by interceptor
  } finally {
    editLoading.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户「${row.username}」吗？此操作不可恢复。`,
      '删除确认',
      { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
    )
    const res = await adminAPI.userDelete(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    }
  } catch {
    // cancelled or error
  }
}

async function openDetail(row) {
  try {
    const res = await adminAPI.userDetail(row.id)
    if (res.code === 200) {
      detailUser.value = res.data
      detailVisible.value = true
    }
  } catch {
    // handled by interceptor
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.users-page {
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

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
