<template>
  <div class="login-page">
    <StarryParticles />
    <div class="login-card">
      <div class="login-header">
        <h1>欢迎回来</h1>
        <p>登录您的 ShopNow 账户</p>
      </div>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        size="large"
        label-position="top"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <div class="form-extra">
          <el-checkbox v-model="rememberMe">记住密码</el-checkbox>
          <router-link to="/forgot-password" class="forgot-link">忘记密码？</router-link>
        </div>
        <el-form-item>
          <el-button
            type="primary"
            native-type="submit"
            :loading="loading"
            class="submit-btn"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        还没有账户？<router-link to="/register" class="register-link">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import StarryParticles from '@/components/StarryParticles.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)

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
    const res = await userStore.login(form)
    if (res.code === 200) {
      if (rememberMe.value) {
        localStorage.setItem('rememberedUsername', form.username)
        localStorage.setItem('rememberMe', 'true')
      } else {
        localStorage.removeItem('rememberedUsername')
        localStorage.removeItem('rememberMe')
      }
      ElMessage.success('登录成功')
      const redirect = route.query.redirect || '/'
      router.push(redirect)
    } else {
      ElMessage.error(res.message || '登录失败')
    }
  } catch {
    ElMessage.error('登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}

// 恢复记住的用户名
const savedUsername = localStorage.getItem('rememberedUsername')
const savedRememberMe = localStorage.getItem('rememberMe')
if (savedRememberMe === 'true' && savedUsername) {
  form.username = savedUsername
  rememberMe.value = true
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #3f0505 0%, #1f0202 50%, #0d0000 100%);
  padding: 20px;
  position: relative;
}

.login-card {
  position: relative;
  z-index: 1;
  width: 420px;
  max-width: 100%;
  background: #fff;
  border-radius: 6px;
  padding: 40px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-header h1 {
  font-size: 28px;
  color: var(--color-text-primary);
  margin: 0 0 8px;
}

.login-header p {
  color: var(--color-text-muted);
  margin: 0;
  font-size: 14px;
}

.form-extra {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.forgot-link {
  color: var(--color-primary);
  font-size: 14px;
  text-decoration: none;
}

.forgot-link:hover {
  text-decoration: underline;
}

.submit-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  border-radius: 3px;
  background: var(--color-primary-gradient);
  border: none;
  box-shadow: 0 4px 14px rgba(220, 38, 38, 0.35);
}

.submit-btn:hover {
  background: linear-gradient(135deg, #b91c1c, #991b1b);
  box-shadow: 0 6px 20px rgba(220, 38, 38, 0.45);
}

.login-footer {
  text-align: center;
  color: var(--color-text-muted);
  font-size: 14px;
}

.register-link {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
}

.register-link:hover {
  text-decoration: underline;
}
</style>
