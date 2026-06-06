import ElementPlus, { ElMessage } from 'element-plus'
import { createPinia } from 'pinia'
import { flushPromises, mount } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { getMessages } from '@/api/message'
import { getOrders, getOrderStats, grabOrder } from '@/api/order'
import { getTaskStats, getTasks } from '@/api/task'
import { getUserHome } from '@/api/user'
import { useAppStore, useAuthStore } from '@/stores'
import {
  createPage,
  createMessage,
  createOrderListItem,
  createTaskListItem,
  createTaskPage,
  createTaskStats,
  createUserInfo,
} from '@/test/factories'
import Home from '@/views/home/Home.vue'

vi.mock('@/api/task', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/api/task')>()
  return {
    ...actual,
    getTasks: vi.fn(),
    getTaskStats: vi.fn(),
  }
})

vi.mock('@/api/order', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/api/order')>()
  return {
    ...actual,
    getOrders: vi.fn(),
    getOrderStats: vi.fn(),
    grabOrder: vi.fn(),
  }
})

vi.mock('@/api/message', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/api/message')>()
  return {
    ...actual,
    getMessages: vi.fn(),
  }
})

vi.mock('@/api/user', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/api/user')>()
  return {
    ...actual,
    getUserHome: vi.fn(),
  }
})

vi.mock('@/utils/taskRecommendation', () => ({
  getRecommendationHint: vi.fn(() => '推荐提示'),
  rankSmartTaskRecommendations: vi.fn((tasks) => tasks),
  readTaskViewProfile: vi.fn(() => []),
  rememberTaskView: vi.fn(() => []),
}))

async function mountHome(initialPath = '/tasks', authenticated = false) {
  const pinia = createPinia()
  const authStore = useAuthStore(pinia)
  if (authenticated) {
    authStore.setSession({
      token: 'token',
      user: createUserInfo(),
    })
  }

  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: { template: '<div>root</div>' } },
      { path: '/tasks', name: 'task-list', component: Home },
      { path: '/tasks/publish', component: { template: '<div>publish</div>' } },
      { path: '/tasks/:taskId', component: { template: '<div>detail</div>' } },
      { path: '/orders', component: { template: '<div>orders</div>' } },
      { path: '/login', component: { template: '<div>login</div>' } },
    ],
  })

  await router.push(initialPath)
  await router.isReady()

  const wrapper = mount(Home, {
    global: {
      plugins: [pinia, router, ElementPlus],
    },
  })
  await flushPromises()

  return { wrapper, router, authStore, appStore: useAppStore(pinia) }
}

describe('Home view', () => {
  beforeEach(() => {
    vi.mocked(getTasks).mockImplementation(async (query: any = {}) => {
      if (query.size === 50) {
        return createTaskPage([createTaskListItem({ taskId: 99, title: '推荐任务' })], { total: 1 })
      }
      return createTaskPage([createTaskListItem({ taskId: 1, title: '首页任务' })], { total: 11 })
    })
    vi.mocked(getTaskStats).mockResolvedValue(createTaskStats())
    vi.mocked(getOrders).mockResolvedValue(createPage([createOrderListItem()], { total: 1 }))
    vi.mocked(getOrderStats).mockResolvedValue({
      waitingAcceptance: 1,
      pending: 2,
      inProgress: 3,
      waitingReview: 4,
      completed: 5,
    })
    vi.mocked(getUserHome).mockResolvedValue({
      userId: 1,
      nickname: '首页用户',
      avatarUrl: '/avatar.png',
      creditScore: 99,
      creditLevel: '优秀',
      publishedTaskCount: 2,
      completedOrderCount: 3,
      reviewCount: 4,
      averageRating: 4.9,
    })
    vi.mocked(getMessages).mockResolvedValue(createPage([createMessage()], { total: 1 }))
    vi.mocked(grabOrder).mockResolvedValue({} as any)
  })

  it('syncs route query into filters and applies new search conditions', async () => {
    const { wrapper, router, appStore } = await mountHome(
      '/tasks?category=TEAM_UP&keyword=study&sort=hot&status=IN_PROGRESS&rewardType=FREE&locationType=WITH_LOCATION',
    )

    expect(getTasks).toHaveBeenCalledWith(expect.objectContaining({
      category: 'TEAM_UP',
      keyword: 'study',
      sort: 'hot',
      status: 'IN_PROGRESS',
      rewardType: 'FREE',
      locationType: 'WITH_LOCATION',
      page: 1,
      size: 10,
    }))
    expect(appStore.activeTaskCategory).toBe('TEAM_UP')

    await wrapper.find('.search-row input').setValue('  next  ')
    await wrapper.find('.search-row button').trigger('click')
    await flushPromises()

    expect(router.currentRoute.value.query.keyword).toBe('next')
    expect(getTasks).toHaveBeenLastCalledWith(expect.objectContaining({
      keyword: 'next',
    }))
  })

  it('redirects guests to login when they try to grab a task', async () => {
    const { wrapper, router } = await mountHome('/tasks')
    await wrapper.findAll('.task-row .task-actions button')[1].trigger('click')
    await flushPromises()

    expect(ElMessage.warning).toHaveBeenCalled()
    expect(router.currentRoute.value.path).toBe('/login')
  })

  it('grabs task for authenticated users and refreshes dependent data', async () => {
    const { wrapper } = await mountHome('/tasks', true)
    const statsCallsBefore = vi.mocked(getTaskStats).mock.calls.length
    const orderCallsBefore = vi.mocked(getOrders).mock.calls.length

    await wrapper.findAll('.task-row .task-actions button')[1].trigger('click')
    await flushPromises()

    expect(grabOrder).toHaveBeenCalledWith({ taskId: 1 })
    expect(vi.mocked(getTaskStats).mock.calls.length).toBeGreaterThan(statsCallsBefore)
    expect(vi.mocked(getOrders).mock.calls.length).toBeGreaterThan(orderCallsBefore)
  })
})
