import { defineStore } from 'pinia'

import { getTasks } from '@/api/task'
import type { SortType, TaskCategory, TaskListDTO } from '@/types'

interface FeedState {
  category: TaskCategory | 'ALL'
  keyword: string
  sort: SortType
  page: number
  size: number
  total: number
  tasks: TaskListDTO[]
  loading: boolean
}

export const useFeedStore = defineStore('feed', {
  state: (): FeedState => ({
    category: 'ALL',
    keyword: '',
    sort: 'time',
    page: 1,
    size: 8,
    total: 0,
    tasks: [],
    loading: false,
  }),

  getters: {
    hasNext: (state) => state.page * state.size < state.total,
  },

  actions: {
    async fetchTasks() {
      this.loading = true

      try {
        const result = await getTasks({
          category: this.category === 'ALL' ? undefined : this.category,
          keyword: this.keyword || undefined,
          sort: this.sort,
          page: this.page,
          size: this.size,
        })
        this.tasks = result.records
        this.total = result.total
      } finally {
        this.loading = false
      }
    },

    setCategory(category: FeedState['category']) {
      this.category = category
      this.page = 1
    },

    setKeyword(keyword: string) {
      this.keyword = keyword.trim()
      this.page = 1
    },

    setSort(sort: SortType) {
      this.sort = sort
      this.page = 1
    },

    setPage(page: number) {
      this.page = page
    },
  },
})
