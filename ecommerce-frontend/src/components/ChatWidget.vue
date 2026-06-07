<template>
  <div class="chat-widget" :class="{ 'chat-widget--open': isOpen }">
    <div class="chat-trigger" @click="toggle">
      <el-badge :value="unreadCount" :hidden="!unreadCount" :max="99">
        <div class="chat-trigger-icon">
          <el-icon size="24"><Headset /></el-icon>
        </div>
      </el-badge>
      <span class="chat-trigger-label">客服</span>
    </div>

    <div class="chat-panel" v-show="isOpen">
      <div class="chat-panel-header">
        <span><el-icon><Headset /></el-icon> 在线客服</span>
        <el-button text size="small" @click="isOpen = false"><el-icon><Close /></el-icon></el-button>
      </div>

      <div class="chat-messages" ref="msgContainer">
        <div v-if="messages.length === 0" class="chat-empty">
          <el-icon size="40"><Headset /></el-icon>
          <p>您好，有什么可以帮您？</p>
        </div>
        <div v-for="msg in messages" :key="msg.id" class="chat-msg" :class="msg.from === userId ? 'chat-msg--me' : 'chat-msg--other'">
          <div class="chat-msg-bubble">{{ msg.content }}</div>
          <div class="chat-msg-time">{{ formatTime(msg.time) }}</div>
        </div>
      </div>

      <div class="chat-input-area">
        <el-input v-model="input" placeholder="输入消息..." @keyup.enter="send" :disabled="sending" maxlength="300" show-word-limit>
          <template #append>
            <el-button :loading="sending" @click="send" type="primary">
              <el-icon><Promotion /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onUnmounted, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { Headset, Close, Promotion } from '@element-plus/icons-vue';
import { chatAPI } from '@/api';
import { useUserStore } from '@/stores/user';

const userStore = useUserStore();
const userId = userStore.user?.id;
const isOpen = ref(false);
const input = ref('');
const sending = ref(false);
const messages = ref([]);
const unreadCount = ref(0);
const msgContainer = ref(null);
let pollTimer = null;

onMounted(() => {
  fetchUnread();
  pollTimer = setInterval(() => {
    if (isOpen.value && messages.value.length > 0) {
      fetchMessages();
    } else {
      fetchUnread();
    }
  }, 3000);
});

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer);
});

watch(isOpen, async (val) => {
  if (val) {
    await fetchMessages();
    scrollBottom();
  }
});

async function toggle() {
  isOpen.value = !isOpen.value;
}

async function fetchMessages() {
  try {
    const res = await chatAPI.messages(0);
    if (res.success) {
      messages.value = res.data;
      await nextTick();
      scrollBottom();
    }
  } catch {}
}

async function fetchUnread() {
  try {
    const res = await chatAPI.unread();
    if (res.success) unreadCount.value = res.data.count;
  } catch {}
}

async function send() {
  const text = input.value.trim();
  if (!text) return;
  sending.value = true;
  try {
    const res = await chatAPI.send({ toUserId: 0, content: text });
    if (res.success) {
      messages.value.push(res.data);
      input.value = '';
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
  return `${pad(d.getHours())}:${pad(d.getMinutes())}`;
}
</script>

<style scoped>
.chat-widget { position: fixed; right: 24px; bottom: 100px; z-index: 999; }

.chat-trigger {
  width: 52px; height: 52px; border-radius: 50%;
  background: var(--color-primary-gradient);
  color: #fff; display: flex; flex-direction: column; align-items: center;
  justify-content: center; cursor: pointer; box-shadow: 0 4px 20px rgba(220, 38, 38, 0.5);
  transition: all 0.3s; position: relative;
}
.chat-trigger:hover { transform: scale(1.08); box-shadow: 0 6px 28px rgba(220, 38, 38, 0.65); }
.chat-trigger-label { font-size: 9px; font-weight: 700; margin-top: -2px; letter-spacing: 0.04em; }

.chat-panel {
  position: absolute; right: 0; bottom: 64px; width: 360px; height: 460px;
  background: #fff; border-radius: 12px; box-shadow: 0 8px 40px rgba(0,0,0,0.18);
  display: flex; flex-direction: column; overflow: hidden;
}
.chat-panel-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 16px; background: var(--color-primary-gradient); color: #fff;
  font-size: 15px; font-weight: 600;
}
.chat-panel-header :deep(.el-button) { color: #fff; }

.chat-messages {
  flex: 1; overflow-y: auto; padding: 12px; display: flex; flex-direction: column; gap: 10px;
  background: #f8fafc;
}
.chat-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: #94a3b8; gap: 10px; }

.chat-msg { max-width: 80%; }
.chat-msg--me { align-self: flex-end; }
.chat-msg--other { align-self: flex-start; }
.chat-msg-bubble {
  padding: 10px 14px; border-radius: 16px; font-size: 14px; line-height: 1.5; word-break: break-word;
}
.chat-msg--me .chat-msg-bubble { background: var(--color-primary-gradient); color: #fff; border-bottom-right-radius: 4px; }
.chat-msg--other .chat-msg-bubble { background: #fff; color: #334155; border-bottom-left-radius: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
.chat-msg-time { font-size: 11px; color: #94a3b8; margin-top: 4px; }
.chat-msg--me .chat-msg-time { text-align: right; }

.chat-input-area { padding: 10px 12px; border-top: 1px solid #e2e8f0; }
.chat-input-area :deep(.el-input-group__append) { padding: 0; }
.chat-input-area :deep(.el-input-group__append .el-button) { border-radius: 0 4px 4px 0; height: 100%; border: none; }
</style>
