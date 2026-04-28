import { ref } from 'vue'
import { defineStore } from 'pinia'
import type { FeedSort, PublishCategory, TaskItem } from '@/types'

export const useFeedStore = defineStore('feed', () => {
  const category = ref<PublishCategory | undefined>(undefined)
  const keyword = ref('')
  const sort = ref<FeedSort>('time')
  const records = ref<TaskItem[]>([])

  function setCategory(value?: PublishCategory) {
    category.value = value
  }

  function setKeyword(value: string) {
    keyword.value = value
  }

  function setSort(value: FeedSort) {
    sort.value = value
  }

  function setRecords(items: TaskItem[]) {
    records.value = items
  }

  function resetFilters() {
    category.value = undefined
    keyword.value = ''
    sort.value = 'time'
  }

  return {
    category,
    keyword,
    sort,
    records,
    setCategory,
    setKeyword,
    setSort,
    setRecords,
    resetFilters,
  }
})