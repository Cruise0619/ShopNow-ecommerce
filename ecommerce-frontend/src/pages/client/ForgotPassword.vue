<template>
  <div class="forgot-page">
    <div class="forgot-card">
      <div class="forgot-header">
        <h1>找回密码</h1>
        <p>{{ step === 1 ? '请输入注册邮箱获取验证码' : '请设置新密码' }}</p>
      </div>

      <!-- 第一步：输入邮箱 -->
      <template v-if="step === 1">
        <el-form ref="emailFormRef" :model="emailForm" :rules="emailRules" size="large" label-position="top">
          <el-form-item prop="email">
            <el-input
              v-model="emailForm.email"
              placeholder="请输入注册邮箱"
              :prefix-icon="Message"
              clearable
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              :loading="sendingCode"
              class="submit-btn"
              @click="handleSendCode"
            >
              获取验证码
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 验证码显示区域（开发模式，模拟邮件发送） -->
        <div v-if="sentCode" class="code-display">
          <div class="code-hint">验证码（模拟邮件发送）</div>
          <div class="code-value">
            <span class="code-text">{{ sentCode }}</span>
            <el-button text type="primary" size="small" @click="copyCode">复制</el-button>
          </div>
          <div class="code-expiry">5分钟内有效</div>
        </div>
      </template>

      <!-- 第二步：验证码 + 新密码 -->
      <template v-else>
        <el-form ref="resetFormRef" :model="resetForm" :rules="resetRules" size="large" label-position="top">
          <el-form-item>
            <div class="email-display">
              验证码已发送至 <strong>{{ emailForm.email }}</strong>
            </div>
          </el-form-item>
          <el-form-item prop="code">
            <el-input
              v-model="resetForm.code"
              placeholder="请输入6位验证码"
              :prefix-icon="Key"
              maxlength="6"
              clearable
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="resetForm.password"
              type="password"
              placeholder="请输入新密码（6-20位）"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="resetForm.confirmPassword"
              type="password"
              placeholder="请确认新密码"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              :loading="resetting"
              class="submit-btn"
              @click="handleResetPassword"
            >
              重置密码
            </el-button>
          </el-form-item>
          <div class="step2-actions">
            <el-button
              text
              type="primary"
              class="resend-btn"
              :disabled="countdown > 0"
              @click="handleSendCode"
            >
              {{ countdown > 0 ? `${countdown}秒后重新发送` : '重新发送验证码' }}
            </el-button>
            <el-button text type="info" @click="goBackToEmail">更换邮箱</el-button>
          </div>
        </el-form>
      </template>

      <div class="forgot-footer">
        <router-link to="/login" class="back-link">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { authAPI } from '@/api'
import { ElMessage } from 'element-plus'
import { Message, Lock, Key } from '@element-plus/icons-vue'

const router = useRouter()
const step = ref(1)
const sendingCode = ref(false)
const resetting = ref(false)
const countdown = ref(0)
const sentCode = ref('')
let timer = null

const emailFormRef = ref(null)
const resetFormRef = ref(null)

const emailForm = reactive({
  email: ''
})

const resetForm = reactive({
  code: '',
  password: '',
  confirmPassword: ''
})

const emailRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const validateResetPassword = (rule, value, callback) => {
  if (value !== resetForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const resetRules = {
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateResetPassword, trigger: 'blur' }
  ]
}

function startCountdown() {
  countdown.value = 60
  if (timer) clearInterval(timer)
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      timer = null
    }
  }, 1000)
}

function goBackToEmail() {
  step.value = 1
  resetForm.code = ''
  resetForm.password = ''
  resetForm.confirmPassword = ''
}

function copyCode() {
  navigator.clipboard.writeText(sentCode.value).then(() => {
    ElMessage.success('验证码已复制到剪贴板')
  }).catch(() => {
    ElMessage.info(`验证码：${sentCode.value}`)
  })
}

async function handleSendCode() {
  if (step.value === 1) {
    const valid = await emailFormRef.value.validate().catch(() => false)
    if (!valid) return
  }

  sendingCode.value = true
  try {
    const res = await authAPI.forgotPassword({ email: emailForm.email })
    if (res.code === 200) {
      const code = res.data?.code || ''
      if (code) {
        sentCode.value = code
        ElMessage.success('验证码已生成，请查看下方展示区')
      } else {
        ElMessage.success('验证码已发送')
      }
      if (step.value === 1) {
        step.value = 2
        resetForm.code = code
      }
      startCountdown()
    } else {
      ElMessage.error(res.message || '发送验证码失败')
    }
  } catch {
    ElMessage.error('发送验证码失败，请稍后重试')
  } finally {
    sendingCode.value = false
  }
}

async function handleResetPassword() {
  const valid = await resetFormRef.value.validate().catch(() => false)
  if (!valid) return

  resetting.value = true
  try {
    const res = await authAPI.resetPassword({
      email: emailForm.email,
      code: resetForm.code,
      password: resetForm.password
    })
    if (res.code === 200) {
      sentCode.value = ''
      ElMessage.success('密码重置成功，即将跳转到登录页')
      setTimeout(() => router.push('/login'), 800)
    } else {
      ElMessage.error(res.message || '密码重置失败')
    }
  } catch {
    ElMessage.error('密码重置失败，请稍后重试')
  } finally {
    resetting.value = false
  }
}

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.forgot-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, #ef4444 0%, #dc2626 30%, #b91c1c 60%, #991b1b 100%);
  padding: 14px;
  position: relative;
}
.forgot-page::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse at 30% 20%, rgba(255,255,255,0.08) 0%, transparent 60%),
              radial-gradient(ellipse at 70% 80%, rgba(239,68,68,0.10) 0%, transparent 60%);
  pointer-events: none;
}

.forgot-card {
  width: 420px;
  max-width: 100%;
  background: rgba(255,255,255,0.97);
  backdrop-filter: blur(20px);
  border-radius: 6px;
  padding: 28px;
  box-shadow: var(--shadow-xl), 0 0 0 1px rgba(255,255,255,0.1);
  border: 1px solid rgba(255,255,255,0.2);
}

.forgot-header {
  text-align: center;
  margin-bottom: 32px;
}

.forgot-header h1 {
  font-size: 28px;
  color: var(--color-text-primary);
  margin: 0 0 8px;
}

.forgot-header p {
  color: var(--color-text-muted);
  margin: 0;
  font-size: 14px;
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
  background: linear-gradient(135deg, #b91c1c, #dc2626);
  box-shadow: 0 6px 20px rgba(220, 38, 38, 0.45);
}

.code-display {
  margin: 16px 0 0;
  padding: 16px;
  background: #f0fdf4;
  border: 1px dashed #22c55e;
  border-radius: 6px;
  text-align: center;
}

.code-hint {
  font-size: 13px;
  color: #16a34a;
  margin-bottom: 8px;
}

.code-value {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 6px;
}

.code-text {
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 4px;
  color: #1e293b;
  font-family: 'Courier New', monospace;
}

.code-expiry {
  font-size: 12px;
  color: var(--color-text-muted);
}

.email-display {
  font-size: 14px;
  color: var(--color-text-secondary);
  padding: 8px 0;
}

.email-display strong {
  color: var(--color-text-primary);
}

.step2-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.forgot-footer {
  text-align: center;
  margin-top: 16px;
}

.back-link {
  color: var(--color-primary);
  text-decoration: none;
  font-size: 14px;
}

.back-link:hover {
  text-decoration: underline;
}
</style>
