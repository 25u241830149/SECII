import ElementPlus, { ElMessage } from 'element-plus'
import { createPinia, setActivePinia } from 'pinia'
import { flushPromises, mount } from '@vue/test-utils'
import { createMemoryHistory, createRouter } from 'vue-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { TOKEN_STORAGE_KEY } from '@/api/request'
import { loginUser } from '@/api/user'
import { useAuthStore } from '@/stores'
import { createLoginResponse } from '@/test/factories'
import LoginView from '@/views/auth/LoginView.vue'

vi.mock('@/api/user', () => ({
  loginUser: vi.fn(),
}))

async function mountLoginView(initialPath = '/login') {
  const pinia = createPinia()
  setActivePinia(pinia)

  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/login', component: LoginView },
      { path: '/', component: { template: '<div>home</div>' } },
      { path: '/orders', component: { template: '<div>orders</div>' } },
      { path: '/register', component: { template: '<div>register</div>' } },
    ],
  })

  await router.push(initialPath)
  await router.isReady()

  const wrapper = mount(LoginView, {
    global: {
      plugins: [pinia, router, ElementPlus],
    },
  })

  return { wrapper, router, authStore: useAuthStore() }
}

describe('LoginView', () => {
  beforeEach(() => {
    vi.mocked(loginUser).mockReset()
  })

  it('blocks submit when validation fails', async () => {
    const { wrapper } = await mountLoginView()
    const inputs = wrapper.findAll('input')

    await inputs[0].setValue('')
    await inputs[1].setValue('123')
    await wrapper.find('.primary-action').trigger('click')
    await flushPromises()

    expect(loginUser).not.toHaveBeenCalled()
  })

  it('submits successfully and redirects to the requested path', async () => {
    vi.mocked(loginUser).mockResolvedValue(createLoginResponse({ token: 'remember-token' }))

    const { wrapper, router, authStore } = await mountLoginView('/login?redirect=%2Forders')
    const inputs = wrapper.findAll('input')

    await inputs[0].setValue('20230001')
    await inputs[1].setValue('123456')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(loginUser).toHaveBeenCalledWith({ studentId: '20230001', password: '123456' })
    expect(authStore.token).toBe('remember-token')
    expect(router.currentRoute.value.path).toBe('/orders')
    expect(ElMessage.success).toHaveBeenCalledWith('登录成功')
    expect(window.sessionStorage.getItem(TOKEN_STORAGE_KEY)).toBe('remember-token')
  })

  it('stores remembered sessions in localStorage', async () => {
    vi.mocked(loginUser).mockResolvedValue(createLoginResponse({ token: 'local-token' }))

    const { wrapper } = await mountLoginView()
    const inputs = wrapper.findAll('input')

    await inputs[0].setValue('20230001')
    await inputs[1].setValue('123456')
    await wrapper.find('input[type="checkbox"]').setValue(true)
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(window.localStorage.getItem(TOKEN_STORAGE_KEY)).toBe('local-token')
    expect(window.sessionStorage.getItem(TOKEN_STORAGE_KEY)).toBeNull()
  })

  it('disables inputs while a login request is pending and recovers after failure', async () => {
    let rejectLogin!: (reason?: unknown) => void
    vi.mocked(loginUser).mockImplementation(
      () =>
        new Promise((_, reject) => {
          rejectLogin = reject
        }),
    )

    const { wrapper } = await mountLoginView()
    const inputs = wrapper.findAll('input')

    await inputs[0].setValue('20230001')
    await inputs[1].setValue('123456')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(wrapper.find('input[autocomplete="username"]').attributes('disabled')).toBeDefined()
    expect(wrapper.find('input[autocomplete="current-password"]').attributes('disabled')).toBeDefined()

    rejectLogin(new Error('login failed'))
    await flushPromises()

    expect(wrapper.find('input[autocomplete="username"]').attributes('disabled')).toBeUndefined()
    expect(wrapper.find('input[autocomplete="current-password"]').attributes('disabled')).toBeUndefined()
  })
})
