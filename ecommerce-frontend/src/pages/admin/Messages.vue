<template>
  <div class="admin-messages-page">
    <el-row :gutter="16" class="msg-container">
      <!-- 左侧会话列表 -->
      <el-col :span="7">
        <el-card shadow="never" class="conv-card">
          <template #header>
            <div class="conv-card-title">
              <span>会话列表</span>
              <el-badge :value="totalUnread" :hidden="!totalUnread" class="unread-badge" />
            </div>
          </template>
          <div class="conv-list" v-loading="convLoading">
            <div
              v-for="conv in conversations"
              :key="conv.userId"
              class="conv-item"
              :class="{ active: activeUserId === conv.userId }"
              @click="selectConv(conv)"
            >
              <el-avatar :size="44" class="conv-avatar">
                {{ (conv.username || '用')[0] }}
              </el-avatar>
              <div class="conv-info">
                <div class="conv-top">
                  <strong class="conv-name">{{ conv.username }}</strong>
                  <span class="conv-time">{{ formatShort(conv.lastTime) }}</span>
                </div>
                <div class="conv-bottom">
                  <span class="conv-preview">{{ conv.lastMessage || '暂无消息' }}</span>
                  <el-badge :value="conv.unread" :hidden="!conv.unread" />
                </div>
              </div>
            </div>
            <el-empty v-if="!conversations.length && !convLoading" description="暂无用户咨询" :image-size="60" />
          </div>
        </el-card>
      </el-col>

      <!-- 右侧聊天窗口 -->
      <el-col :span="17">
        <el-card shadow="never" class="chat-card" v-if="activeUserId">
          <template #header>
            <div class="chat-header">
              <el-avatar :size="38" class="chat-header-avatar">
                {{ (activeUsername || '用')[0] }}
              </el-avatar>
              <div class="chat-header-info">
                <span class="chat-header-name">{{ activeUsername }}</span>
                <span class="chat-header-sub">用户咨询</span>
              </div>
            </div>
          </template>
          <div class="chat-body" ref="adminChatBodyRef" v-loading="chatLoading">
            <div class="sys-msg" v-if="chatMessages.length">
              <span class="sys-text">—— 以上为历史消息 ——</span>
            </div>
            <template v-for="(item, i) in displayChatMessages" :key="item._key">
              <div class="sys-msg" v-if="item._isTime">{{ item._timeLabel }}</div>
              <div
                v-else
                class="msg-row"
                :class="item._mine ? 'me' : 'cs'"
              >
                <el-avatar :size="36" class="msg-avatar" v-if="!item._mine">
                  {{ (activeUsername || '用')[0] }}
                </el-avatar>
                <div class="msg-content">
                  <div class="msg-bubble">{{ item.content }}</div>
                </div>
                <el-avatar :size="36" class="msg-avatar me-avatar" v-if="item._mine">我</el-avatar>
              </div>
            </template>
            <el-empty v-if="!chatMessages.length && !chatLoading" description="暂无对话，从左侧选择会话" :image-size="60" />
          </div>
          <div class="chat-footer">
            <div class="footer-input-row">
              <el-input
                v-model="replyText"
                type="textarea"
                :rows="2"
                placeholder="输入回复内容..."
                resize="none"
                class="reply-textarea"
                @keydown.enter.exact.prevent="handleAdminReply"
              />
              <div class="footer-actions">
                <span class="reply-hint">按 Enter 发送</span>
                <el-button
                  type="primary"
                  :loading="replyLoading"
                  @click="handleAdminReply"
                  :disabled="!replyText.trim()"
                  class="reply-btn"
                >发送回复</el-button>
              </div>
            </div>
          </div>
        </el-card>
        <el-card shadow="never" v-else class="chat-card empty-chat">
          <el-empty description="选择一个会话开始回复" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useUserStore } from '@/stores/user'
import { adminAPI } from '@/api'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

const conversations = ref([])
const activeUserId = ref(null)
const activeUsername = ref('')
const chatMessages = ref([])
const replyText = ref('')
const convLoading = ref(false)
const chatLoading = ref(false)
const replyLoading = ref(false)
const adminChatBodyRef = ref(null)
let pollTimer = null

const totalUnread = computed(() => conversations.value.reduce((s, c) => s + (c.unread || 0), 0))

// 处理消息：插入时间分隔（从用户视角反转）
const displayChatMessages = computed(() => {
  const result = []
  const gap = 2 * 60 * 1000
  for (let i = 0; i < chatMessages.value.length; i++) {
    const m = chatMessages.value[i]
    if (i === 0 || (new Date(m.createdAt) - new Date(chatMessages.value[i - 1].createdAt)) > gap) {
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

function formatShort(t) {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  if (d.toDateString() === now.toDateString()) {
    return d.toLocaleString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  return d.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit' })
}

onMounted(() => {
  loadConversations()
  pollTimer = setInterval(loadConversations, 5000)
})

onBeforeUnmount(() => {
  if (pollTimer) clearInterval(pollTimer)
})

async function loadConversations() {
  try {
    const res = await adminAPI.messageConversations({ page: 1, limit: 50 })
    if (res.code === 200) {
      conversations.value = res.data?.list || []
    }
  } catch { /* ignore */ }
}

async function selectConv(conv) {
  activeUserId.value = conv.userId
  activeUsername.value = conv.username
  conv.unread = 0
  chatLoading.value = true
  try {
    const res = await adminAPI.messageDetail(conv.userId)
    if (res.code === 200) {
      chatMessages.value = res.data || []
      await nextTick()
      scrollChatBottom()
    }
  } catch { /* ignore */ }
  finally { chatLoading.value = false }
}

async function handleAdminReply() {
  const text = replyText.value.trim()
  if (!text || !activeUserId.value) return
  replyLoading.value = true
  const tempId = Date.now()
  chatMessages.value.push({
    id: tempId, content: text,
    fromUserId: userStore.user?.id, toUserId: activeUserId.value,
    createdAt: new Date().toISOString()
  })
  replyText.value = ''
  await nextTick()
  scrollChatBottom()
  try {
    const res = await adminAPI.messageReply(activeUserId.value, text)
    if (res.code === 200) {
      const idx = chatMessages.value.findIndex(m => m.id === tempId)
      if (idx > -1) chatMessages.value[idx] = res.data
    } else {
      chatMessages.value = chatMessages.value.filter(m => m.id !== tempId)
      ElMessage.error(res.message || '回复失败')
    }
  } catch {
    chatMessages.value = chatMessages.value.filter(m => m.id !== tempId)
    ElMessage.error('回复失败')
  } finally {
    replyLoading.value = false
    await nextTick()
    scrollChatBottom()
  }
}

function scrollChatBottom() {
  if (adminChatBodyRef.value) {
    adminChatBodyRef.value.scrollTop = adminChatBodyRef.value.scrollHeight
  }
}
</script>

<style scoped>
.admin-messages-page { animation: fadeIn 0.3s ease; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }

.msg-container { height: calc(100vh - 170px); }

/* ===== 左侧会话列表 ===== */
.conv-card {
  height: 100%; display: flex; flex-direction: column;
  border-radius: 12px; border: 1px solid #eee;
}
.conv-card :deep(.el-card__header) { padding: 16px 18px 12px; border-bottom: 1px solid #f0f0f0; }
.conv-card :deep(.el-card__body) { flex: 1; overflow-y: auto; padding: 0; }
.conv-card-title { display: flex; align-items: center; gap: 8px; font-weight: 600; font-size: 15px; color: #333; }

.conv-list { display: flex; flex-direction: column; }
.conv-item {
  display: flex; gap: 12px; padding: 14px 18px; cursor: pointer;
  border-bottom: 1px solid #f5f5f5; transition: background 0.2s;
}
.conv-item:hover { background: #fef7f2; }
.conv-item.active { background: #fef2f2; border-left: 3px solid #dc2626; padding-left: 15px; }

.conv-avatar { flex-shrink: 0; background: linear-gradient(135deg, #dc2626, #ef4444); color: #fff; font-weight: 600; }

.conv-info { flex: 1; min-width: 0; }
.conv-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
.conv-name { font-size: 14px; color: #333; }
.conv-time { font-size: 11px; color: #bbb; }
.conv-bottom { display: flex; justify-content: space-between; align-items: center; }
.conv-preview {
  font-size: 12px; color: #999; margin: 0;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 160px;
}

/* ===== 右侧聊天窗口 ===== */
.chat-card {
  height: 100%; display: flex; flex-direction: column;
  border-radius: 12px; border: 1px solid #eee;
}
.chat-card :deep(.el-card__header) { padding: 14px 20px; border-bottom: 1px solid #f0f0f0; }
.chat-card :deep(.el-card__body) { flex: 1; display: flex; flex-direction: column; padding: 0; }

.empty-chat {
  display: flex; align-items: center; justify-content: center;
}

.chat-header { display: flex; align-items: center; gap: 12px; }
.chat-header-avatar { background: linear-gradient(135deg, #dc2626, #ef4444); color: #fff; font-weight: 600; }
.chat-header-name { font-size: 16px; font-weight: 600; color: #333; }
.chat-header-sub { font-size: 12px; color: #999; margin-left: 6px; }

/* 消息区 */
.chat-body {
  flex: 1; overflow-y: auto; padding: 20px 24px;
  background: #f5f5f5;
}
.chat-body::-webkit-scrollbar { width: 5px; }
.chat-body::-webkit-scrollbar-thumb { background: #d0d5dd; border-radius: 3px; }

/* 系统消息 */
.sys-msg { display: flex; justify-content: center; padding: 8px 0; }
.sys-text {
  font-size: 12px; color: #999; background: #e8e8e8;
  padding: 4px 14px; border-radius: 10px;
}

/* 消息行 */
.msg-row { display: flex; align-items: flex-start; gap: 12px; margin-bottom: 16px; }
.msg-row.me { flex-direction: row-reverse; }

/* 头像 */
.msg-avatar { flex-shrink: 0; font-size: 14px; font-weight: 600; }
.msg-row.cs .msg-avatar { background: #fff; color: #dc2626; border: 2px solid #fecaca; }
.msg-row.me .msg-avatar { background: linear-gradient(135deg, #10b981, #059669); color: #fff; }

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
  background: #e8f8ee; color: #333; border-top-right-radius: 2px;
}

/* 输入区 */
.chat-footer {
  padding: 14px 20px 18px; background: #fff; border-top: 1px solid #eee;
}
.reply-textarea :deep(.el-textarea__inner) {
  border-radius: 12px; border-color: #e5e7eb; font-size: 14px;
  padding: 12px 16px; background: #ffffff;
}
.reply-textarea :deep(.el-textarea__inner:focus) {
  border-color: #10b981; background: #fff;
}
.footer-actions {
  display: flex; justify-content: space-between; align-items: center; margin-top: 10px;
}
.reply-hint { font-size: 12px; color: #aaa; }
.reply-btn {
  border-radius: 20px; background: #10b981; border-color: #10b981;
  padding: 8px 24px; font-size: 14px;
}
.reply-btn:hover { background: #059669; border-color: #059669; }
</style>
