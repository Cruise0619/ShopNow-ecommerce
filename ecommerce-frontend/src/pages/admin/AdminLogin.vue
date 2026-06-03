<template>
  <div class="admin-login-page">
    <StarryParticles :palette="bluePalette" />
    <div class="login-card">
      <div class="login-header">
        <div class="login-logo">ShopNow</div>
        <p class="login-subtitle">管理员登录</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="submit" :loading="loading" class="login-btn">
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <router-link to="/">返回前台首页</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authAPI } from '@/api'
import StarryParticles from '@/components/StarryParticles.vue'

const bluePalette = [
  '#ffffff', '#ffffff', '#ffffff', '#ffffff',
  '#f0f5ff', '#f0f5ff',
  '#c8dfff',
  '#69b1ff', '#69b1ff', '#69b1ff',
  '#4d9fff',
  '#91caff',
  '#7ab7ff',
  '#5a9cff',
  '#85a5ff',
  '#2f6fef',
]

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await authAPI.login({ username: form.username, password: form.password })
    if (res.code === 200) {
      const user = res.data.user
      if (user.role === 'admin') {
        localStorage.setItem('token', res.data.token)
        localStorage.setItem('user', JSON.stringify(user))
        ElMessage.success('登录成功')
        router.push('/admin/dashboard')
      } else {
        ElMessage.error('非管理员账号，无法登录后台')
      }
    } else {
      ElMessage.error(res.message || '登录失败')
    }
  } catch (error) {
    // Error already handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, #0a1628 0%, #0f2440 40%, #0d1b35 70%, #091428 100%);
  position: relative;
}
.admin-login-page::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse at 30% 30%, rgba(47, 111, 239, 0.10) 0%, transparent 50%),
              radial-gradient(ellipse at 70% 70%, rgba(77, 159, 255, 0.07) 0%, transparent 50%);
  pointer-events: none;
}

.login-card {
  width: 420px;
  padding: 48px 40px 36px;
  background: rgba(255,255,255,0.97);
  backdrop-filter: blur(20px);
  border-radius: 12px;
  box-shadow: var(--shadow-xl);
  border: 1px solid rgba(255,255,255,0.15);
}

.login-header {
  text-align: center;
  margin-bottom: 36px;
}

.login-logo {
  font-size: 2rem;
  font-weight: 800;
  background: linear-gradient(135deg, #4d9fff, #2f6fef);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 1px;
}

.login-subtitle {
  margin-top: 8px;
  color: var(--color-text-muted);
  font-size: 0.95rem;
}

.login-btn {
  width: 100%;
  background: linear-gradient(135deg, #4d9fff, #2f6fef);
  border: none;
  box-shadow: 0 4px 14px rgba(47, 111, 239, 0.35);
}

.login-btn:hover {
  background: linear-gradient(135deg, #3a8eef, #2563eb);
  box-shadow: 0 6px 20px rgba(47, 111, 239, 0.45);
}

.login-footer {
  text-align: center;
  margin-top: 8px;
}

.login-footer a {
  color: #4d9fff;
  font-size: 0.85rem;
  text-decoration: none;
}

.login-footer a:hover {
  text-decoration: underline;
}
</style>
