import { ref } from 'vue'
import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', () => {
  const globalLoading = ref(false)
  const noticeDialogVisible = ref(false)
  const globalNotice = ref('')

  function setLoading(loading: boolean) {
    globalLoading.value = loading
  }

  function showNotice(message: string) {
    globalNotice.value = message
    noticeDialogVisible.value = true
  }

  function hideNotice() {
    noticeDialogVisible.value = false
  }

  return {
    globalLoading,
    noticeDialogVisible,
    globalNotice,
    setLoading,
    showNotice,
    hideNotice,
  }
})