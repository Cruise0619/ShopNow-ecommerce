<template>
  <div class="fb-wrapper">
    <div class="fb-trigger" @click="showDialog = true" :class="{ 'fb-trigger--hidden': showDialog }">
      <el-icon size="22"><EditPen /></el-icon>
      <span class="fb-label">反馈</span>
      <span class="fb-pulse"></span>
    </div>

    <el-dialog v-model="showDialog" title="意见反馈" width="460px" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="form" label-position="top">
        <el-form-item label="请描述您的意见或问题">
          <el-input v-model="form.content" type="textarea" :rows="5" maxlength="500" show-word-limit placeholder="您的反馈将帮助我们持续改进..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">提交反馈</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import { EditPen } from '@element-plus/icons-vue';
import { feedbackAPI } from '@/api';

const showDialog = ref(false);
const submitting = ref(false);
const form = reactive({ content: '' });

async function submit() {
  if (!form.content.trim()) return ElMessage.warning('请输入反馈内容');
  submitting.value = true;
  try {
    await feedbackAPI.create({ content: form.content });
    ElMessage.success('感谢您的反馈！');
    form.content = '';
    showDialog.value = false;
  } catch {
    // error handled by interceptor
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped>
.fb-wrapper { position: fixed; right: 24px; bottom: 180px; z-index: 999; }

.fb-trigger {
  width: 52px; height: 52px; border-radius: 50%;
  background: linear-gradient(135deg, #f59e0b, #ef4444);
  color: #fff; display: flex; flex-direction: column; align-items: center;
  justify-content: center; cursor: pointer; box-shadow: 0 4px 20px rgba(245, 158, 11, 0.45);
  transition: all 0.3s; position: relative;
}
.fb-trigger:hover { transform: scale(1.08); box-shadow: 0 6px 28px rgba(245, 158, 11, 0.6); }
.fb-trigger--hidden { opacity: 0; pointer-events: none; transform: scale(0.8); }
.fb-label { font-size: 9px; font-weight: 700; margin-top: -2px; letter-spacing: 0.04em; }
.fb-pulse {
  position: absolute; inset: -4px; border-radius: 50%;
  border: 2px solid rgba(245, 158, 11, 0.5); animation: fb-pulse 2s ease-out infinite;
}
@keyframes fb-pulse {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(1.4); opacity: 0; }
}
</style>
