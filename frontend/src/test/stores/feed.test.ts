import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { getTaskStats, getTasks } from '@/api/task'
import { useFeedStore } from '@/stores'
import { createTaskListItem, createTaskPage, createTaskStats } from '@/test/factories'

vi.mock('@/api/task', () => ({
  getTasks: vi.fn(),
  getTaskStats: vi.fn(),
}))

describe('useFeedStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('fetches tasks with current filters and normalizes ALL filters to undefined', async () => {
    const records = Array.from({ length: 10 }, (_, index) =>
      createTaskListItem({
        taskId: index + 1,
        title: `task-${index + 1}`,
      }),
    )
    vi.mocked(getTasks).mockResolvedValue(createTaskPage(records, { total: 12 }))

    const store = useFeedStore()
    await store.fetchTasks()

    expect(getTasks).toHaveBeenCalledWith({
      category: undefined,
      keyword: undefined,
      sort: 'time',
      status: undefined,
      rewardType: undefined,
      locationType: undefined,
      page: 1,
      size: 10,
      excludeCompleted: true,
    })
    expect(store.tasks).toEqual(records)
    expect(store.total).toBe(12)
    expect(store.loading).toBe(false)
    expect(store.hasNext).toBe(true)
  })

  it('shrinks total to loaded count when the backend returns a partial page', async () => {
    const records = [
      createTaskListItem(),
      createTaskListItem({ taskId: 2, title: 'task-2' }),
    ]
    vi.mocked(getTasks).mockResolvedValue(createTaskPage(records, { total: 12 }))

    const store = useFeedStore()
    await store.fetchTasks()

    expect(store.total).toBe(2)
    expect(store.hasNext).toBe(false)
  })

  it('resets loading when fetchTasks fails', async () => {
    vi.mocked(getTasks).mockRejectedValue(new Error('boom'))

    const store = useFeedStore()

    await expect(store.fetchTasks()).rejects.toThrow('boom')
    expect(store.loading).toBe(false)
  })

  it('trims keyword and resets page when filters change', () => {
    const store = useFeedStore()
    store.page = 3

    store.setCategory('TEAM_UP')
    expect(store.category).toBe('TEAM_UP')
    expect(store.page).toBe(1)

    store.page = 4
    store.setKeyword('  校园跑腿  ')
    expect(store.keyword).toBe('校园跑腿')
    expect(store.page).toBe(1)
  })

  it('falls back to default stats when fetchStats fails', async () => {
    vi.mocked(getTaskStats).mockRejectedValue(new Error('stats failed'))

    const store = useFeedStore()
    store.stats = createTaskStats({ todayCreated: 9, inProgress: 9, completed: 9 })

    await store.fetchStats()

    expect(store.stats).toEqual({
      todayCreated: 0,
      inProgress: 0,
      completed: 0,
    })
  })
})
