<template>
  <div class="admin-profile-page">
    <el-row :gutter="20">
      <!-- Profile Info -->
      <el-col :span="12">
        <el-card shadow="never" class="profile-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">管理员信息</span>
            </div>
          </template>
          <div class="profile-avatar">
            <el-avatar :size="80" icon="UserFilled" style="background: #dc2626;" />
          </div>
          <el-descriptions :column="1" border class="profile-desc">
            <el-descriptions-item label="用户名">
              <span class="info-value">{{ userInfo.username || '—' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="邮箱">
              <span class="info-value">{{ userInfo.email || '—' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="手机号">
              <span class="info-value">{{ userInfo.phone || '—' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="角色">
              <el-tag type="primary" size="small" effect="light">管理员</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <!-- Change Password -->
      <el-col :span="12">
        <el-card shadow="never" class="profile-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">修改密码</span>
            </div>
          </template>
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="90px"
            class="password-form"
          >
            <el-form-item label="原密码" prop="old_password">
              <el-input
                v-model="passwordForm.old_password"
                type="password"
                placeholder="请输入原密码"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            <el-form-item label="新密码" prop="new_password">
              <el-input
                v-model="passwordForm.new_password"
                type="password"
                placeholder="请输入新密码（至少 6 位）"
                prefix-icon="Key"
                show-password
              />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirm_password">
              <el-input
                v-model="passwordForm.confirm_password"
                type="password"
                placeholder="请再次输入新密码"
                prefix-icon="Key"
                show-password
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="passwordLoading" @click="handleChangePassword" style="width: 100%;">
                {{ passwordLoading ? '修改中...' : '修改密码' }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { profileAPI } from '@/api'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

const userInfo = computed(() => userStore.user || {})

const passwordFormRef = ref(null)
const passwordLoading = ref(false)

const passwordForm = reactive({
  old_password: '',
  new_password: '',
  confirm_password: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.new_password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  old_password: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  new_password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少 6 位', trigger: 'blur' }
  ],
  confirm_password: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

async function handleChangePassword() {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return

  passwordLoading.value = true
  try {
    const res = await profileAPI.changePassword({
      old_password: passwordForm.old_password,
      new_password: passwordForm.new_password,
      confirm_password: passwordForm.confirm_password
    })
    if (res.code === 200) {
      ElMessage.success('密码修改成功，请重新登录')
      passwordForm.old_password = ''
      passwordForm.new_password = ''
      passwordForm.confirm_password = ''
    }
  } catch (e) {
    // handled by interceptor
  } finally {
    passwordLoading.value = false
  }
}

onMounted(() => {
  userStore.loadFromStorage()
})
</script>

<style scoped>
.admin-profile-page {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.profile-card {
  border: 1px solid var(--color-border-light);
  border-radius: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-text-primary);
}

.profile-avatar {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.profile-desc {
  max-width: 400px;
  margin: 0 auto;
}

.info-value {
  font-weight: 500;
  color: var(--color-text-body);
}

.password-form {
  max-width: 400px;
  margin: 0 auto;
}
</style>
