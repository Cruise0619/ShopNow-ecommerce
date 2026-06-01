<template>
  <div class="profile-page">
    <h1 class="page-title">个人中心</h1>

    <el-row :gutter="24">
      <!-- 左侧菜单 -->
      <el-col :xs="24" :sm="6" :md="5">
        <el-card class="sidebar-card" shadow="never">
          <div class="user-summary">
            <div
              class="sidebar-avatar-wrapper"
              :class="{ editable: editing }"
              @click="editing && triggerUpload()"
            >
              <el-avatar :size="64" :src="avatarPreview || profile.avatar" class="user-avatar">
                {{ (profile.username || 'U')[0]?.toUpperCase() }}
              </el-avatar>
              <div class="avatar-overlay" v-if="editing">
                <el-icon size="18"><Camera /></el-icon>
                <span>更换头像</span>
              </div>
            </div>
            <input
              ref="avatarInput"
              type="file"
              accept="image/jpeg,image/png,image/gif,image/webp"
              hidden
              @change="handleAvatarChange"
            />
            <h3 class="user-name">{{ profile.username || '用户' }}</h3>
            <p class="user-email">{{ profile.email || '' }}</p>
          </div>
          <el-menu
            :default-active="activeMenu"
            class="sidebar-menu"
            @select="handleMenuSelect"
          >
            <el-menu-item index="info">
              <el-icon><User /></el-icon>
              <span>个人信息</span>
            </el-menu-item>
            <el-menu-item index="password">
              <el-icon><Lock /></el-icon>
              <span>修改密码</span>
            </el-menu-item>
            <el-menu-item index="addresses">
              <el-icon><Location /></el-icon>
              <span>收货地址</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>

      <!-- 右侧内容 -->
      <el-col :xs="24" :sm="18" :md="19">
        <!-- 个人信息 -->
        <el-card class="content-card" shadow="never" v-if="activeMenu === 'info'" v-loading="profileLoading">
          <template #header><span class="card-title">个人信息</span></template>
          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-position="top"
            :disabled="!editing"
          >
            <el-row :gutter="16">
              <el-col :xs="24" :sm="12">
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="profileForm.username" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="profileForm.email" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="手机号码" prop="phone">
                  <el-input v-model="profileForm.phone" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
          <div class="form-actions" v-if="!editing">
            <el-button type="primary" @click="editing = true">编辑资料</el-button>
          </div>
          <div class="form-actions" v-else>
            <el-button @click="cancelEdit">取消</el-button>
            <el-button type="primary" :loading="savingProfile" @click="handleSaveProfile">保存</el-button>
          </div>
        </el-card>

        <!-- 修改密码 -->
        <el-card class="content-card" shadow="never" v-if="activeMenu === 'password'">
          <template #header><span class="card-title">修改密码</span></template>
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-position="top"
            style="max-width: 480px"
          >
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                show-password
                placeholder="请输入当前密码"
              />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                show-password
                placeholder="请输入新密码（6-20位）"
              />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                show-password
                placeholder="请再次输入新密码"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :loading="changingPassword"
                @click="handleChangePassword"
              >
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 收货地址（简要入口） -->
        <el-card class="content-card" shadow="never" v-if="activeMenu === 'addresses'">
          <template #header>
            <div class="card-title-row">
              <span class="card-title">收货地址</span>
              <el-button type="primary" size="small" @click="$router.push('/addresses')">
                管理收货地址
              </el-button>
            </div>
          </template>
          <p class="hint-text">点击上方按钮跳转到收货地址管理页面，可添加、编辑或删除您的收货地址。</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { profileAPI } from '@/api'
import { ElMessage } from 'element-plus'
import { User, Lock, Location, Camera } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const activeMenu = ref('info')
const editing = ref(false)
const profileLoading = ref(false)
const savingProfile = ref(false)
const changingPassword = ref(false)

const profileFormRef = ref(null)
const passwordFormRef = ref(null)
const avatarInput = ref(null)
const avatarPreview = ref('')
const uploadingAvatar = ref(false)

const profile = reactive({
  username: '',
  email: '',
  phone: '',
  avatar: ''
})

const profileForm = reactive({
  username: '',
  email: '',
  phone: '',
  avatar: ''
})

const profileRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [{ required: true, message: '请输入手机号码', trigger: 'blur' }]
}

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateNewPwd = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateNewPwd, trigger: 'blur' }
  ]
}

onMounted(async () => {
  await fetchProfile()
})

async function fetchProfile() {
  profileLoading.value = true
  try {
    const res = await profileAPI.get()
    if (res.code === 200) {
      const data = res.data
      Object.assign(profile, data)
      avatarPreview.value = data.avatar || ''
      if (!editing.value) {
        Object.assign(profileForm, data)
      }
      // 更新 store
      if (userStore.user) {
        Object.assign(userStore.user, data)
      }
    }
  } catch {
    // 使用 store 中的用户信息
    if (userStore.user) {
      Object.assign(profile, userStore.user)
      Object.assign(profileForm, userStore.user)
      avatarPreview.value = userStore.user.avatar || ''
    }
  } finally {
    profileLoading.value = false
  }
}

function handleMenuSelect(index) {
  activeMenu.value = index
}

function triggerUpload() {
  avatarInput.value?.click()
}

async function handleAvatarChange(e) {
  const file = e.target.files?.[0]
  if (!file) return

  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 5MB')
    return
  }

  const formData = new FormData()
  formData.append('avatar', file)
  uploadingAvatar.value = true
  try {
    const res = await profileAPI.uploadAvatar(formData)
    if (res.code === 200) {
      avatarPreview.value = res.data.avatar
      profileForm.avatar = res.data.avatar
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch {
    ElMessage.error('上传失败')
  } finally {
    uploadingAvatar.value = false
    // Reset input so re-selecting the same file triggers change again
    e.target.value = ''
  }
}

function cancelEdit() {
  editing.value = false
  avatarPreview.value = profile.avatar || ''
  Object.assign(profileForm, profile)
}

async function handleSaveProfile() {
  const valid = await profileFormRef.value.validate().catch(() => false)
  if (!valid) return

  savingProfile.value = true
  try {
    const res = await profileAPI.update(profileForm)
    if (res.code === 200) {
      ElMessage.success('资料更新成功')
      Object.assign(profile, profileForm)
      if (userStore.user) {
        Object.assign(userStore.user, profileForm)
        localStorage.setItem('user', JSON.stringify(userStore.user))
      }
      editing.value = false
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch {
    ElMessage.error('更新失败，请稍后重试')
  } finally {
    savingProfile.value = false
  }
}

async function handleChangePassword() {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return

  changingPassword.value = true
  try {
    const res = await profileAPI.changePassword({
      old_password: passwordForm.oldPassword,
      new_password: passwordForm.newPassword
    })
    if (res.code === 200) {
      ElMessage.success('密码修改成功，请重新登录')
      userStore.logout()
      router.push('/login')
    } else {
      ElMessage.error(res.message || '修改失败')
    }
  } catch {
    ElMessage.error('修改密码失败')
  } finally {
    changingPassword.value = false
  }
}
</script>

<style scoped>
.profile-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 14px 14px;
}

.page-title {
  font-size: 1.3rem;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0 0 16px;
}

.sidebar-card {
  border-radius: 12px;
  text-align: center;
  background: var(--gradient-card);
  border: 1px solid var(--color-border-light);
}

.user-summary {
  padding: 20px 0 12px;
  border-bottom: 1px solid var(--color-border-light);
  margin-bottom: 8px;
}

.sidebar-avatar-wrapper {
  position: relative;
  display: inline-block;
  border-radius: 50%;
  margin-bottom: 12px;
}

.sidebar-avatar-wrapper.editable {
  cursor: pointer;
}

.sidebar-avatar-wrapper.editable:hover .avatar-overlay {
  opacity: 1;
}

.user-avatar {
  display: block;
}

.user-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 4px;
}

.user-email {
  font-size: 13px;
  color: var(--color-text-placeholder);
  margin: 0;
}

.sidebar-menu {
  border: none;
}

.sidebar-menu .el-menu-item {
  height: 44px;
  line-height: 44px;
  border-radius: 8px;
  margin: 2px 8px;
}

.content-card {
  border-radius: 12px;
  min-height: 400px;
  background: var(--gradient-card);
  border: 1px solid var(--color-border-light);
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.card-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-actions {
  padding-top: 16px;
  border-top: 1px solid var(--color-border-light);
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.hint-text {
  color: var(--color-text-placeholder);
  font-size: 14px;
  line-height: 1.6;
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 11px;
  gap: 2px;
  opacity: 0;
  transition: opacity 0.3s;
  border-radius: 50%;
  pointer-events: none;
}

@media (max-width: 768px) {
  .sidebar-card {
    margin-bottom: 16px;
  }
}
</style>
