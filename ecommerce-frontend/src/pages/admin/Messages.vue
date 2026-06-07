<template>
  <div class="admin-messages">
    <div class="msg-layout">
      <div class="conv-panel">
        <div class="conv-panel-header">
          <span><el-icon><Headset /></el-icon> 用户咨询</span>
          <el-badge v-if="totalUnread > 0" :value="totalUnread" />
        </div>
        <div class="conv-list" v-loading="loadingConvs">
          <div v-if="conversations.length === 0 && !loadingConvs" class="conv-empty">暂无用户咨询</div>
          <div
            v-for="conv in conversations" :key="conv.userId"
            class="conv-item" :class="{ 'conv-item--active': activeUserId === conv.userId }"
            @click="selectUser(conv.userId)"
          >
            <div class="conv-item-top">
              <span class="conv-uid">用户 #{{ conv.userId }}</span>
              <el-badge v-if="conv.unread > 0" :value="conv.unread" type="danger" />
            </div>
            <div class="conv-item-preview">{{ conv.lastMessage || '暂无消息' }}</div>
          </div>
        </div>
      </div>

      <div class="chat-panel">
        <template v-if="activeUserId">
          <div class="chat-panel-header">
            <span>用户 #{{ activeUserId }}</span>
          </div>
          <div class="chat-messages" ref="msgContainer" v-loading="loadingMsgs">
            <div v-if="activeMessages.length === 0 && !loadingMsgs" class="chat-empty">暂无消息</div>
            <div
              v-for="msg in activeMessages" :key="msg.id"
              class="chat-msg" :class="msg.from === adminId ? 'chat-msg--admin' : 'chat-msg--user'"
            >
              <div class="chat-msg-sender">{{ msg.from === adminId ? '我' : '用户 #' + msg.from }}</div>
              <div class="chat-msg-bubble">{{ msg.content }}</div>
              <div class="chat-msg-time">{{ formatTime(msg.time) }}</div>
            </div>
          </div>
          <div class="chat-input-area">
            <el-input
              v-model="replyInput" placeholder="输入回复..." @keyup.enter="reply"
              :disabled="sending" maxlength="500"
            >
              <template #append>
                <el-button :loading="sending" @click="reply" type="primary">发送</el-button>
              </template>
            </el-input>
          </div>
        </template>
        <div v-else class="chat-placeholder">
          <el-icon size="48"><Headset /></el-icon>
          <p>选择左侧用户开始对话</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onUnmounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { Headset } from '@element-plus/icons-vue';
import { chatAPI } from '@/api';

const adminId = 0;

const conversations = ref([]);
const activeUserId = ref(null);
const activeMessages = ref([]);
const replyInput = ref('');
const sending = ref(false);
const loadingConvs = ref(false);
const loadingMsgs = ref(false);
const msgContainer = ref(null);
let pollTimer = null;

const totalUnread = computed(() =>
  conversations.value.reduce((sum, c) => sum + (c.unread || 0), 0)
);

onMounted(() => {
  fetchConversations();
  pollTimer = setInterval(() => {
    fetchConversations();
    if (activeUserId.value) fetchMessages(activeUserId.value);
  }, 4000);
});

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer);
});

async function fetchConversations() {
  try {
    const res = await chatAPI.conversations();
    if (res.success) conversations.value = res.data;
  } catch {}
}

async function fetchMessages(otherId) {
  try {
    const res = await chatAPI.messages(otherId);
    if (res.success) {
      activeMessages.value = res.data;
      await nextTick();
      scrollBottom();
    }
  } catch {}
}

async function selectUser(userId) {
  activeUserId.value = userId;
  await fetchMessages(userId);
}

async function reply() {
  const text = replyInput.value.trim();
  if (!text) return;
  sending.value = true;
  try {
    const res = await chatAPI.send({ toUserId: activeUserId.value, content: text });
    if (res.success) {
      activeMessages.value.push(res.data);
      replyInput.value = '';
      await nextTick();
      scrollBottom();
    }
  } catch {
    // handled by interceptor
  } finally {
    sending.value = false;
  }
}

function scrollBottom() {
  if (msgContainer.value) {
    msgContainer.value.scrollTop = msgContainer.value.scrollHeight;
  }
}

function formatTime(ts) {
  if (!ts) return '';
  const d = new Date(ts);
  const pad = n => String(n).padStart(2, '0');
  return `${d.getMonth() + 1}/${d.getDate()} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}
</script>

<style scoped>
.admin-messages { height: calc(100vh - 110px); }
.msg-layout { display: flex; height: 100%; gap: 1px; background: #e2e8f0; border-radius: 8px; overflow: hidden; }

.conv-panel { width: 280px; background: #fff; display: flex; flex-direction: column; flex-shrink: 0; }
.conv-panel-header {
  padding: 14px 16px; font-size: 15px; font-weight: 600; border-bottom: 1px solid #e2e8f0;
  display: flex; align-items: center; gap: 8px; justify-content: space-between;
}
.conv-list { flex: 1; overflow-y: auto; }
.conv-empty { text-align: center; color: #94a3b8; padding: 40px 16px; font-size: 14px; }

.conv-item { padding: 12px 16px; cursor: pointer; border-bottom: 1px solid #f1f5f9; transition: background 0.15s; }
.conv-item:hover { background: #f8fafc; }
.conv-item--active { background: #fef2f2; border-left: 3px solid var(--color-primary); }
.conv-item-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 4px; }
.conv-uid { font-weight: 600; font-size: 14px; color: #1e293b; }
.conv-item-preview { font-size: 13px; color: #64748b; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.chat-panel { flex: 1; background: #fff; display: flex; flex-direction: column; }
.chat-panel-header {
  padding: 14px 16px; font-size: 15px; font-weight: 600; border-bottom: 1px solid #e2e8f0;
  background: #fafafa;
}
.chat-messages { flex: 1; overflow-y: auto; padding: 16px; display: flex; flex-direction: column; gap: 12px; background: #f8fafc; }
.chat-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: #94a3b8; }

.chat-msg { max-width: 70%; }
.chat-msg--admin { align-self: flex-end; }
.chat-msg--user { align-self: flex-start; }
.chat-msg-sender { font-size: 12px; color: #94a3b8; margin-bottom: 2px; }
.chat-msg--admin .chat-msg-sender { text-align: right; }
.chat-msg-bubble {
  padding: 10px 14px; border-radius: 14px; font-size: 14px; line-height: 1.5; word-break: break-word;
}
.chat-msg--admin .chat-msg-bubble { background: var(--color-primary-gradient); color: #fff; border-bottom-right-radius: 4px; }
.chat-msg--user .chat-msg-bubble { background: #fff; color: #334155; border-bottom-left-radius: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
.chat-msg-time { font-size: 11px; color: #94a3b8; margin-top: 3px; }
.chat-msg--admin .chat-msg-time { text-align: right; }

.chat-input-area { padding: 12px 16px; border-top: 1px solid #e2e8f0; }
.chat-input-area :deep(.el-input-group__append) { padding: 0; }
.chat-input-area :deep(.el-input-group__append .el-button) { border-radius: 0 4px 4px 0; height: 100%; border: none; }

.chat-placeholder { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #94a3b8; gap: 12px; }
</style>
