<template>
  <div class="feedbacks-page">
    <el-card shadow="never" class="page-card">
      <div class="page-toolbar">
        <div class="toolbar-title">反馈管理</div>
      </div>

      <el-table
        :data="tableData"
        v-loading="loading"
        style="width: 100%"
        stripe
        :header-cell-style="{ background: '#ffffff', color: '#475569', fontWeight: 600 }"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="用户" width="130" />
        <el-table-column prop="content" label="反馈内容" min-width="220" show-overflow-tooltip />
        <el-table-column prop="reply" label="回复内容" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.reply">{{ row.reply }}</span>
            <span v-else class="no-reply">暂未回复</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'replied' ? 'success' : 'warning'" size="small" effect="light">
              {{ row.status === 'replied' ? '已回复' : '待回复' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="170" />
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status !== 'replied'"
              type="primary"
              size="small"
              link
              @click="openReplyDialog(row)"
            >
              回复
            </el-button>
            <el-button
              v-else
              type="info"
              size="small"
              link
              @click="viewReply(row)"
            >
              查看回复
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无反馈数据" />
        </template>
      </el-table>

      <div class="page-pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @current-change="loadData"
          @size-change="loadData"
        />
      </div>
    </el-card>

    <!-- Reply Dialog -->
    <el-dialog
      v-model="replyDialogVisible"
      :title="replyDialogTitle"
      width="550px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <template v-if="replyMode === 'reply'">
        <div class="feedback-content-box">
          <div class="feedback-label">用户反馈：</div>
          <div class="feedback-text">{{ currentRow?.content }}</div>
        </div>
        <el-form ref="replyFormRef" :model="replyForm" :rules="replyRules" label-width="80px" style="margin-top: 16px">
          <el-form-item label="回复内容" prop="reply">
            <el-input v-model="replyForm.reply" type="textarea" :rows="5" placeholder="请输入回复内容" />
          </el-form-item>
        </el-form>
      </template>
      <template v-else>
        <div class="feedback-content-box">
          <div class="feedback-label">用户反馈：</div>
          <div class="feedback-text">{{ currentRow?.content }}</div>
        </div>
        <div class="feedback-content-box" style="margin-top: 12px; background: #f0fdf4; border-color: #bbf7d0;">
          <div class="feedback-label" style="color: #16a34a;">管理员回复：</div>
          <div class="feedback-text">{{ currentRow?.reply }}</div>
        </div>
      </template>
      <template #footer>
        <el-button @click="replyDialogVisible = false">关闭</el-button>
        <el-button v-if="replyMode === 'reply'" type="primary" :loading="replyLoading" @click="handleReply">提交回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminAPI } from '@/api'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const replyDialogVisible = ref(false)
const replyDialogTitle = ref('')
const replyLoading = ref(false)
const replyFormRef = ref(null)
const replyMode = ref('reply')
const currentRow = ref(null)

const replyForm = reactive({ reply: '' })

const replyRules = {
  reply: [{ required: true, message: '请输入回复内容', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await adminAPI.feedbacks({
      page: currentPage.value,
      limit: pageSize.value
    })
    if (res.code === 200) {
      tableData.value = res.data?.list || res.data || []
      total.value = res.meta?.total || res.data?.total || 0
    }
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function openReplyDialog(row) {
  currentRow.value = row
  replyMode.value = 'reply'
  replyDialogTitle.value = '回复反馈'
  replyForm.reply = ''
  replyDialogVisible.value = true
}

function viewReply(row) {
  currentRow.value = row
  replyMode.value = 'view'
  replyDialogTitle.value = '查看回复'
  replyDialogVisible.value = true
}

async function handleReply() {
  const valid = await replyFormRef.value.validate().catch(() => false)
  if (!valid) return

  replyLoading.value = true
  try {
    const res = await adminAPI.feedbackReply(currentRow.value.id, { reply: replyForm.reply })
    if (res.code === 200) {
      ElMessage.success('回复成功')
      replyDialogVisible.value = false
      loadData()
    }
  } catch (e) {
    // handled by interceptor
  } finally {
    replyLoading.value = false
  }
}

onMounted(() => { loadData() })
</script>

<style scoped>
.feedbacks-page {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-card {
  border-radius: 12px;
  border: 1px solid var(--color-border-light);
}

.page-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.toolbar-title {
  font-size: 1.15rem;
  font-weight: 600;
  color: var(--color-text-primary);
}

.page-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.no-reply {
  color: var(--color-text-placeholder);
  font-style: italic;
}

.feedback-content-box {
  background: var(--color-border-light);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 14px 16px;
}

.feedback-label {
  font-weight: 600;
  color: var(--color-text-body);
  font-size: 0.9rem;
  margin-bottom: 6px;
}

.feedback-text {
  color: var(--color-text-body);
  font-size: 0.9rem;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
