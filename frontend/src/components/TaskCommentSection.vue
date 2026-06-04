<template>
  <section class="comments">
    <header>
      <div>
        <h2>评论区</h2>
        <p>公开讨论需求细节，便于参与者提前沟通。</p>
      </div>
      <span>{{ comments.length }} 条评论</span>
    </header>
    <div v-if="authStore.isAuthenticated" class="composer">
      <el-input v-model="draft" type="textarea" :rows="2" maxlength="500" show-word-limit placeholder="写下你的评论" />
      <el-button type="primary" :loading="submitting" @click="submit">发表评论</el-button>
    </div>
    <el-empty v-if="!comments.length" description="暂无评论，来留下第一条吧" />
    <div v-else class="comment-list">
      <article v-for="comment in comments" :key="comment.commentId">
        <el-avatar :size="38" :src="comment.authorAvatarUrl || undefined">{{ comment.authorName.slice(0, 1) }}</el-avatar>
        <div>
          <strong>{{ comment.authorName }}</strong>
          <time>{{ formatDate(comment.createdAt) }}</time>
          <p>{{ comment.content }}</p>
        </div>
        <el-button v-if="comment.authorId === authStore.user?.userId" text type="danger" @click="remove(comment.commentId)">
          删除
        </el-button>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createTaskComment, deleteTaskComment, getTaskComments } from '@/api/task'
import { useAuthStore } from '@/stores'
import type { EntityId, TaskCommentDTO } from '@/types'

const props = defineProps<{ taskId: EntityId }>()
const authStore = useAuthStore()
const comments = ref<TaskCommentDTO[]>([])
const draft = ref('')
const submitting = ref(false)
const load = async () => { comments.value = await getTaskComments(props.taskId) }
const submit = async () => {
  if (!draft.value.trim()) return
  submitting.value = true
  try {
    comments.value = await createTaskComment(props.taskId, draft.value)
    draft.value = ''
    ElMessage.success('评论已发布')
  } finally { submitting.value = false }
}
const remove = async (commentId: EntityId) => {
  await deleteTaskComment(props.taskId, commentId)
  ElMessage.success('评论已删除')
  await load()
}
const formatDate = (value: string) => new Date(value).toLocaleString('zh-CN')
onMounted(load)
</script>

<style scoped>
.comments { display:grid; box-sizing:border-box; height:430px; grid-template-rows:auto auto minmax(0,1fr); gap:18px; padding:22px; border:1px solid #e7edf7; border-radius:8px; background:#fff; box-shadow:0 16px 32px rgba(15,23,42,.07); }
header,.composer,article { display:flex; gap:14px; align-items:flex-start; justify-content:space-between; }
h2,p { margin:0; }
header p,time { color:#94a3b8; font-size:13px; }
.composer { align-items:flex-end; }
.composer :deep(.el-textarea) { flex:1; }
.comment-list { display:grid; min-height:0; align-content:start; gap:14px; overflow-y:auto; padding-right:8px; scrollbar-gutter:stable; }
.comment-list::-webkit-scrollbar { width:8px; }
.comment-list::-webkit-scrollbar-thumb { border-radius:999px; background:#cbd5e1; }
article { padding-top:14px; border-top:1px solid #edf2f7; }
article > div { flex:1; }
article p { margin-top:8px; color:#475569; white-space:pre-wrap; }
time { margin-left:10px; }
</style>
