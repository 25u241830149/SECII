import { ref } from 'vue'
import { defineStore } from 'pinia'
import type { ChatMessage, NoticeItem } from '@/types'

export const useMessageStore = defineStore('message', () => {
  const unreadCount = ref(0)
  const notices = ref<NoticeItem[]>([])
  const recentChats = ref<ChatMessage[]>([])

  function setUnreadCount(count: number) {
    unreadCount.value = count
  }

  function setNotices(items: NoticeItem[]) {
    notices.value = items
  }

  function setRecentChats(items: ChatMessage[]) {
    recentChats.value = items
  }

  return {
    unreadCount,
    notices,
    recentChats,
    setUnreadCount,
    setNotices,
    setRecentChats,
  }
})