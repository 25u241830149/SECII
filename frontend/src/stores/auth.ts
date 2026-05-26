import { defineStore } from 'pinia'

import { loginUser } from '@/api/user'
import { TOKEN_STORAGE_KEY, USER_STORAGE_KEY } from '@/api/request'
import type { AuthSession, LoginRequest, LoginResponseDTO, UserInfoDTO } from '@/types'

interface AuthState {
  token: string
  user: UserInfoDTO | null
  initialized: boolean
  redirectPath: string
}

interface LoginOptions {
  rememberMe?: boolean
}

const readStoredValue = (key: string) => {
  if (typeof window === 'undefined') return null

  return window.localStorage.getItem(key) || window.sessionStorage.getItem(key)
}

const clearStoredSession = () => {
  if (typeof window === 'undefined') return

  window.localStorage.removeItem(TOKEN_STORAGE_KEY)
  window.localStorage.removeItem(USER_STORAGE_KEY)
  window.sessionStorage.removeItem(TOKEN_STORAGE_KEY)
  window.sessionStorage.removeItem(USER_STORAGE_KEY)
}

const readJson = <T>(key: string): T | null => {
  if (typeof window === 'undefined') return null

  const value = readStoredValue(key)
  if (!value) return null

  try {
    return JSON.parse(value) as T
  } catch {
    window.localStorage.removeItem(key)
    window.sessionStorage.removeItem(key)
    return null
  }
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: '',
    user: null,
    initialized: false,
    redirectPath: '/',
  }),

  getters: {
    isAuthenticated: (state) => Boolean(state.token),
    isAdmin: (state) => state.user?.role === 'ADMIN',
  },

  actions: {
    restoreSession() {
      if (typeof window === 'undefined') {
        this.initialized = true
        return
      }

      this.token = readStoredValue(TOKEN_STORAGE_KEY) || ''
      this.user = readJson<UserInfoDTO>(USER_STORAGE_KEY)
      this.initialized = true
    },

    setSession(session: AuthSession, rememberMe = false) {
      this.token = session.token
      this.user = session.user

      if (typeof window !== 'undefined') {
        clearStoredSession()

        const storage = rememberMe ? window.localStorage : window.sessionStorage
        storage.setItem(TOKEN_STORAGE_KEY, session.token)

        if (session.user) {
          storage.setItem(USER_STORAGE_KEY, JSON.stringify(session.user))
        }
      }
    },

    async login(payload: LoginRequest, options: LoginOptions = {}) {
      const result = await loginUser(payload)

      this.setSession({
        token: result.token,
        user: result.user,
      }, Boolean(options.rememberMe))

      return result
    },

    logout() {
      this.token = ''
      this.user = null

      if (typeof window !== 'undefined') {
        clearStoredSession()
      }
    },

    setRedirectPath(path: string) {
      this.redirectPath = path || '/'
    },
  },
})
