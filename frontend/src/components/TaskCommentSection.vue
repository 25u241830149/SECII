<template>
  <section class="comments">
    <header class="comments-header">
      <div>
        <h2>评论区</h2>
        <p>公开讨论需求细节，便于参与者提前沟通。</p>
      </div>
      <strong>共 {{ totalCount }} 条评论</strong>
    </header>

    <div class="comment-toolbar">
      <div class="sort-tabs" aria-label="评论排序">
        <button :class="{ active: sortMode === 'likes' }" type="button" @click="sortMode = 'likes'">
          按赞数
        </button>
        <button :class="{ active: sortMode === 'time' }" type="button" @click="sortMode = 'time'">
          按时间
        </button>
      </div>
    </div>

    <div v-if="authStore.isAuthenticated" class="composer">
      <el-avatar :size="42" :src="currentAvatar || undefined" class="composer-avatar">
        {{ currentFallback }}
      </el-avatar>
      <div class="composer-main">
        <div v-if="replyTarget" class="reply-target">
          <span>回复 @{{ replyTarget.authorName }}</span>
          <button type="button" @click="replyTarget = null">取消</button>
        </div>
        <el-input
          v-model="draft"
          type="textarea"
          :rows="2"
          maxlength="500"
          show-word-limit
          :placeholder="replyTarget ? `回复 @${replyTarget.authorName}` : '友善评论，传递温暖'"
          @keyup.ctrl.enter="submit"
        />
      </div>
      <el-button type="primary" :loading="submitting" @click="submit">
        {{ replyTarget ? '回复' : '发表评论' }}
      </el-button>
    </div>
    <div v-else class="login-tip">登录后可以参与评论、回复和点赞。</div>

    <el-empty v-if="!loading && !comments.length" description="暂无评论，来留下第一条吧" />
    <div v-else v-loading="loading" class="comment-list">
      <article v-for="comment in commentThreads" :key="comment.commentId" class="comment-thread">
        <div class="comment-item">
          <el-avatar :size="42" :src="avatarUrl(comment.authorAvatarUrl) || undefined" class="comment-avatar">
            {{ fallbackName(comment.authorName) }}
          </el-avatar>
          <div class="comment-body">
            <div class="comment-meta">
              <div>
                <strong>{{ comment.authorName }}</strong>
                <time>{{ formatDate(comment.createdAt) }}</time>
              </div>
              <el-dropdown trigger="click" @command="(command: string) => handleAction(command, comment)">
                <button class="more-button" type="button" aria-label="评论操作">
                  <el-icon><MoreFilled /></el-icon>
                </button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="reply">回复</el-dropdown-item>
                    <el-dropdown-item command="copy">复制</el-dropdown-item>
                    <el-dropdown-item command="report" divided>举报</el-dropdown-item>
                    <el-dropdown-item v-if="canDelete(comment)" command="delete" divided>删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
            <p class="comment-content">{{ comment.content }}</p>
            <div class="comment-actions">
              <button :class="{ liked: comment.likedByMe }" type="button" @click="toggleLike(comment)">
                <el-icon><Pointer /></el-icon>
                <span>{{ comment.likeCount || '赞' }}</span>
              </button>
              <button type="button" @click="startReply(comment)">
                <el-icon><ChatDotRound /></el-icon>
                <span>回复</span>
              </button>
            </div>
          </div>
        </div>

        <div v-if="comment.replies.length" class="reply-list">
          <div v-for="reply in comment.replies" :key="reply.commentId" class="comment-item reply-item">
            <el-avatar :size="34" :src="avatarUrl(reply.authorAvatarUrl) || undefined" class="comment-avatar">
              {{ fallbackName(reply.authorName) }}
            </el-avatar>
            <div class="comment-body">
              <div class="comment-meta">
                <div>
                  <strong>{{ reply.authorName }}</strong>
                  <span v-if="reply.replyToUserName" class="reply-to">回复 @{{ reply.replyToUserName }}</span>
                  <time>{{ formatDate(reply.createdAt) }}</time>
                </div>
                <el-dropdown trigger="click" @command="(command: string) => handleAction(command, reply)">
                  <button class="more-button" type="button" aria-label="回复操作">
                    <el-icon><MoreFilled /></el-icon>
                  </button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="reply">回复</el-dropdown-item>
                      <el-dropdown-item command="copy">复制</el-dropdown-item>
                      <el-dropdown-item command="report" divided>举报</el-dropdown-item>
                      <el-dropdown-item v-if="canDelete(reply)" command="delete" divided>删除</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
              <p class="comment-content">{{ reply.content }}</p>
              <div class="comment-actions">
                <button :class="{ liked: reply.likedByMe }" type="button" @click="toggleLike(reply)">
                  <el-icon><Pointer /></el-icon>
                  <span>{{ reply.likeCount || '赞' }}</span>
                </button>
                <button type="button" @click="startReply(reply)">
                  <el-icon><ChatDotRound /></el-icon>
                  <span>回复</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound, MoreFilled, Pointer } from '@element-plus/icons-vue'

import { createReport } from '@/api/report'
import {
  createTaskComment,
  deleteTaskComment,
  getTaskComments,
  likeTaskComment,
  unlikeTaskComment,
  type TaskCommentSort,
} from '@/api/task'
import { useAuthStore } from '@/stores'
import { resolveAssetUrl } from '@/utils/asset'
import type { EntityId, TaskCommentDTO } from '@/types'

interface CommentThread extends TaskCommentDTO {
  replies: TaskCommentDTO[]
}

const props = defineProps<{ taskId: EntityId }>()
const authStore = useAuthStore()

const comments = ref<TaskCommentDTO[]>([])
const draft = ref('')
const loading = ref(false)
const submitting = ref(false)
const sortMode = ref<TaskCommentSort>('time')
const replyTarget = ref<TaskCommentDTO | null>(null)

const totalCount = computed(() => comments.value.length)
const currentAvatar = computed(() => resolveAssetUrl(authStore.user?.avatarUrl || ''))
const currentFallback = computed(() => fallbackName(authStore.user?.nickname || '我'))

const commentThreads = computed<CommentThread[]>(() => {
  const roots = new Map<EntityId, CommentThread>()
  const pendingReplies: TaskCommentDTO[] = []

  comments.value.forEach((comment) => {
    if (!comment.parentCommentId) {
      roots.set(comment.commentId, { ...comment, replies: [] })
    } else {
      pendingReplies.push(comment)
    }
  })

  pendingReplies.forEach((reply) => {
    const parent = roots.get(reply.parentCommentId as EntityId)
    if (parent) {
      parent.replies.push(reply)
    } else {
      roots.set(reply.commentId, { ...reply, parentCommentId: null, replies: [] })
    }
  })

  return Array.from(roots.values()).map((root) => ({
    ...root,
    replies: [...root.replies].sort(compareByTimeAsc),
  }))
})

const load = async () => {
  loading.value = true
  try {
    comments.value = await getTaskComments(props.taskId, sortMode.value)
  } finally {
    loading.value = false
  }
}

const submit = async () => {
  const content = draft.value.trim()
  if (!content) return

  submitting.value = true
  try {
    await createTaskComment(props.taskId, {
      content,
      parentCommentId: replyTarget.value?.parentCommentId || replyTarget.value?.commentId || null,
      replyToUserId: replyTarget.value?.authorId || null,
    })
    draft.value = ''
    replyTarget.value = null
    ElMessage.success('评论已发布')
    await load()
  } finally {
    submitting.value = false
  }
}

const startReply = (comment: TaskCommentDTO) => {
  if (!ensureLogin('登录后可以回复评论')) return
  replyTarget.value = comment
}

const toggleLike = async (comment: TaskCommentDTO) => {
  if (!ensureLogin('登录后可以点赞评论')) return
  if (comment.likedByMe) {
    await unlikeTaskComment(props.taskId, comment.commentId)
    ElMessage.success('已取消点赞')
  } else {
    await likeTaskComment(props.taskId, comment.commentId)
    ElMessage.success('已点赞')
  }
  await load()
}

const handleAction = async (command: string, comment: TaskCommentDTO) => {
  if (command === 'reply') {
    startReply(comment)
    return
  }
  if (command === 'copy') {
    await copyComment(comment)
    return
  }
  if (command === 'report') {
    await reportComment(comment)
    return
  }
  if (command === 'delete') {
    await remove(comment.commentId)
  }
}

const remove = async (commentId: EntityId) => {
  try {
    await ElMessageBox.confirm('删除后评论将不再展示，确定继续吗？', '删除评论', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  await deleteTaskComment(props.taskId, commentId)
  ElMessage.success('评论已删除')
  await load()
}

const copyComment = async (comment: TaskCommentDTO) => {
  try {
    await navigator.clipboard.writeText(comment.content)
    ElMessage.success('评论内容已复制')
  } catch {
    ElMessage.warning('复制失败，请手动选择文本复制')
  }
}

const reportComment = async (comment: TaskCommentDTO) => {
  if (!ensureLogin('登录后可以举报评论')) return
  let value = ''
  try {
    const result = await ElMessageBox.prompt('请填写举报原因，管理员会在后台处理。', '举报评论', {
      confirmButtonText: '提交举报',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：不友善、广告、无关内容等',
      inputValidator: (value) => Boolean(value && value.trim()),
      inputErrorMessage: '请填写举报原因',
    })
    value = result.value
  } catch {
    return
  }
  await createReport({
    targetType: 'COMMENT',
    targetId: comment.commentId,
    targetUserId: comment.authorId,
    reason: value.trim(),
  })
  ElMessage.success('举报已提交')
}

const canDelete = (comment: TaskCommentDTO) => comment.authorId === authStore.user?.userId
const avatarUrl = (value?: string | null) => resolveAssetUrl(value || '')
const fallbackName = (value: string) => (value || '评').slice(0, 1).toUpperCase()
const compareByTimeAsc = (left: TaskCommentDTO, right: TaskCommentDTO) =>
  new Date(left.createdAt).getTime() - new Date(right.createdAt).getTime()

const formatDate = (value: string) => {
  const date = new Date(value)
  return Number.isNaN(date.getTime())
    ? value
    : date.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

const ensureLogin = (message: string) => {
  if (authStore.isAuthenticated) return true
  ElMessage.warning(message)
  return false
}

watch(() => props.taskId, () => {
  replyTarget.value = null
  load()
})
watch(sortMode, load)
onMounted(load)
</script>

<style scoped>
.comments {
  display: grid;
  box-sizing: border-box;
  height: 430px;
  grid-template-rows: auto auto auto minmax(0, 1fr);
  gap: 14px;
  padding: 22px;
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 16px 32px rgba(15, 23, 42, .07);
}

.comments-header,
.comment-toolbar,
.composer,
.comment-item,
.comment-meta,
.comment-actions {
  display: flex;
}

.comments-header {
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

h2,
p {
  margin: 0;
}

.comments-header h2 {
  font-size: 26px;
  color: #0f172a;
}

.comments-header p,
.comments-header strong,
time,
.reply-to,
.login-tip {
  color: #64748b;
}

.comments-header p {
  margin-top: 4px;
  font-size: 14px;
}

.comments-header strong {
  white-space: nowrap;
  font-size: 16px;
}

.comment-toolbar {
  justify-content: flex-end;
}

.sort-tabs {
  display: inline-flex;
  padding: 4px;
  border-radius: 999px;
  background: #f1f5f9;
}

.sort-tabs button {
  min-width: 76px;
  border: 0;
  border-radius: 999px;
  padding: 8px 14px;
  background: transparent;
  color: #475569;
  font: inherit;
  cursor: pointer;
}

.sort-tabs button.active {
  background: #fff;
  color: #0f172a;
  font-weight: 700;
  box-shadow: 0 6px 16px rgba(15, 23, 42, .08);
}

.composer {
  align-items: flex-end;
  gap: 12px;
  padding: 12px;
  border-radius: 18px;
  background: #f8fafc;
}

.composer-avatar {
  flex: 0 0 auto;
}

.composer-main {
  flex: 1;
  min-width: 0;
}

.composer :deep(.el-textarea__inner) {
  border-radius: 16px;
  background: #fff;
}

.reply-target {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
  color: #64748b;
  font-size: 13px;
}

.reply-target button,
.more-button,
.comment-actions button {
  border: 0;
  background: transparent;
  font: inherit;
  cursor: pointer;
}

.reply-target button {
  color: #2563eb;
}

.login-tip {
  padding: 12px 14px;
  border-radius: 16px;
  background: #f8fafc;
  font-size: 14px;
}

.comment-list {
  display: grid;
  min-height: 0;
  align-content: start;
  gap: 16px;
  overflow-y: auto;
  padding-right: 8px;
  scrollbar-gutter: stable;
}

.comment-list::-webkit-scrollbar {
  width: 8px;
}

.comment-list::-webkit-scrollbar-thumb {
  border-radius: 999px;
  background: #cbd5e1;
}

.comment-thread {
  display: grid;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #edf2f7;
}

.comment-item {
  align-items: flex-start;
  gap: 12px;
}

.comment-avatar {
  flex: 0 0 auto;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-meta {
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.comment-meta strong {
  margin-right: 8px;
  color: #334155;
}

time {
  font-size: 13px;
}

.reply-to {
  margin-right: 8px;
  font-size: 13px;
}

.more-button {
  display: grid;
  width: 28px;
  height: 28px;
  place-items: center;
  border-radius: 999px;
  color: #94a3b8;
}

.more-button:hover {
  background: #f1f5f9;
  color: #334155;
}

.comment-content {
  margin-top: 8px;
  color: #334155;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}

.comment-actions {
  align-items: center;
  gap: 18px;
  margin-top: 8px;
}

.comment-actions button {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 0;
  color: #64748b;
}

.comment-actions button:hover,
.comment-actions button.liked {
  color: #2563eb;
}

.reply-list {
  display: grid;
  gap: 12px;
  margin-left: 54px;
  padding: 12px;
  border-radius: 16px;
  background: #f8fafc;
}

.reply-item + .reply-item {
  padding-top: 12px;
  border-top: 1px solid #e7edf7;
}

@media (max-width: 900px) {
  .comments {
    height: 520px;
  }

  .composer {
    align-items: stretch;
    flex-wrap: wrap;
  }

  .composer .el-button {
    margin-left: 54px;
  }
}
</style>
