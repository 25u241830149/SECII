import axios, { AxiosError, type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

import type { ApiResponse } from '@/types'

interface RequestConfig extends AxiosRequestConfig {
  suppressErrorMessage?: boolean
}

export const TOKEN_STORAGE_KEY = 'campushub.auth.token'
export const USER_STORAGE_KEY = 'campushub.auth.user'

export class ApiError<T = unknown> extends Error {
  code?: number
  status?: number
  data?: T

  constructor(message: string, options: { code?: number; status?: number; data?: T } = {}) {
    super(message)
    this.name = 'ApiError'
    this.code = options.code
    this.status = options.status
    this.data = options.data
  }
}

const getStoredToken = () => {
  if (typeof window === 'undefined') return ''
  return (
    window.localStorage.getItem(TOKEN_STORAGE_KEY) ||
    window.sessionStorage.getItem(TOKEN_STORAGE_KEY) ||
    ''
  )
}

const clearStoredSession = () => {
  if (typeof window === 'undefined') return
  window.localStorage.removeItem(TOKEN_STORAGE_KEY)
  window.localStorage.removeItem(USER_STORAGE_KEY)
  window.sessionStorage.removeItem(TOKEN_STORAGE_KEY)
  window.sessionStorage.removeItem(USER_STORAGE_KEY)
}

const shouldRedirectToLogin = () => {
  if (typeof window === 'undefined') return false
  return !window.location.pathname.startsWith('/login')
}

const isLoginRequest = (url?: string) => {
  return Boolean(url && /(^|\/)user\/login(?:$|\?)/.test(url))
}

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 12_000,
  headers: {
    'Content-Type': 'application/json',
  },
})

request.interceptors.request.use((config) => {
  const token = getStoredToken()

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  config.headers['X-Requested-With'] = 'XMLHttpRequest'
  return config
})

request.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiResponse>) => {
    const status = error.response?.status
    const body = error.response?.data
    const message = body?.message || error.message || '请求失败，请稍后重试'
    const suppressErrorMessage = (error.config as RequestConfig | undefined)?.suppressErrorMessage

    if (status === 401 && isLoginRequest(error.config?.url)) {
      ElMessage.error(message)
    } else if (status === 401) {
      clearStoredSession()
      ElMessage.warning('登录状态已过期，请重新登录')

      if (shouldRedirectToLogin()) {
        const redirect = encodeURIComponent(
          `${window.location.pathname}${window.location.search}`,
        )
        window.location.assign(`/login?redirect=${redirect}`)
      }
    } else if (suppressErrorMessage) {
      // Background refresh failures are handled by the caller without interrupting the UI.
    } else if (status && status >= 500) {
      ElMessage.error('服务暂时不可用，请稍后再试')
    } else if (message) {
      ElMessage.error(message)
    }

    return Promise.reject(
      new ApiError(message, {
        code: body?.code,
        status,
        data: body,
      }),
    )
  },
)

function unwrapResponse<T>(body: ApiResponse<T> | T): T {
  if (
    body &&
    typeof body === 'object' &&
    'code' in body &&
    'message' in body &&
    'data' in body
  ) {
    const apiBody = body as ApiResponse<T>
    const ok = apiBody.success === true || apiBody.code === 0 || apiBody.code === 200

    if (!ok) {
      throw new ApiError(apiBody.message || '业务处理失败', {
        code: apiBody.code,
        data: apiBody,
      })
    }

    return apiBody.data
  }

  return body as T
}

export async function apiRequest<T>(config: RequestConfig) {
  const response = await request.request<ApiResponse<T> | T>(config)
  return unwrapResponse<T>(response.data)
}

export function apiGet<T>(url: string, config?: RequestConfig) {
  return apiRequest<T>({ ...config, method: 'GET', url })
}

export function apiPost<T, D = unknown>(url: string, data?: D, config?: RequestConfig) {
  return apiRequest<T>({ ...config, method: 'POST', url, data })
}

export function apiPut<T, D = unknown>(url: string, data?: D, config?: RequestConfig) {
  return apiRequest<T>({ ...config, method: 'PUT', url, data })
}

export function apiDelete<T>(url: string, config?: RequestConfig) {
  return apiRequest<T>({ ...config, method: 'DELETE', url })
}

export default request
