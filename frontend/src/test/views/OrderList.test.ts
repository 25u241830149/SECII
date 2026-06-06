import ElementPlus from 'element-plus'
import { createPinia } from 'pinia'
import { flushPromises, mount } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { getOrders } from '@/api/order'
import { getPublishedTasks } from '@/api/task'
import { useAuthStore } from '@/stores'
import { createOrderListItem, createPage, createTaskListItem, createTaskPage, createUserInfo } from '@/test/factories'
import OrderList from '@/views/order/OrderList.vue'

vi.mock('@/api/order', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/api/order')>()
  return {
    ...actual,
    getOrders: vi.fn(),
  }
})

vi.mock('@/api/task', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/api/task')>()
  return {
    ...actual,
    getPublishedTasks: vi.fn(),
  }
})

async function mountOrders(initialPath = '/orders') {
  const pinia = createPinia()
  useAuthStore(pinia).setSession({
    token: 'token',
    user: createUserInfo(),
  })

  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/orders', component: OrderList },
      { path: '/orders/:orderId', component: { template: '<div>detail</div>' } },
      { path: '/tasks/:taskId', component: { template: '<div>task</div>' } },
    ],
  })
  await router.push(initialPath)
  await router.isReady()

  const wrapper = mount(OrderList, {
    global: {
      plugins: [pinia, router, ElementPlus],
    },
  })
  await flushPromises()
  return { wrapper, router }
}

describe('OrderList view', () => {
  beforeEach(() => {
    vi.mocked(getPublishedTasks).mockResolvedValue(
      createTaskPage([createTaskListItem({ taskId: 1, status: 'IN_PROGRESS' })], { total: 1 }),
    )
    vi.mocked(getOrders).mockResolvedValue(
      createPage([createOrderListItem({ orderId: 501, taskId: 1, status: 'CONFIRMED' })], { total: 1 }),
    )
  })

  it('normalizes invalid accepted OPEN route into helper order query', async () => {
    await mountOrders('/orders?role=accepted&status=OPEN&page=-1')

    expect(getOrders).toHaveBeenCalledWith({
      userId: 1,
      role: 'helper',
      page: 1,
      size: 10,
    })
  })

  it('updates route when switching role and status', async () => {
    const { wrapper, router } = await mountOrders('/orders')

    await wrapper.findAll('.role-tabs button')[1].trigger('click')
    await flushPromises()
    expect(router.currentRoute.value.query).toEqual({
      role: 'accepted',
      status: 'ALL',
      page: '1',
    })

    await wrapper.findAll('.status-tabs button')[1].trigger('click')
    await flushPromises()
    expect(router.currentRoute.value.query).toEqual({
      role: 'accepted',
      status: 'PENDING',
      page: '1',
    })
  })

  it('maps published confirmed tasks to order detail and keeps source query', async () => {
    const { wrapper, router } = await mountOrders('/orders?role=published&status=CONFIRMED&page=2')

    expect(getPublishedTasks).toHaveBeenCalledWith({
      userId: 1,
      status: 'IN_PROGRESS',
      page: 2,
      size: 10,
    })

    await wrapper.find('.order-row button').trigger('click')
    await flushPromises()

    expect(router.currentRoute.value.path).toBe('/orders/501')
    expect(router.currentRoute.value.query).toEqual({
      fromRole: 'published',
      fromStatus: 'CONFIRMED',
      fromPage: '2',
    })
  })
})
