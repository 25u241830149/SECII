import { defineStore } from 'pinia'

import { getTaskStats, getTasks } from '@/api/task'
import type {
  LocationTypeFilter,
  RewardTypeFilter,
  SortType,
  TaskCategory,
  TaskListDTO,
  TaskStatsDTO,
  TaskStatusFilter,
} from '@/types'

interface FeedState {
  category: TaskCategory | 'ALL'
  keyword: string
  sort: SortType
  status: TaskStatusFilter
  rewardType: RewardTypeFilter
  locationType: LocationTypeFilter
  page: number
  size: number
  total: number
  tasks: TaskListDTO[]
  stats: TaskStatsDTO
  loading: boolean
}

export const useFeedStore = defineStore('feed', {
  state: (): FeedState => ({
    category: 'ALL',
    keyword: '',
    sort: 'time',
    status: 'ALL',
    rewardType: 'ALL',
    locationType: 'ALL',
    page: 1,
    size: 10,
    total: 0,
    tasks: [],
    stats: {
      todayCreated: 0,
      inProgress: 0,
      completed: 0,
    },
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
          status: this.status === 'ALL' ? undefined : this.status,
          rewardType: this.rewardType === 'ALL' ? undefined : this.rewardType,
          locationType: this.locationType === 'ALL' ? undefined : this.locationType,
          page: this.page,
          size: this.size,
          excludeCompleted: true,
        })
        this.tasks = result.records

        const loadedTotal = (this.page - 1) * this.size + result.records.length
        this.total = result.records.length < this.size ? loadedTotal : Math.max(result.total, loadedTotal)
      } finally {
        this.loading = false
      }
    },

    async fetchStats() {
      try {
        this.stats = await getTaskStats()
      } catch {
        this.stats = {
          todayCreated: 0,
          inProgress: 0,
          completed: 0,
        }
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

    setStatus(status: TaskStatusFilter) {
      this.status = status
      this.page = 1
    },

    setRewardType(rewardType: RewardTypeFilter) {
      this.rewardType = rewardType
      this.page = 1
    },

    setLocationType(locationType: LocationTypeFilter) {
      this.locationType = locationType
      this.page = 1
    },

    setPage(page: number) {
      this.page = page
    },
  },
})
