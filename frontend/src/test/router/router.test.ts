import { createPinia, setActivePinia } from 'pinia'
import { flushPromises } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'

async function createTestRouter() {
  vi.resetModules()
  setActivePinia(createPinia())

  const simpleView = { template: '<div />' }
  vi.doMock('@/layouts/AdminLayout.vue', () => ({ default: simpleView }))
  vi.doMock('@/layouts/MainLayout.vue', () => ({ default: simpleView }))
  vi.doMock('@/layouts/PublicLayout.vue', () => ({ default: simpleView }))
  vi.doMock('@/views/home/Home.vue', () => ({ default: simpleView }))
  vi.doMock('@/views/system/ForbiddenView.vue', () => ({ default: simpleView }))
  vi.doMock('@/views/system/NotFoundView.vue', () => ({ default: simpleView }))
  vi.doMock('@/views/auth/LoginView.vue', () => ({ default: simpleView }))
  vi.doMock('@/views/order/OrderList.vue', () => ({ default: simpleView }))
  vi.doMock('@/views/admin/StatsDashboard.vue', () => ({ default: simpleView }))

  const { useAuthStore } = await import('@/stores/auth')
  const authStore = useAuthStore()
  const { default: router } = await import('@/router')

  return { router, authStore }
}

describe('router guards', () => {
  it('restores session and redirects unauthenticated users to login', async () => {
    const { router, authStore } = await createTestRouter()
    const restoreSpy = vi.spyOn(authStore, 'restoreSession')

    await router.push('/orders')
    await flushPromises()

    expect(restoreSpy).toHaveBeenCalledTimes(1)
    expect(router.currentRoute.value.path).toBe('/login')
    expect(router.currentRoute.value.query.redirect).toBe('/orders')
    expect(authStore.redirectPath).toBe('/orders')
  }, 15000)

  it('redirects authenticated users away from guest-only pages', async () => {
    const { router, authStore } = await createTestRouter()
    authStore.initialized = true
    authStore.token = 'token'
    authStore.redirectPath = '/orders'

    await router.push('/login')
    await flushPromises()

    expect(router.currentRoute.value.path).toBe('/orders')
  })

  it('blocks non-admin users from admin routes', async () => {
    const { router, authStore } = await createTestRouter()
    authStore.initialized = true
    authStore.token = 'token'
    authStore.user = {
      userId: 1,
      studentId: '20230001',
      nickname: 'normal-user',
      role: 'USER',
      creditScore: 99,
      verificationStatus: 'APPROVED',
    }

    await router.push('/admin')
    await flushPromises()

    expect(router.currentRoute.value.path).toBe('/403')
  })

  it('updates document title after successful navigation', async () => {
    const { router, authStore } = await createTestRouter()
    authStore.initialized = true
    authStore.token = 'token'

    await router.push('/orders')
    await flushPromises()

    expect(document.title.endsWith(' - CampusHub')).toBe(true)
  })
})
