import { defineStore } from 'pinia'

import type { TaskCategory } from '@/types'

interface AppState {
  sidebarCollapsed: boolean
  activeTaskCategory: TaskCategory | 'ALL'
  unreadNoticeCount: number
  pageLoading: boolean
}

export const useAppStore = defineStore('app', {
  state: (): AppState => ({
    sidebarCollapsed: false,
    activeTaskCategory: 'ALL',
    unreadNoticeCount: 3,
    pageLoading: false,
  }),

  actions: {
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    },

    setActiveTaskCategory(category: AppState['activeTaskCategory']) {
      this.activeTaskCategory = category
    },

    markNoticesRead() {
      this.unreadNoticeCount = 0
    },

    setPageLoading(loading: boolean) {
      this.pageLoading = loading
    },
  },
})
