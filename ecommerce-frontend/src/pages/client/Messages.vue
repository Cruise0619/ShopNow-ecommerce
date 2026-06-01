<template>
  <div class="messages-page">
    <div class="chat-container">
      <!-- 头部 -->
      <div class="chat-header">
        <el-avatar :size="40" class="header-avatar">客</el-avatar>
        <div class="header-info">
          <span class="header-name">商城客服</span>
          <span class="header-sub">在线客服 · 服务时间 9:00-21:00</span>
        </div>
        <span class="header-status">在线</span>
      </div>

      <!-- 消息区 -->
      <div class="chat-body" ref="chatBodyRef" v-loading="loading">
        <!-- 欢迎语 -->
        <div class="sys-msg">
          <span class="sys-text">欢迎来到商城客服</span>
        </div>
        <div class="sys-msg">
          <span class="sys-text">您可以在此咨询订单、物流、售后等问题</span>
        </div>

        <!-- 快捷问题（无历史时显示） -->
        <div class="quick-replies" v-if="messages.length === 0 && !loading">
          <span
            v-for="q in quickQuestions"
            :key="q"
            class="quick-chip"
            @click="sendQuick(q)"
          >{{ q }}</span>
        </div>

        <!-- 消息列表 -->
        <template v-for="(item, i) in displayMessages" :key="item._key">
          <div class="sys-msg" v-if="item._isTime">{{ item._timeLabel }}</div>
          <div
            v-else
            class="msg-row"
            :class="item._mine ? 'me' : 'cs'"
          >
            <el-avatar :size="38" class="msg-avatar" v-if="!item._mine">客</el-avatar>
            <div class="msg-content">
              <div class="msg-bubble">{{ item.content }}</div>
            </div>
            <el-avatar :size="38" class="msg-avatar me-avatar" v-if="item._mine">
              {{ (userStore.user?.username || '我')[0] }}
            </el-avatar>
          </div>
        </template>

        <el-empty v-if="messages.length === 0 && !loading" description="暂无消息" :image-size="60" />
      </div>

      <!-- 输入区 -->
      <div class="chat-footer">
        <div class="footer-input-row">
          <el-input
            v-model="inputText"
            type="textarea"
            :rows="2"
            placeholder="输入您的问题..."
            resize="none"
            class="msg-textarea"
            @keydown.enter.exact.prevent="handleSend"
          />
          <div class="footer-actions">
            <span class="enter-hint">按 Enter 发送，Shift+Enter 换行</span>
            <el-button
              type="primary"
              :loading="sending"
              :disabled="!inputText.trim()"
              @click="handleSend"
              class="send-btn"
            >发送</el-button>
          </div>
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
  '物流信息在哪查？',
  '支持哪些支付方式？',
  '优惠券如何使用？'
]

// 处理消息：插入时间分隔
const displayMessages = computed(() => {
  const result = []
  const gap = 2 * 60 * 1000
  for (let i = 0; i < messages.value.length; i++) {
    const m = messages.value[i]
    if (i === 0 || (new Date(m.createdAt) - new Date(messages.value[i - 1].createdAt)) > gap) {
      result.push({
        _key: 't' + m.id,
        _isTime: true,
        _timeLabel: formatDate(m.createdAt)
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
  const now = new Date()
  const time = d.toLocaleString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  if (d.toDateString() === now.toDateString()) return time
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
  } catch { /* silent poll */ }
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
    id: tempId,
    content: text,
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
.messages-page {
  max-width: 820px; margin: 0 auto; padding: 14px 14px;
  height: calc(100vh - 140px); display: flex; flex-direction: column;
}

.chat-container {
  flex: 1; background: #fff; border-radius: 12px; display: flex;
  flex-direction: column; overflow: hidden;
  box-shadow: 0 2px 20px rgba(0,0,0,0.06);
}

/* 头部 */
.chat-header {
  display: flex; align-items: center; gap: 12px;
  padding: 16px 22px;
  background: linear-gradient(135deg, #dc2626, #ef4444);
  color: #fff;
}
.header-avatar { background: rgba(255,255,255,0.22); color: #fff; font-weight: 700; font-size: 17px; }
.header-info { flex: 1; display: flex; flex-direction: column; }
.header-name { font-size: 17px; font-weight: 600; }
.header-sub { font-size: 12px; opacity: 0.85; margin-top: 2px; }
.header-status {
  font-size: 12px; background: rgba(255,255,255,0.2);
  padding: 4px 12px; border-radius: 20px;
}

/* 消息区 */
.chat-body {
  flex: 1; overflow-y: auto; padding: 22px 28px;
  background: #f5f5f5;
}
.chat-body::-webkit-scrollbar { width: 5px; }
.chat-body::-webkit-scrollbar-thumb { background: #d0d5dd; border-radius: 3px; }

/* 系统消息 */
.sys-msg { display: flex; justify-content: center; padding: 8px 0; }
.sys-text {
  font-size: 12px; color: #999; background: #e8e8e8;
  padding: 5px 16px; border-radius: 10px;
  text-align: center; line-height: 1.5;
}

/* 快捷问题 */
.quick-replies { display: flex; flex-wrap: wrap; gap: 10px; padding: 12px 0 20px; justify-content: center; }
.quick-chip {
  font-size: 13px; color: #dc2626; background: #fff;
  border: 1px solid #fca5a5; border-radius: 20px;
  padding: 8px 18px; cursor: pointer; user-select: none;
  transition: all 0.2s;
}
.quick-chip:hover { background: #fef2f2; border-color: #dc2626; }

/* 消息行 */
.msg-row { display: flex; align-items: flex-start; gap: 12px; margin-bottom: 16px; }
.msg-row.me { flex-direction: row-reverse; }

/* 头像 */
.msg-avatar { flex-shrink: 0; font-size: 15px; font-weight: 600; }
.msg-row.cs .msg-avatar { background: #fff; color: #dc2626; border: 2px solid #fecaca; }
.msg-row.me .msg-avatar { background: linear-gradient(135deg, #ef4444, #dc2626); color: #fff; }

/* 消息内容 */
.msg-content { max-width: 68%; display: flex; flex-direction: column; }
.msg-row.me .msg-content { align-items: flex-end; }

/* 气泡 */
.msg-bubble {
  padding: 12px 16px; border-radius: 12px; font-size: 14px;
  line-height: 1.6; word-break: break-word; position: relative;
}
.msg-row.cs .msg-bubble {
  background: #fff; color: #333; border-top-left-radius: 2px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.msg-row.me .msg-bubble {
  background: #fff0e6; color: #333; border-top-right-radius: 2px;
}

/* 输入区 */
.chat-footer {
  padding: 14px 22px 18px; background: #fff; border-top: 1px solid #eee;
}
.msg-textarea :deep(.el-textarea__inner) {
  border-radius: 12px; border-color: #e5e7eb; font-size: 14px;
  padding: 12px 16px; background: #ffffff;
}
.msg-textarea :deep(.el-textarea__inner:focus) {
  border-color: #dc2626; background: #fff;
}
.footer-actions {
  display: flex; justify-content: space-between; align-items: center; margin-top: 10px;
}
.enter-hint { font-size: 12px; color: #aaa; }
.send-btn {
  border-radius: 20px; background: #dc2626; border-color: #dc2626;
  padding: 8px 24px; font-size: 14px;
}
.send-btn:hover { background: #ef4444; border-color: #ef4444; }
</style>
