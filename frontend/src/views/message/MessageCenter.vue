<template>
  <section class="message-page">
    <article class="toolbar-card">
      <div>
        <h1>消息中心</h1>
        <p>未读 {{ unreadCount }} 条，订单、评价、举报处理结果会集中显示在这里。</p>
      </div>
      <div class="toolbar-actions">
        <el-select v-model="typeFilter" @change="reload">
          <el-option label="全部类型" value="all" />
          <el-option v-for="type in filterTypes" :key="type" :label="messageTypeLabels[type]" :value="type" />
        </el-select>
        <el-select v-model="readFilter" @change="reload">
          <el-option label="全部状态" value="all" />
          <el-option label="只看未读" value="unread" />
          <el-option label="只看已读" value="read" />
        </el-select>
        <el-button type="primary" :disabled="!unreadCount" @click="markAllRead">全部已读</el-button>
      </div>
    </article>

    <section class="content-grid">
      <article class="list-card">
        <el-skeleton :loading="loading" animated :rows="6">
          <template #default>
            <el-empty v-if="!messages.length" description="暂无消息" />
            <div v-else class="message-list">
              <article
                v-for="message in messages"
                :key="message.messageId"
                class="message-row"
                :class="{ unread: !message.read }"
              >
                <div class="message-main">
                  <div class="row-title">
                    <el-tag size="small" :type="messageTagType(message.type)">
                      {{ messageTypeLabels[message.type] }}
                    </el-tag>
                    <h2>{{ message.title }}</h2>
                    <span v-if="!message.read">未读</span>
                  </div>
                  <p>{{ message.content || '无详细内容' }}</p>
                  <time>{{ formatTime(message.createdAt) }}</time>
                </div>
                <div class="row-actions">
                  <el-button v-if="!message.read" @click="readOne(message.messageId)">设为已读</el-button>
                  <el-button type="danger" plain @click="removeOne(message.messageId)">删除</el-button>
                </div>
              </article>
            </div>
          </template>
        </el-skeleton>

        <div class="pager">
          <span>共 {{ total }} 条</span>
          <el-pagination
            v-model:current-page="page"
            layout="prev, pager, next"
            :page-size="pageSize"
            :total="total"
            @current-change="loadMessages"
          />
        </div>
      </article>

      <article class="notice-card">
        <header>
          <h2>最新公告</h2>
          <RouterLink to="/">返回首页</RouterLink>
        </header>
        <el-empty v-if="!notices.length" description="暂无公告" />
        <div v-else class="notice-list">
          <section v-for="notice in notices" :key="notice.noticeId" class="notice-item">
            <h3>{{ notice.title }}</h3>
            <p>{{ notice.content }}</p>
            <small>{{ notice.publisherName }} · {{ formatTime(notice.createdAt) }}</small>
          </section>
        </div>
      </article>
    </section>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'

import {
  deleteMessage,
  getLatestNotices,
  getMessages,
  getUnreadMessageCount,
  markAllMessagesRead,
  markMessageRead,
} from '@/api/message'
import { messageTypeLabels } from '@/types'
import type { EntityId, MessageDTO, MessageType, NoticeDTO } from '@/types'

const filterTypes: MessageType[] = ['SYSTEM', 'TASK', 'ORDER', 'REVIEW', 'REPORT']

const loading = ref(false)
const messages = ref<MessageDTO[]>([])
const notices = ref<NoticeDTO[]>([])
const unreadCount = ref(0)
const total = ref(0)
const page = ref(1)
const pageSize = 10
const typeFilter = ref<'all' | MessageType>('all')
const readFilter = ref<'all' | 'unread' | 'read'>('all')
const messageUnreadUpdatedEvent = 'campushub:message-unread-updated'

const formatTime = (value: string) => new Date(value).toLocaleString('zh-CN', { hour12: false })
const messageTagType = (type: MessageType) => {
  if (type === 'ORDER') return 'success'
  if (type === 'REVIEW') return 'warning'
  if (type === 'REPORT') return 'danger'
  return 'info'
}

const loadUnreadCount = async () => {
  unreadCount.value = (await getUnreadMessageCount()).count
}

const notifyUnreadCountUpdated = () => {
  window.dispatchEvent(new CustomEvent(messageUnreadUpdatedEvent))
}

const loadMessages = async () => {
  loading.value = true
  try {
    const result = await getMessages({
      page: page.value,
      size: pageSize,
      type: typeFilter.value === 'all' ? undefined : typeFilter.value,
      unread: readFilter.value === 'all' ? undefined : readFilter.value === 'unread',
    })
    messages.value = result.records
    total.value = result.total
    await loadUnreadCount()
  } finally {
    loading.value = false
  }
}

const loadNotices = async () => {
  notices.value = await getLatestNotices(5)
}

const reload = () => {
  page.value = 1
  loadMessages()
}

const readOne = async (messageId: EntityId) => {
  await markMessageRead(messageId)
  ElMessage.success('已标记为已读')
  await loadMessages()
  notifyUnreadCountUpdated()
}

const markAllRead = async () => {
  await markAllMessagesRead()
  ElMessage.success('全部消息已读')
  await loadMessages()
  notifyUnreadCountUpdated()
}

const removeOne = async (messageId: EntityId) => {
  await deleteMessage(messageId)
  ElMessage.success('消息已删除')
  await loadMessages()
  notifyUnreadCountUpdated()
}

onMounted(() => {
  loadMessages()
  loadNotices()
})
</script>

<style scoped>
.message-page {
  display: grid;
  height: calc(100vh - 122px);
  grid-template-rows: auto minmax(0, 1fr);
  gap: 18px;
  min-height: 0;
}

.toolbar-card,
.list-card,
.notice-card {
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.07);
}

.toolbar-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 22px;
}

.toolbar-card h1,
.toolbar-card p,
.notice-card h2,
.notice-item h3,
.notice-item p,
.message-main h2,
.message-main p {
  margin: 0;
}

.toolbar-card h1 {
  color: #111827;
  font-size: 26px;
}

.toolbar-card p {
  margin-top: 6px;
  color: #64748b;
}

.toolbar-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-end;
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: 18px;
  min-height: 0;
}

.list-card,
.notice-card {
  display: flex;
  min-height: 0;
  flex-direction: column;
  padding: 20px;
}

.list-card :deep(.el-skeleton),
.list-card :deep(.el-skeleton__content) {
  display: flex;
  min-height: 0;
  flex: 1;
  flex-direction: column;
}

.message-list,
.notice-list {
  min-height: 0;
  overflow-y: auto;
  padding-right: 8px;
  scrollbar-gutter: stable;
}

.message-list {
  flex: 1;
}

.message-list::-webkit-scrollbar,
.notice-list::-webkit-scrollbar {
  width: 8px;
}

.message-list::-webkit-scrollbar-thumb,
.notice-list::-webkit-scrollbar-thumb {
  border-radius: 999px;
  background: #cbd5e1;
}

.message-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 160px;
  gap: 16px;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #edf2f7;
}

.message-row.unread .message-main h2 {
  color: #0f62d6;
}

.row-title {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.row-title h2 {
  color: #111827;
  font-size: 17px;
}

.row-title span {
  color: #e11d48;
  font-size: 12px;
  font-weight: 700;
}

.message-main p {
  margin-top: 8px;
  color: #475569;
  line-height: 1.55;
}

.message-main time {
  display: block;
  margin-top: 8px;
  color: #94a3b8;
  font-size: 12px;
}

.row-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.pager {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 18px;
  color: #64748b;
  flex-shrink: 0;
}

.notice-card header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.notice-card header a {
  color: #1268ed;
  font-size: 13px;
  text-decoration: none;
}

.notice-list {
  display: grid;
  gap: 12px;
  flex: 1;
  align-content: start;
}

.notice-item {
  padding-bottom: 12px;
  border-bottom: 1px solid #edf2f7;
}

.notice-item h3 {
  color: #111827;
  font-size: 16px;
}

.notice-item p {
  margin-top: 8px;
  color: #475569;
  line-height: 1.55;
  white-space: pre-wrap;
}

.notice-item small {
  display: block;
  margin-top: 8px;
  color: #94a3b8;
}

@media (max-width: 1100px) {
  .message-page {
    height: auto;
  }

  .toolbar-card,
  .content-grid,
  .message-row,
  .pager {
    grid-template-columns: 1fr;
  }

  .toolbar-card {
    align-items: flex-start;
    flex-direction: column;
  }

  .row-actions {
    justify-content: flex-start;
  }

  .message-list,
  .notice-list {
    max-height: 520px;
  }
}
</style>
