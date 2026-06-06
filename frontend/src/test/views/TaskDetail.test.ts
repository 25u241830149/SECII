import ElementPlus, { ElMessage } from 'element-plus'
import { createPinia } from 'pinia'
import { flushPromises, mount } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { grabOrder } from '@/api/order'
import { deleteTask, favoriteTask, getTaskDetail, unfavoriteTask } from '@/api/task'
import { useAuthStore } from '@/stores'
import { createTaskListItem, createUserInfo } from '@/test/factories'
import TaskDetail from '@/views/task/TaskDetail.vue'

const rememberTaskView = vi.hoisted(() => vi.fn(() => []))

vi.mock('@/api/task', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/api/task')>()
  return {
    ...actual,
    getTaskDetail: vi.fn(),
    favoriteTask: vi.fn(),
    unfavoriteTask: vi.fn(),
    deleteTask: vi.fn(),
  }
})

vi.mock('@/api/order', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/api/order')>()
  return {
    ...actual,
    grabOrder: vi.fn(),
  }
})

vi.mock('@/utils/taskRecommendation', () => ({
  rememberTaskView,
}))

async function mountTaskDetail(initialPath = '/tasks/1', authenticated = false, userId = 1) {
  const pinia = createPinia()
  const authStore = useAuthStore(pinia)
  if (authenticated) {
    authStore.setSession({
      token: 'token',
      user: createUserInfo({ userId }),
    })
  }

  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/tasks/:taskId', component: TaskDetail },
      { path: '/orders', component: { template: '<div>orders</div>' } },
      { path: '/login', component: { template: '<div>login</div>' } },
    ],
  })
  await router.push(initialPath)
  await router.isReady()

  const wrapper = mount(TaskDetail, {
    global: {
      plugins: [pinia, router, ElementPlus],
      stubs: {
        TaskCommentSection: true,
      },
    },
  })
  await flushPromises()
  return { wrapper, router }
}

describe('TaskDetail view', () => {
  beforeEach(() => {
    rememberTaskView.mockClear()
    vi.mocked(getTaskDetail).mockResolvedValue(createTaskListItem({
      taskId: 1,
      publisherId: 2,
      favorited: false,
    }) as any)
    vi.mocked(favoriteTask).mockResolvedValue({ taskId: 1, favorited: true })
    vi.mocked(unfavoriteTask).mockResolvedValue({ taskId: 1, favorited: false })
    vi.mocked(deleteTask).mockResolvedValue()
    vi.mocked(grabOrder).mockResolvedValue({} as any)
  })

  it('loads detail and allows authenticated users to favorite', async () => {
    const { wrapper } = await mountTaskDetail('/tasks/1', true)
    await wrapper.findAll('footer.actions button')[1].trigger('click')
    await flushPromises()

    expect(getTaskDetail).toHaveBeenCalledWith(1)
    expect(rememberTaskView).toHaveBeenCalled()
    expect(favoriteTask).toHaveBeenCalledWith(1)
    expect(getTaskDetail).toHaveBeenCalledTimes(2)
  })

  it('redirects guests to login when they try to grab task', async () => {
    const { wrapper, router } = await mountTaskDetail('/tasks/1')
    await wrapper.find('footer.actions .el-button--primary').trigger('click')
    await flushPromises()

    expect(ElMessage.warning).toHaveBeenCalled()
    expect(router.currentRoute.value.path).toBe('/login')
  })

  it('returns to order list with source query when goBack is triggered', async () => {
    const { wrapper, router } = await mountTaskDetail(
      '/tasks/1?fromRole=accepted&fromStatus=CONFIRMED&fromPage=2',
      true,
    )
    await wrapper.findAll('footer.actions button')[0].trigger('click')
    await flushPromises()

    expect(router.currentRoute.value.path).toBe('/orders')
    expect(router.currentRoute.value.query).toEqual({
      role: 'accepted',
      status: 'CONFIRMED',
      page: '2',
    })
  })

  it('allows publisher to cancel task and reload detail', async () => {
    vi.mocked(getTaskDetail).mockResolvedValue(createTaskListItem({
      taskId: 1,
      publisherId: 1,
      status: 'OPEN',
    }) as any)

    const { wrapper } = await mountTaskDetail('/tasks/1', true, 1)
    await wrapper.findAll('footer.actions button')[2].trigger('click')
    await flushPromises()

    expect(deleteTask).toHaveBeenCalledWith(1)
    expect(getTaskDetail).toHaveBeenCalledTimes(2)
  })
})
