<template>
  <div class="admin-cs-page">
    <div class="admin-cs-container">
      <!-- 左侧会话列表 -->
      <div class="conv-panel">
        <div class="conv-panel-header">
          <div class="panel-title">
            <el-icon><ChatDotRound /></el-icon>
            <span>咨询列表</span>
          </div>
          <el-badge :value="totalUnread" :hidden="!totalUnread" :max="99" class="panel-badge" />
        </div>
        <div class="conv-search">
          <el-input v-model="convSearch" placeholder="搜索用户..." size="small" clearable prefix-icon="Search" />
        </div>
        <div class="conv-list" v-loading="convLoading">
          <div
            v-for="conv in filteredConversations"
            :key="conv.userId"
            class="conv-item"
            :class="{ active: activeUserId === conv.userId }"
            @click="selectConv(conv)"
          >
            <div class="conv-avatar-wrap">
              <el-avatar :size="42" class="conv-avatar">
                {{ (conv.username || '用')[0] }}
              </el-avatar>
              <span class="conv-dot" v-if="conv.unread"></span>
            </div>
            <div class="conv-body">
              <div class="conv-top-row">
                <span class="conv-username">{{ conv.username }}</span>
                <span class="conv-time">{{ formatConvTime(conv.lastTime) }}</span>
              </div>
              <div class="conv-bottom-row">
                <span class="conv-preview">{{ conv.lastMessage || '暂无消息' }}</span>
                <el-badge :value="conv.unread" :hidden="!conv.unread" :max="99" />
              </div>
            </div>
          </div>
          <el-empty v-if="!filteredConversations.length && !convLoading" description="暂无用户咨询" :image-size="56" />
        </div>
      </div>

      <!-- 右侧聊天区 -->
      <div class="chat-panel" v-if="activeUserId">
        <!-- 聊天头部 -->
        <div class="chat-panel-header">
          <div class="chat-user-info">
            <el-avatar :size="40" class="chat-user-avatar">
              {{ (activeUsername || '用')[0] }}
            </el-avatar>
            <div class="chat-user-text">
              <div class="chat-user-name">{{ activeUsername }}</div>
              <div class="chat-user-id">UID: {{ String(activeUserId).padStart(3, '0') }}</div>
            </div>
          </div>
          <div class="chat-header-actions">
            <el-button size="small" text @click="insertQuickReply('感谢您的咨询，还有其他问题可以随时联系我们。')">
              快捷结束语
            </el-button>
          </div>
        </div>

        <!-- 消息区 -->
        <div class="chat-body" ref="adminChatBodyRef" v-loading="chatLoading">
          <div class="chat-date-tag" v-if="chatMessages.length">
            <span>{{ formatDate(chatMessages[0]?.createdAt) }}</span>
          </div>
          <template v-for="(item, i) in displayChatMessages" :key="item._key">
            <div class="chat-date-tag" v-if="item._isTime"><span>{{ item._timeLabel }}</span></div>
            <div
              v-else
              class="chat-msg"
              :class="item._mine ? 'msg-mine' : 'msg-other'"
            >
              <el-avatar :size="34" class="chat-msg-avatar" v-if="!item._mine">
                {{ (activeUsername || '用')[0] }}
              </el-avatar>
              <div class="chat-msg-body">
                <div class="chat-msg-name" v-if="!item._mine">{{ activeUsername }}</div>
                <div class="chat-msg-bubble">{{ item.content }}</div>
              </div>
              <el-avatar :size="34" class="chat-msg-avatar admin-avatar" v-if="item._mine" :src="userStore.user?.avatar">我</el-avatar>
            </div>
          </template>
          <el-empty v-if="!chatMessages.length && !chatLoading" description="暂无对话" :image-size="48" />
        </div>

        <!-- 快捷回复 -->
        <div class="quick-reply-bar" v-if="chatMessages.length">
          <span
            v-for="tpl in quickReplyTemplates"
            :key="tpl"
            class="quick-reply-tag"
            @click="insertQuickReply(tpl)"
          >{{ tpl }}</span>
        </div>

        <!-- 输入区 -->
        <div class="chat-footer">
          <div class="chat-input-row">
            <el-input
              v-model="replyText"
              type="textarea"
              :rows="2"
              placeholder="输入回复内容，按 Enter 发送..."
              resize="none"
              class="chat-input"
              maxlength="500"
              show-word-limit
              @keydown.enter.exact.prevent="handleAdminReply"
            />
            <el-button
              type="primary"
              :loading="replyLoading"
              :disabled="!replyText.trim()"
              @click="handleAdminReply"
              class="chat-send-btn"
              round
            >发送</el-button>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div class="chat-panel chat-empty-panel" v-else>
        <div class="empty-content">
          <el-icon :size="64" class="empty-icon"><ChatDotRound /></el-icon>
          <div class="empty-title">ShopNow 客服工作台</div>
          <div class="empty-desc">选择左侧会话开始回复用户咨询</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useUserStore } from '@/stores/user'
import { adminAPI } from '@/api'
import { ElMessage } from 'element-plus'
import { ChatDotRound } from '@element-plus/icons-vue'

const userStore = useUserStore()

const conversations = ref([])
const convSearch = ref('')
const activeUserId = ref(null)
const activeUsername = ref('')
const chatMessages = ref([])
const replyText = ref('')
const convLoading = ref(false)
const chatLoading = ref(false)
const replyLoading = ref(false)
const adminChatBodyRef = ref(null)
let pollTimer = null

const quickReplyTemplates = [
  '您好，请问有什么可以帮您？',
  '请稍等，我帮您查询一下。',
  '您的问题已收到，会尽快为您处理。',
  '感谢您的耐心等待。',
  '如有其他问题，随时联系我们。'
]

const totalUnread = computed(() => conversations.value.reduce((s, c) => s + (c.unread || 0), 0))
const filteredConversations = computed(() => {
  if (!convSearch.value) return conversations.value
  const kw = convSearch.value.toLowerCase()
  return conversations.value.filter(c => (c.username || '').toLowerCase().includes(kw))
})

const displayChatMessages = computed(() => {
  const result = []
  const gap = 3 * 60 * 1000
  for (let i = 0; i < chatMessages.value.length; i++) {
    const m = chatMessages.value[i]
    if (i === 0 || (new Date(m.createdAt) - new Date(chatMessages.value[i - 1].createdAt)) > gap) {
      result.push({
        _key: 't' + m.id,
        _isTime: true,
        _timeLabel: formatMsgTime(m.createdAt)
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
  return new Date(t).toLocaleString('zh-CN', { month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

function formatMsgTime(t) {
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

function formatConvTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  if (d.toDateString() === now.toDateString()) {
    return d.toLocaleString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  return d.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit' })
}

function insertQuickReply(text) {
  replyText.value = text
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
.admin-cs-page {
  height: calc(100vh - 80px);
  padding: 0;
}

.admin-cs-container {
  height: 100%;
  display: flex;
  background: #fff;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 1px 16px rgba(0,0,0,0.04);
}

/* ===== 左侧会话面板 ===== */
.conv-panel {
  width: 280px;
  flex-shrink: 0;
  border-right: 1px solid #eef0f4;
  display: flex;
  flex-direction: column;
  background: #fafbfc;
}
.conv-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px 12px;
}
.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
}
.panel-title .el-icon { color: #3b5998; font-size: 17px; }
.conv-search {
  padding: 0 14px 10px;
}
.conv-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}
.conv-list::-webkit-scrollbar { width: 4px; }
.conv-list::-webkit-scrollbar-thumb { background: #d0d5dd; border-radius: 2px; }

.conv-item {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.15s;
  border-left: 3px solid transparent;
}
.conv-item:hover { background: #f0f2f5; }
.conv-item.active {
  background: #eef1f8;
  border-left-color: #3b5998;
}
.conv-avatar-wrap {
  position: relative;
  flex-shrink: 0;
}
.conv-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-weight: 600;
  font-size: 16px;
}
.conv-dot {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 10px;
  height: 10px;
  background: #ef4444;
  border: 2px solid #fff;
  border-radius: 50%;
}
.conv-body {
  flex: 1;
  min-width: 0;
}
.conv-top-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 3px;
}
.conv-username {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a2e;
}
.conv-time {
  font-size: 11px;
  color: #b0b0b0;
}
.conv-bottom-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.conv-preview {
  font-size: 12px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 150px;
}

/* ===== 右侧聊天面板 ===== */
.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.chat-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border-bottom: 1px solid #eef0f4;
  flex-shrink: 0;
}
.chat-user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}
.chat-user-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-weight: 600;
}
.chat-user-name {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
}
.chat-user-id {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

/* 消息区 */
.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  background: #f7f8fa;
}
.chat-body::-webkit-scrollbar { width: 5px; }
.chat-body::-webkit-scrollbar-thumb { background: #d0d5dd; border-radius: 3px; }

.chat-date-tag {
  display: flex;
  justify-content: center;
  padding: 12px 0;
}
.chat-date-tag span {
  font-size: 12px;
  color: #b0b0b0;
  background: #ecedf0;
  padding: 4px 14px;
  border-radius: 4px;
}

/* 消息 */
.chat-msg {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 16px;
}
.chat-msg.msg-other {
  flex-direction: row;
}
.chat-msg.msg-mine {
  flex-direction: row-reverse;
}
.chat-msg-avatar {
  flex-shrink: 0;
  font-size: 13px;
  font-weight: 600;
}
.msg-other .chat-msg-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
}
.msg-mine .chat-msg-avatar,
.admin-avatar {
  background: linear-gradient(135deg, #10b981, #059669);
  color: #fff;
}
.chat-msg-body {
  max-width: 62%;
}
.chat-msg-name {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
  padding-left: 4px;
}
.chat-msg-bubble {
  padding: 10px 16px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
  border-radius: 4px;
}
.msg-mine .chat-msg-bubble {
  background: #fff;
  color: #333;
  border-top-left-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.msg-other .chat-msg-bubble {
  background: #3b5998;
  color: #fff;
  border-top-right-radius: 4px;
  box-shadow: 0 2px 8px rgba(59,89,152,0.18);
}

/* 快捷回复 */
.quick-reply-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 8px 20px;
  background: #fafbfc;
  border-top: 1px solid #f0f0f0;
}
.quick-reply-tag {
  font-size: 12px;
  color: #3b5998;
  background: #eef1f8;
  border: 1px solid #dce3f0;
  padding: 5px 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.15s;
  white-space: nowrap;
}
.quick-reply-tag:hover {
  background: #3b5998;
  color: #fff;
  border-color: #3b5998;
}

/* 输入区 */
.chat-footer {
  padding: 12px 20px 16px;
  background: #fff;
  border-top: 1px solid #eef0f4;
  flex-shrink: 0;
}
.chat-input-row {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}
.chat-input {
  flex: 1;
}
.chat-input :deep(.el-textarea__inner) {
  border-radius: 4px;
  border-color: #e2e5ea;
  font-size: 14px;
  padding: 10px 14px;
  background: #f9fafb;
  transition: all 0.2s;
}
.chat-input :deep(.el-textarea__inner:focus) {
  border-color: #3b5998;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(59,89,152,0.08);
}
.chat-send-btn {
  background: linear-gradient(135deg, #3b5998, #4a6db5);
  border: none;
  padding: 10px 24px;
  font-size: 14px;
  height: 40px;
  box-shadow: 0 3px 10px rgba(59,89,152,0.25);
}
.chat-send-btn:hover {
  background: linear-gradient(135deg, #344e86, #3b5998);
}

/* 空状态 */
.chat-empty-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f7f8fa;
}
.empty-content {
  text-align: center;
}
.empty-icon {
  color: #d0d5dd;
  margin-bottom: 16px;
}
.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: #666;
  margin-bottom: 8px;
}
.empty-desc {
  font-size: 13px;
  color: #aaa;
}
</style>
