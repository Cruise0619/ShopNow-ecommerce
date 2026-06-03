<template>
  <div class="cs-page">
    <div class="cs-container">
      <!-- 商家信息头部 -->
      <div class="cs-header">
        <div class="cs-header-top">
          <div class="cs-brand">
            <div class="cs-brand-avatar">
              <el-avatar :size="44" src="/default-avatar.png">客</el-avatar>
              <span class="cs-online-dot"></span>
            </div>
            <div class="cs-brand-info">
              <div class="cs-brand-name">
                ShopNow 官方客服
                <span class="cs-badge">官方</span>
              </div>
              <div class="cs-brand-desc">专业客服 · 用心服务每一位客户</div>
            </div>
          </div>
          <div class="cs-header-meta">
            <div class="cs-status">
              <span class="cs-status-dot"></span>在线
            </div>
            <div class="cs-hours">服务时间 9:00 - 21:00</div>
          </div>
        </div>
      </div>

      <!-- 消息区域 -->
      <div class="cs-body" ref="chatBodyRef" v-loading="loading">
        <!-- 欢迎卡片 -->
        <div class="cs-welcome" v-if="messages.length === 0 && !loading">
          <div class="welcome-card">
            <div class="welcome-icon">
              <el-avatar :size="56" src="/default-avatar.png">客</el-avatar>
            </div>
            <div class="welcome-text">
              <div class="welcome-title">您好，欢迎咨询 ShopNow 客服</div>
              <div class="welcome-sub">请问有什么可以帮到您？</div>
            </div>
          </div>
          <div class="welcome-divider"><span>常见问题</span></div>
          <div class="welcome-faq">
            <div
              v-for="q in quickQuestions"
              :key="q"
              class="faq-chip"
              @click="sendQuick(q)"
            >
              <el-icon><QuestionFilled /></el-icon>
              <span>{{ q }}</span>
            </div>
          </div>
        </div>

        <!-- 时间标签 -->
        <div class="cs-time-tag" v-if="messages.length">
          <span>{{ formatDate(messages[0]?.createdAt) }}</span>
        </div>

        <!-- 消息列表 -->
        <template v-for="(item, i) in displayMessages" :key="item._key">
          <div class="cs-time-tag" v-if="item._isTime"><span>{{ item._timeLabel }}</span></div>
          <div
            v-else
            class="cs-msg"
            :class="item._mine ? 'msg-mine' : 'msg-cs'"
          >
            <el-avatar :size="36" class="msg-avatar" v-if="!item._mine" src="/default-avatar.png">客</el-avatar>
            <div class="msg-body">
              <div class="msg-bubble" v-if="!item._mine">
                {{ item.content }}
              </div>
              <div class="msg-bubble" v-else>
                {{ item.content }}
              </div>
            </div>
            <el-avatar :size="36" class="msg-avatar mine-avatar" v-if="item._mine">
              {{ (userStore.user?.username || '我')[0] }}
            </el-avatar>
          </div>
        </template>
        <div class="cs-empty" v-if="messages.length === 0 && !loading">
          <span>暂无消息记录</span>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="cs-footer">
        <div class="footer-toolbar">
          <span class="toolbar-hint">输入您的问题，客服会尽快回复</span>
        </div>
        <div class="footer-input-row">
          <el-input
            v-model="inputText"
            type="textarea"
            :rows="2"
            placeholder="请输入您要咨询的问题..."
            resize="none"
            class="input-textarea"
            maxlength="500"
            show-word-limit
            @keydown.enter.exact.prevent="handleSend"
          />
          <el-button
            type="primary"
            :loading="sending"
            :disabled="!inputText.trim()"
            @click="handleSend"
            class="send-btn"
            round
          >
            发送
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useUserStore } from '@/stores/user'
import { messageAPI } from '@/api'
import { ElMessage } from 'element-plus'
import { QuestionFilled } from '@element-plus/icons-vue'

const userStore = useUserStore()
const loading = ref(false)
const sending = ref(false)
const messages = ref([])
const inputText = ref('')
const chatBodyRef = ref(null)
let pollTimer = null

const quickQuestions = [
  '订单什么时候发货？',
  '如何申请退款？',
  '物流信息在哪里查看？',
  '支持哪些支付方式？',
  '优惠券如何使用？',
  '商品质量有问题怎么办？'
]

const displayMessages = computed(() => {
  const result = []
  const gap = 3 * 60 * 1000
  for (let i = 0; i < messages.value.length; i++) {
    const m = messages.value[i]
    if (i === 0 || (new Date(m.createdAt) - new Date(messages.value[i - 1].createdAt)) > gap) {
      result.push({
        _key: 't' + m.id,
        _isTime: true,
        _timeLabel: formatTime(m.createdAt)
      })
    }
    result.push({
      _key: 'm' + m.id,
      ...m,
      _mine: m.fromUserId === userStore.user?.id
    })
  }
  return result
})

function formatDate(t) {
  if (!t) return ''
  const d = new Date(t)
  return d.toLocaleString('zh-CN', { month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const time = d.toLocaleString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  if (d.toDateString() === now.toDateString()) return time
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (d.toDateString() === yesterday.toDateString()) return '昨天 ' + time
  return d.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit' }) + ' ' + time
}

onMounted(async () => {
  loading.value = true
  await fetchMessages()
  loading.value = false
  pollTimer = setInterval(fetchMessages, 3000)
})

onBeforeUnmount(() => {
  if (pollTimer) clearInterval(pollTimer)
})

async function fetchMessages() {
  try {
    const res = await messageAPI.list()
    if (res.code === 200) {
      const list = res.data || []
      if (list.length !== messages.value.length) {
        messages.value = list
        await nextTick()
        scrollToBottom()
      }
    }
  } catch { /* silent */ }
}

function sendQuick(q) {
  inputText.value = q
  handleSend()
}

async function handleSend() {
  const text = inputText.value.trim()
  if (!text) return
  sending.value = true
  const tempId = Date.now()
  messages.value.push({
    id: tempId, content: text,
    fromUserId: userStore.user?.id,
    createdAt: new Date().toISOString()
  })
  inputText.value = ''
  await nextTick()
  scrollToBottom()
  try {
    const res = await messageAPI.send(text)
    if (res.code === 200) {
      const idx = messages.value.findIndex(m => m.id === tempId)
      if (idx > -1) messages.value[idx] = res.data
    } else {
      messages.value = messages.value.filter(m => m.id !== tempId)
      ElMessage.error(res.message || '发送失败')
    }
  } catch {
    messages.value = messages.value.filter(m => m.id !== tempId)
    ElMessage.error('发送失败，请稍后重试')
  } finally {
    sending.value = false
    await fetchMessages()
  }
}

function scrollToBottom() {
  if (chatBodyRef.value) {
    chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight
  }
}
</script>

<style scoped>
.cs-page {
  max-width: 780px;
  margin: 0 auto;
  padding: 16px 16px 0;
  height: calc(100vh - 140px);
}

.cs-container {
  height: 100%;
  background: #fff;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 2px 24px rgba(0,0,0,0.06);
}

/* ===== 头部 ===== */
.cs-header {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  padding: 20px 24px;
  color: #fff;
  flex-shrink: 0;
}
.cs-header-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.cs-brand {
  display: flex;
  align-items: center;
  gap: 14px;
}
.cs-brand-avatar {
  position: relative;
}
.cs-online-dot {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 12px;
  height: 12px;
  background: #22c55e;
  border: 2px solid #fff;
  border-radius: 50%;
}
.cs-brand-name {
  font-size: 17px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}
.cs-badge {
  font-size: 11px;
  background: rgba(255,255,255,0.2);
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 400;
}
.cs-brand-desc {
  font-size: 12px;
  opacity: 0.75;
  margin-top: 3px;
}
.cs-header-meta {
  text-align: right;
}
.cs-status {
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 6px;
  justify-content: flex-end;
}
.cs-status-dot {
  width: 8px;
  height: 8px;
  background: #22c55e;
  border-radius: 50%;
  animation: pulse-dot 2s infinite;
}
@keyframes pulse-dot {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}
.cs-hours {
  font-size: 11px;
  opacity: 0.6;
  margin-top: 4px;
}

/* ===== 消息区 ===== */
.cs-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  background: #f7f8fa;
}
.cs-body::-webkit-scrollbar { width: 5px; }
.cs-body::-webkit-scrollbar-thumb { background: #d0d5dd; border-radius: 3px; }

/* 欢迎卡片 */
.cs-welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}
.welcome-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: #fff;
  border-radius: 16px;
  padding: 20px 28px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  margin-bottom: 24px;
}
.welcome-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 4px;
}
.welcome-sub {
  font-size: 13px;
  color: #888;
}
.welcome-divider {
  width: 100%;
  text-align: center;
  border-bottom: 1px solid #e8e8e8;
  line-height: 0;
  margin-bottom: 16px;
}
.welcome-divider span {
  background: #f7f8fa;
  padding: 0 16px;
  font-size: 12px;
  color: #aaa;
}
.welcome-faq {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}
.faq-chip {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #3b5998;
  background: #fff;
  border: 1px solid #dce3f0;
  border-radius: 20px;
  padding: 10px 18px;
  cursor: pointer;
  transition: all 0.2s;
}
.faq-chip:hover {
  background: #f0f4ff;
  border-color: #3b5998;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(59,89,152,0.1);
}
.faq-chip .el-icon {
  font-size: 15px;
}

/* 时间标签 */
.cs-time-tag {
  display: flex;
  justify-content: center;
  padding: 14px 0;
}
.cs-time-tag span {
  font-size: 12px;
  color: #b0b0b0;
  background: #ecedf0;
  padding: 4px 14px;
  border-radius: 10px;
}
.cs-empty {
  text-align: center;
  padding: 40px;
  color: #ccc;
  font-size: 13px;
}

/* 消息行 */
.cs-msg {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 18px;
}
.cs-msg.msg-mine {
  flex-direction: row-reverse;
}
.msg-avatar {
  flex-shrink: 0;
  font-size: 14px;
  font-weight: 600;
}
.cs-msg.msg-cs .msg-avatar {
  border: 2px solid #e8ecf1;
}
.cs-msg.msg-mine .msg-avatar,
.mine-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
}

/* 消息气泡 */
.msg-body {
  max-width: 65%;
}
.msg-bubble {
  padding: 12px 16px;
  font-size: 14px;
  line-height: 1.65;
  word-break: break-word;
  border-radius: 14px;
  position: relative;
}
.cs-msg.msg-cs .msg-bubble {
  background: #fff;
  color: #333;
  border-top-left-radius: 4px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.cs-msg.msg-mine .msg-bubble {
  background: #3b5998;
  color: #fff;
  border-top-right-radius: 4px;
  box-shadow: 0 2px 8px rgba(59,89,152,0.18);
}

/* ===== 输入区 ===== */
.cs-footer {
  padding: 12px 20px 16px;
  background: #fff;
  border-top: 1px solid #eef0f4;
  flex-shrink: 0;
}
.footer-toolbar {
  margin-bottom: 8px;
}
.toolbar-hint {
  font-size: 12px;
  color: #bbb;
}
.footer-input-row {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}
.input-textarea {
  flex: 1;
}
.input-textarea :deep(.el-textarea__inner) {
  border-radius: 12px;
  border-color: #e2e5ea;
  font-size: 14px;
  padding: 10px 14px;
  background: #f9fafb;
  transition: all 0.2s;
}
.input-textarea :deep(.el-textarea__inner:focus) {
  border-color: #3b5998;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(59,89,152,0.08);
}
.send-btn {
  background: linear-gradient(135deg, #3b5998, #4a6db5);
  border: none;
  padding: 10px 28px;
  font-size: 14px;
  height: 40px;
  box-shadow: 0 3px 10px rgba(59,89,152,0.25);
}
.send-btn:hover {
  background: linear-gradient(135deg, #344e86, #3b5998);
}
</style>
