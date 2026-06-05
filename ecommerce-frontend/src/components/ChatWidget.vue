<template>
  <div class="chat-widget">
    <!-- 浮动入口 -->
    <transition name="bounce">
      <div class="chat-float-btn" v-show="!chatOpen" @click="openChat">
        <el-icon size="26"><Headset /></el-icon>
        <span class="chat-dot"></span>
      </div>
    </transition>

    <!-- 聊天面板 -->
    <transition name="slide-up">
      <div class="chat-panel" v-show="chatOpen">
        <!-- 头部 -->
        <div class="panel-header">
          <el-avatar :size="36" class="header-avatar">客</el-avatar>
          <div class="header-info">
            <span class="header-name">商城客服</span>
            <span class="header-sub">在线客服 · 服务时间 9:00-21:00</span>
          </div>
          <span class="header-tag online">在线</span>
          <el-button text circle size="small" @click="chatOpen = false" class="close-btn">
            <el-icon size="16"><Close /></el-icon>
          </el-button>
        </div>

        <!-- 消息区域 -->
        <div class="panel-body" ref="panelBodyRef">
          <!-- 欢迎语 -->
          <div class="sys-msg">
            <span class="sys-text">欢迎来到商城客服</span>
          </div>
          <div class="sys-msg">
            <span class="sys-text">您可以在此咨询订单、物流、售后等问题</span>
          </div>

          <!-- 快捷问题 -->
          <div class="quick-replies" v-if="msgs.length === 0">
            <span
              v-for="q in quickQuestions"
              :key="q"
              class="quick-chip"
              @click="sendQuick(q)"
            >{{ q }}</span>
          </div>

          <!-- 消息列表 -->
          <template v-for="(item, i) in displayMessages" :key="item._key">
            <!-- 时间分隔 -->
            <div class="sys-msg" v-if="item._isTime">{{ item._timeLabel }}</div>
            <!-- 普通消息 -->
            <div
              v-else
              class="msg-row"
              :class="item._mine ? 'me' : 'cs'"
            >
              <el-avatar :size="34" class="msg-avatar" v-if="!item._mine">客</el-avatar>
              <div class="msg-content">
                <div class="msg-bubble">{{ item.content }}</div>
              </div>
              <el-avatar :size="34" class="msg-avatar me-avatar" v-if="item._mine" :src="userStore.user?.avatar">
                {{ (userStore.user?.username || '我')[0] }}
              </el-avatar>
            </div>
          </template>
        </div>

        <!-- 输入区域 -->
        <div class="panel-footer">
          <div class="input-row">
            <el-input
              v-model="text"
              placeholder="输入您的问题..."
              size="small"
              class="chat-input"
              @keydown.enter="sendMsg"
              :disabled="sending"
            />
            <el-button
              type="primary"
              size="small"
              :loading="sending"
              @click="sendMsg"
              class="send-btn"
            >发送</el-button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { useUserStore } from '@/stores/user'
import { messageAPI } from '@/api'
import { Headset, Close } from '@element-plus/icons-vue'

const userStore = useUserStore()
const chatOpen = ref(false)
const msgs = ref([])
const text = ref('')
const sending = ref(false)
const panelBodyRef = ref(null)

const quickQuestions = [
  '订单什么时候发货？',
  '如何申请退款？',
  '物流信息在哪查？',
  '支持哪些支付方式？'
]

// 处理消息：插入时间分隔
const displayMessages = computed(() => {
  const result = []
  const gap = 2 * 60 * 1000 // 2分钟以上插入时间分隔
  for (let i = 0; i < msgs.value.length; i++) {
    const m = msgs.value[i]
    if (i === 0 || (new Date(m.createdAt) - new Date(msgs.value[i - 1].createdAt)) > gap) {
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
  const date = d.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit' })
  return date + ' ' + time
}

function openChat() {
  if (!userStore.isLoggedIn) {
    import('element-plus').then(({ ElMessage }) => {
      ElMessage.warning('请先登录后咨询客服')
    })
    return
  }
  chatOpen.value = true
  fetchMsgs()
}

async function fetchMsgs() {
  try {
    const res = await messageAPI.list()
    if (res.code === 200) msgs.value = res.data || []
  } catch { /* ignore */ }
}

function sendQuick(q) {
  text.value = q
  sendMsg()
}

async function sendMsg() {
  const content = text.value.trim()
  if (!content) return
  sending.value = true
  const tempId = Date.now()
  const temp = { id: tempId, content, fromUserId: userStore.user?.id, createdAt: new Date().toISOString() }
  msgs.value.push(temp)
  text.value = ''
  scrollBottom()
  try {
    const res = await messageAPI.send(content)
    if (res.code === 200) {
      await fetchMsgs()
    } else {
      msgs.value = msgs.value.filter(m => m.id !== tempId)
      import('element-plus').then(({ ElMessage }) => {
        ElMessage.error(res.message || '发送失败')
      })
    }
  } catch {
    msgs.value = msgs.value.filter(m => m.id !== tempId)
    import('element-plus').then(({ ElMessage }) => {
      ElMessage.error('发送失败，请稍后重试')
    })
  } finally {
    sending.value = false
    scrollBottom()
  }
}

function scrollBottom() {
  nextTick(() => {
    if (panelBodyRef.value) {
      panelBodyRef.value.scrollTop = panelBodyRef.value.scrollHeight
    }
  })
}
</script>

<style scoped>
/* ===== 容器 ===== */
.chat-widget { position: fixed; bottom: 24px; right: 24px; z-index: 999; }

/* ===== 浮动按钮 ===== */
.chat-float-btn {
  width: 56px; height: 56px; border-radius: 50%;
  background: linear-gradient(135deg, #dc2626, #ef4444);
  color: #fff; display: flex; align-items: center; justify-content: center;
  cursor: pointer; position: relative;
  box-shadow: 0 4px 20px rgba(255,80,0,0.4);
  transition: transform 0.3s, box-shadow 0.3s;
}
.chat-float-btn:hover { transform: scale(1.08); box-shadow: 0 6px 28px rgba(255,80,0,0.55); }
.chat-dot {
  position: absolute; top: 6px; right: 6px; width: 12px; height: 12px;
  border-radius: 50%; background: #10b981; border: 2px solid #fff;
  animation: pulse-dot 1.5s ease-in-out infinite;
}
@keyframes pulse-dot { 0%,100%{transform:scale(1)} 50%{transform:scale(1.4)} }

/* ===== 面板 ===== */
.chat-panel {
  width: 380px; height: 540px; background: #f5f6fa; border-radius: 4px;
  display: flex; flex-direction: column; overflow: hidden;
  box-shadow: 0 8px 40px rgba(0,0,0,0.15);
}

/* ===== 头部（淘宝橙） ===== */
.panel-header {
  display: flex; align-items: center; gap: 10px;
  padding: 14px 16px;
  background: linear-gradient(135deg, #dc2626, #ef4444);
  color: #fff;
}
.header-avatar { background: rgba(255,255,255,0.25); color: #fff; font-weight: 700; font-size: 15px; flex-shrink: 0; }
.header-info { flex: 1; display: flex; flex-direction: column; min-width: 0; }
.header-name { font-size: 15px; font-weight: 600; }
.header-sub { font-size: 11px; opacity: 0.85; margin-top: 1px; }
.header-tag { font-size: 10px; padding: 2px 8px; border-radius: 3px; background: rgba(255,255,255,0.2); }
.close-btn { color: #fff; opacity: 0.8; flex-shrink: 0; }
.close-btn:hover { opacity: 1; }

/* ===== 消息区 ===== */
.panel-body {
  flex: 1; overflow-y: auto; padding: 14px 16px;
  background: #f5f5f5;
}
.panel-body::-webkit-scrollbar { width: 5px; }
.panel-body::-webkit-scrollbar-thumb { background: #d0d5dd; border-radius: 3px; }

/* 系统消息 */
.sys-msg { display: flex; justify-content: center; padding: 8px 0; }
.sys-text {
  font-size: 11px; color: #999; background: #e8e8e8;
  padding: 4px 14px; border-radius: 3px; max-width: 85%;
  text-align: center; line-height: 1.5;
}

/* 快捷问题 */
.quick-replies { display: flex; flex-wrap: wrap; gap: 8px; padding: 10px 0 16px; justify-content: center; }
.quick-chip {
  font-size: 12px; color: #dc2626; background: #fff;
  border: 1px solid #fca5a5; border-radius: 4px;
  padding: 7px 14px; cursor: pointer; user-select: none;
  transition: all 0.2s; white-space: nowrap;
}
.quick-chip:hover { background: #fef2f2; border-color: #dc2626; }

/* 消息行 */
.msg-row { display: flex; align-items: flex-start; gap: 10px; margin-bottom: 14px; }
.msg-row.me { flex-direction: row-reverse; }

/* 头像 */
.msg-avatar {
  flex-shrink: 0; font-size: 14px; font-weight: 600;
  background: #fff; color: #dc2626; border: 1.5px solid #fecaca;
}
.me-avatar {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: #fff; border: none;
}

/* 消息内容 */
.msg-content { max-width: 72%; display: flex; flex-direction: column; }
.msg-row.me .msg-content { align-items: flex-end; }

/* 气泡 */
.msg-bubble {
  padding: 10px 14px; border-radius: 4px; font-size: 13px;
  line-height: 1.55; word-break: break-word; position: relative;
}
.msg-row.cs .msg-bubble {
  background: #fff; color: #333; border-top-left-radius: 2px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.04);
}
.msg-row.me .msg-bubble {
  background: linear-gradient(135deg, #ef4444, #dc2626); color: #fff; border-top-right-radius: 2px;
}

/* ===== 输入区 ===== */
.panel-footer {
  padding: 10px 14px; background: #fff;
  border-top: 1px solid #eee;
}
.input-row { display: flex; gap: 8px; align-items: center; }
.chat-input :deep(.el-input__wrapper) {
  border-radius: 4px; background: #f5f6fa; border: none;
  box-shadow: none; padding: 2px 14px;
}
.chat-input :deep(.el-input__wrapper:focus),
.chat-input :deep(.el-input__wrapper:hover) { background: #f0f1f5; }
.chat-input :deep(.el-input__inner) { font-size: 13px; }
.send-btn {
  border-radius: 4px; background: #dc2626; border-color: #dc2626;
  padding: 6px 18px; font-size: 13px;
}
.send-btn:hover { background: #ef4444; border-color: #ef4444; }

/* ===== 过渡动画 ===== */
.bounce-enter-active { animation: bounceIn 0.4s; }
.bounce-leave-active { animation: bounceIn 0.3s reverse; }
@keyframes bounceIn { 0%{transform:scale(0);opacity:0} 60%{transform:scale(1.15)} 100%{transform:scale(1);opacity:1} }
.slide-up-enter-active { transition: all 0.3s cubic-bezier(0.4,0,0.2,1); }
.slide-up-leave-active { transition: all 0.25s ease-in; }
.slide-up-enter-from { opacity: 0; transform: translateY(20px) scale(0.95); }
.slide-up-leave-to { opacity: 0; transform: translateY(20px) scale(0.95); }
</style>
