<template>
  <div class="register-page">
    <div class="register-card">
      <div class="register-header">
        <h1>创建账户</h1>
        <p>立即注册，开启品质购物之旅</p>
      </div>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        size="large"
        label-position="top"
        @submit.prevent="handleRegister"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>
        <el-form-item prop="email">
          <div class="email-row">
            <el-input
              v-model="form.email"
              placeholder="请输入邮箱地址"
              :prefix-icon="Message"
              clearable
            />
            <el-button
              class="send-code-btn"
              :disabled="sendCooldown > 0 || !form.email"
              :loading="sendCodeLoading"
              @click="sendVerifyCode"
            >
              {{ sendCooldown > 0 ? `${sendCooldown}s后重发` : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item prop="code">
          <el-input
            v-model="form.code"
            placeholder="请输入邮箱验证码"
            :prefix-icon="Key"
            clearable
          />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入手机号码"
            :prefix-icon="Phone"
            clearable
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码（6-20位）"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请确认密码"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            native-type="submit"
            :loading="loading"
            class="submit-btn"
          >
            注 册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="register-footer">
        已有账户？<router-link to="/login" class="login-link">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { authAPI } from '@/api'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, Phone, Key } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const sendCodeLoading = ref(false)
const sendCooldown = ref(0)
let cooldownTimer = null

const form = reactive({
  username: '',
  email: '',
  code: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

async function sendVerifyCode() {
  if (!form.email) {
    ElMessage.warning('请先输入邮箱地址')
    return
  }
  sendCodeLoading.value = true
  try {
    const res = await authAPI.sendCode({ email: form.email })
    if (res.code === 200) {
      ElMessage.success(`验证码已发送至 ${form.email}`)
      // 开发环境自动填充验证码
      if (res.data?.code) {
        form.code = res.data.code
        ElMessage.info(`开发环境验证码自动填充: ${res.data.code}`)
      }
      sendCooldown.value = 60
      cooldownTimer = setInterval(() => {
        sendCooldown.value--
        if (sendCooldown.value <= 0) {
          clearInterval(cooldownTimer)
          cooldownTimer = null
        }
      }, 1000)
    }
  } catch {
    // handled by interceptor
  } finally {
    sendCodeLoading.value = false
  }
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await userStore.register({
      username: form.username,
      email: form.email,
      code: form.code,
      phone: form.phone,
      password: form.password
    })
    if (res.code === 200) {
      ElMessage.success('注册成功，欢迎您！')
      router.push('/')
    } else {
      ElMessage.error(res.message || '注册失败')
    }
  } catch {
    ElMessage.error('注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, #ef4444 0%, #dc2626 30%, #b91c1c 60%, #991b1b 100%);
  padding: 14px;
  position: relative;
}
.register-page::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse at 30% 20%, rgba(255,255,255,0.08) 0%, transparent 60%),
              radial-gradient(ellipse at 70% 80%, rgba(239,68,68,0.10) 0%, transparent 60%);
  pointer-events: none;
}

.register-card {
  width: 460px;
  max-width: 100%;
  background: rgba(255,255,255,0.97);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 28px;
  box-shadow: var(--shadow-xl), 0 0 0 1px rgba(255,255,255,0.1);
  border: 1px solid rgba(255,255,255,0.2);
}

.register-header {
  text-align: center;
  margin-bottom: 28px;
}

.register-header h1 {
  font-size: 28px;
  color: var(--color-text-primary);
  margin: 0 0 8px;
}

.register-header p {
  color: var(--color-text-muted);
  margin: 0;
  font-size: 14px;
}

.email-row {
  display: flex;
  gap: 8px;
}
.email-row .el-input {
  flex: 1;
}
.send-code-btn {
  white-space: nowrap;
  min-width: 110px;
}
.submit-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  border-radius: 8px;
  background: var(--color-primary-gradient);
  border: none;
  margin-top: 8px;
  box-shadow: 0 4px 14px rgba(220, 38, 38, 0.35);
}

.submit-btn:hover {
  background: linear-gradient(135deg, #b91c1c, #dc2626);
  box-shadow: 0 6px 20px rgba(220, 38, 38, 0.45);
}

.register-footer {
  text-align: center;
  color: var(--color-text-muted);
  font-size: 14px;
}

.login-link {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
}

.login-link:hover {
  text-decoration: underline;
}
</style>
