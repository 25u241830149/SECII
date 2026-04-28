import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import type { LoginResponse, UserRole } from '@/types'
import { clearToken, getToken, setToken } from '@/utils/auth'

const ROLE_KEY = 'campushub_role'
const USER_ID_KEY = 'campushub_user_id'

export const useAuthStore = defineStore('auth', () => {
  const token = ref('')
  const role = ref<UserRole>('USER')
  const userId = ref<number | null>(null)
  const initialized = ref(false)
  const isLoggedIn = computed(() => Boolean(token.value))

  function restoreSession() {
    token.value = getToken()

    const cachedRole = localStorage.getItem(ROLE_KEY)
    if (cachedRole === 'ADMIN' || cachedRole === 'USER') {
      role.value = cachedRole
    }

    const cachedUserId = localStorage.getItem(USER_ID_KEY)
    userId.value = cachedUserId ? Number(cachedUserId) : null
    initialized.value = true
  }

  function loginSuccess(payload: LoginResponse) {
    token.value = payload.token
    role.value = payload.role
    userId.value = payload.userId
    initialized.value = true
    setToken(payload.token)
    localStorage.setItem(ROLE_KEY, payload.role)
    localStorage.setItem(USER_ID_KEY, String(payload.userId))
  }

  function logout() {
    token.value = ''
    role.value = 'USER'
    userId.value = null
    initialized.value = true
    clearToken()
    localStorage.removeItem(ROLE_KEY)
    localStorage.removeItem(USER_ID_KEY)
  }

  return {
    token,
    role,
    userId,
    initialized,
    isLoggedIn,
    restoreSession,
    loginSuccess,
    logout,
  }
})