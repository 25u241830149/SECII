<template>
  <section class="page-surface">
    <div class="section-heading">
      <div>
        <h1 class="section-title">消息中心</h1>
        <p class="section-subtitle">统一承接站内消息、公告和订单聊天，和右上角消息铃铛联动。</p>
      </div>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="站内消息" name="messages" />
        <el-tab-pane label="公告提醒" name="notices" />
        <el-tab-pane label="最近聊天" name="chats" />
      </el-tabs>
    </div>

    <div v-if="activeTab === 'messages'" class="message-list">
      <div v-for="item in mockMessages" :key="item.messageId" class="message-item">
        <div class="row-between">
          <strong>{{ item.title }}</strong>
          <el-tag :type="item.isRead ? 'info' : 'danger'" effect="plain">{{ item.isRead ? '已读' : '未读' }}</el-tag>
        </div>
        <p class="muted">{{ item.content }}</p>
      </div>
    </div>

    <div v-else-if="activeTab === 'notices'" class="notice-list">
      <div v-for="item in messageStore.notices" :key="item.noticeId" class="notice-card">
        <div class="row-between">
          <strong>{{ item.title }}</strong>
          <el-tag :type="item.isRead ? 'info' : 'warning'" effect="plain">{{ item.isRead ? '已读' : '新公告' }}</el-tag>
        </div>
        <p class="muted">{{ item.content }}</p>
      </div>
    </div>

    <div v-else class="chat-list">
      <div v-for="chat in messageStore.recentChats" :key="chat.messageId" class="chat-item">
        <div class="row-between">
          <strong>{{ chat.senderName }}</strong>
          <span class="muted">{{ chat.sentAt }}</span>
        </div>
        <p class="muted">{{ chat.content }}</p>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { mockMessages, mockNotices } from '@/constants/mock'
import { useMessageStore } from '@/stores/message'
import type { ChatMessage } from '@/types'

const messageStore = useMessageStore()
const activeTab = ref('messages')
const chatPreview: ChatMessage[] = [
  { messageId: 9201, senderName: '苏同学', content: '我已经到驿站了，十分钟后送到楼下。', sentAt: '18:42', self: false },
  { messageId: 9202, senderName: '我', content: '收到，我下楼等你。', sentAt: '18:43', self: true },
]

messageStore.setUnreadCount(mockMessages.filter((item) => !item.isRead).length)
messageStore.setNotices(mockNotices)
messageStore.setRecentChats(chatPreview)
</script>