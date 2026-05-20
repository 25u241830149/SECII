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

const readJson = <T>(key: string): T | null => {
  if (typeof window === 'undefined') return null

  const value = window.localStorage.getItem(key)
  if (!value) return null

  try {
    return JSON.parse(value) as T
  } catch {
    window.localStorage.removeItem(key)
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

      this.token = window.localStorage.getItem(TOKEN_STORAGE_KEY) || ''
      this.user = readJson<UserInfoDTO>(USER_STORAGE_KEY)
      this.initialized = true
    },

    setSession(session: AuthSession) {
      this.token = session.token
      this.user = session.user

      if (typeof window !== 'undefined') {
        window.localStorage.setItem(TOKEN_STORAGE_KEY, session.token)

        if (session.user) {
          window.localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(session.user))
        } else {
          window.localStorage.removeItem(USER_STORAGE_KEY)
        }
      }
    },

    async login(payload: LoginRequest) {
      const result = await loginUser(payload)

      this.setSession({
        token: result.token,
        user: result.user,
      })

      return result
    },

    logout() {
      this.token = ''
      this.user = null

      if (typeof window !== 'undefined') {
        window.localStorage.removeItem(TOKEN_STORAGE_KEY)
        window.localStorage.removeItem(USER_STORAGE_KEY)
      }
    },

    setRedirectPath(path: string) {
      this.redirectPath = path || '/'
    },
  },
})
