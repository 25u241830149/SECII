<template>
  <section class="chat-panel">
    <header class="panel-head">
      <div>
        <h2>{{ panelTitle }}</h2>
        <p>{{ panelDescription }}</p>
      </div>
      <el-button :loading="loading" @click="loadMessages">刷新</el-button>
    </header>

    <el-skeleton :loading="loading" animated :rows="4">
      <template #default>
        <div ref="messageBox" class="message-box">
          <el-empty v-if="!messages.length" description="暂无聊天记录" />
          <template v-else>
            <article
              v-for="message in messages"
              :key="message.messageId"
              class="message-item"
              :class="{ mine: message.senderId === authStore.user?.userId }"
            >
              <el-avatar :size="34" :src="avatarUrl(message.senderAvatarUrl)">
                {{ fallbackName(message.senderName) }}
              </el-avatar>
              <div class="bubble">
                <div class="bubble-meta">
                  <strong>{{ message.senderName }}</strong>
                  <span>{{ formatTime(message.createdAt) }}</span>
                </div>
                <p>{{ message.content }}</p>
              </div>
            </article>
          </template>
        </div>
      </template>
    </el-skeleton>

    <form class="send-row" @submit.prevent="sendMessage">
      <el-input
        v-model="draft"
        maxlength="500"
        show-word-limit
        :disabled="disabled"
        :placeholder="inputPlaceholder"
      />
      <el-button type="primary" native-type="submit" :loading="sending">
        {{ disabled ? '沟通已关闭' : '发送' }}
      </el-button>
    </form>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import { getOrderChat, sendOrderChat } from '@/api/message'
import { useAuthStore } from '@/stores'
import { resolveAssetUrl } from '@/utils/asset'
import type { ChatMessageDTO, EntityId } from '@/types'

const props = defineProps<{
  orderId: EntityId
  teamUp?: boolean
  disabled?: boolean
  disabledReason?: string
}>()

const authStore = useAuthStore()
const loading = ref(false)
const sending = ref(false)
const draft = ref('')
const messages = ref<ChatMessageDTO[]>([])
const messageBox = ref<HTMLDivElement>()
const panelTitle = computed(() => (props.teamUp ? '队伍沟通' : '订单沟通'))
const panelDescription = computed(() => (
  props.disabled
    ? props.disabledReason || '当前沟通渠道已关闭，无法继续发送消息。'
    : props.teamUp
      ? '仅组队发起人与申请同学可见，刷新时会自动同步最新消息。'
      : '仅订单双方可见，刷新时会自动同步最新消息。'
))
const inputPlaceholder = computed(() => (
  props.teamUp
    ? props.disabled ? '队伍沟通已关闭' : '输入组队沟通消息'
    : props.disabled ? '订单沟通已关闭' : '输入给对方的订单消息'
))

const formatTime = (value: string) => new Date(value).toLocaleString('zh-CN', { hour12: false })
const fallbackName = (name: string) => (name || '同学').slice(0, 1).toUpperCase()
const avatarUrl = (value?: string | null) => resolveAssetUrl(value || '')

const scrollToBottom = async () => {
  await nextTick()
  if (messageBox.value) {
    messageBox.value.scrollTop = messageBox.value.scrollHeight
  }
}

const loadMessages = async () => {
  loading.value = true
  try {
    messages.value = await getOrderChat(props.orderId)
    await scrollToBottom()
  } finally {
    loading.value = false
  }
}

const sendMessage = async () => {
  if (props.disabled) {
    await ElMessageBox.alert(
      props.disabledReason || '当前沟通渠道已关闭，无法继续发送消息。',
      props.teamUp ? '队伍沟通已关闭' : '订单沟通已关闭',
      {
        confirmButtonText: '知道了',
        type: 'warning',
      },
    )
    return
  }

  const content = draft.value.trim()
  if (!content) {
    ElMessage.warning('请输入消息内容')
    return
  }

  sending.value = true
  try {
    const created = await sendOrderChat(props.orderId, { content })
    messages.value = [...messages.value, created]
    draft.value = ''
    await scrollToBottom()
  } finally {
    sending.value = false
  }
}

onMounted(loadMessages)
</script>

<style scoped>
.chat-panel {
  display: grid;
  box-sizing: border-box;
  height: 430px;
  grid-template-rows: auto minmax(0, 1fr) auto;
  gap: 14px;
  padding: 22px;
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.07);
}

.chat-panel :deep(.el-skeleton),
.chat-panel :deep(.el-skeleton__content) {
  display: flex;
  min-height: 0;
  flex: 1;
  flex-direction: column;
}

.panel-head,
.send-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.panel-head h2,
.panel-head p,
.bubble p {
  margin: 0;
}

.panel-head h2 {
  color: #111827;
  font-size: 20px;
}

.panel-head p {
  margin-top: 5px;
  color: #64748b;
  font-size: 13px;
}

.message-box {
  display: grid;
  min-height: 0;
  flex: 1;
  align-content: start;
  gap: 12px;
  overflow-y: auto;
  padding: 14px;
  border: 1px solid #edf2f7;
  border-radius: 8px;
  background: #f8fafc;
}

.message-item {
  display: grid;
  grid-template-columns: 34px minmax(0, 1fr);
  gap: 10px;
  align-items: start;
}

.message-item.mine {
  grid-template-columns: minmax(0, 1fr) 34px;
}

.message-item.mine :deep(.el-avatar) {
  grid-column: 2;
  grid-row: 1;
}

.message-item.mine .bubble {
  grid-column: 1;
  justify-self: end;
  background: #eef5ff;
}

.bubble {
  max-width: min(620px, 100%);
  padding: 10px 12px;
  border: 1px solid #e6edf6;
  border-radius: 8px;
  background: #fff;
}

.bubble-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-bottom: 6px;
  color: #64748b;
  font-size: 12px;
}

.bubble-meta strong {
  color: #111827;
}

.bubble p {
  color: #374151;
  line-height: 1.55;
  white-space: pre-wrap;
  word-break: break-word;
}

.send-row :deep(.el-input) {
  flex: 1;
}

@media (max-width: 720px) {
  .panel-head,
  .send-row {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
