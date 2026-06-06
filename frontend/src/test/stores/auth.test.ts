import { setActivePinia, createPinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { TOKEN_STORAGE_KEY, USER_STORAGE_KEY } from '@/api/request'
import { loginUser } from '@/api/user'
import { useAuthStore } from '@/stores'
import { createLoginResponse, createUserInfo } from '@/test/factories'

vi.mock('@/api/user', () => ({
  loginUser: vi.fn(),
}))

describe('useAuthStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('restores a persisted session from localStorage', () => {
    const user = createUserInfo({ role: 'ADMIN' })
    window.localStorage.setItem(TOKEN_STORAGE_KEY, 'persisted-token')
    window.localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user))

    const store = useAuthStore()
    store.restoreSession()

    expect(store.token).toBe('persisted-token')
    expect(store.user).toEqual(user)
    expect(store.initialized).toBe(true)
    expect(store.isAuthenticated).toBe(true)
    expect(store.isAdmin).toBe(true)
  })

  it('clears invalid persisted user JSON while keeping initialization stable', () => {
    window.sessionStorage.setItem(TOKEN_STORAGE_KEY, 'persisted-token')
    window.sessionStorage.setItem(USER_STORAGE_KEY, '{invalid json')

    const store = useAuthStore()
    store.restoreSession()

    expect(store.token).toBe('persisted-token')
    expect(store.user).toBeNull()
    expect(store.initialized).toBe(true)
    expect(window.sessionStorage.getItem(USER_STORAGE_KEY)).toBeNull()
  })

  it('stores non-remembered sessions in sessionStorage after clearing stale state', () => {
    const user = createUserInfo()
    const store = useAuthStore()

    window.localStorage.setItem(TOKEN_STORAGE_KEY, 'old-token')
    window.localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user))

    store.setSession({ token: 'fresh-token', user }, false)

    expect(store.token).toBe('fresh-token')
    expect(window.localStorage.getItem(TOKEN_STORAGE_KEY)).toBeNull()
    expect(window.sessionStorage.getItem(TOKEN_STORAGE_KEY)).toBe('fresh-token')
    expect(window.sessionStorage.getItem(USER_STORAGE_KEY)).toBe(JSON.stringify(user))
  })

  it('logs in with rememberMe and persists to localStorage', async () => {
    const response = createLoginResponse()
    vi.mocked(loginUser).mockResolvedValue(response)

    const store = useAuthStore()
    const result = await store.login(
      { studentId: '20230001', password: '123456' },
      { rememberMe: true },
    )

    expect(result).toEqual(response)
    expect(loginUser).toHaveBeenCalledWith({ studentId: '20230001', password: '123456' })
    expect(store.token).toBe(response.token)
    expect(window.localStorage.getItem(TOKEN_STORAGE_KEY)).toBe(response.token)
    expect(window.sessionStorage.getItem(TOKEN_STORAGE_KEY)).toBeNull()
  })

  it('clears state and storage on logout', () => {
    const user = createUserInfo()
    const store = useAuthStore()

    store.setSession({ token: 'logout-token', user }, true)
    store.logout()

    expect(store.token).toBe('')
    expect(store.user).toBeNull()
    expect(window.localStorage.getItem(TOKEN_STORAGE_KEY)).toBeNull()
    expect(window.localStorage.getItem(USER_STORAGE_KEY)).toBeNull()
    expect(window.sessionStorage.getItem(TOKEN_STORAGE_KEY)).toBeNull()
  })
})
