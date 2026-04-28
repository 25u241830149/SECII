import axios, { type AxiosError, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { router } from '@/router'
import { pinia } from '@/stores'
import { useAuthStore } from '@/stores/auth'
import { useUserStore } from '@/stores/user'
import type { ApiResponse } from '@/types'
import { getToken } from '@/utils/auth'

const LOGIN_PATH = '/login'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
})

function redirectToLogin() {
  const authStore = useAuthStore(pinia)
  const userStore = useUserStore(pinia)
  const currentRoute = router.currentRoute.value

  authStore.logout()
  userStore.clearProfile()

  if (currentRoute.path === LOGIN_PATH) {
    return
  }

  void router.replace({
    name: 'login',
    query: { redirect: currentRoute.fullPath },
  })
}

function throwBusinessError(payload: ApiResponse<unknown>): never {
  const message = payload.message || '请求失败'

  if (payload.code === 401) {
    redirectToLogin()
  } else {
    ElMessage.error(message)
  }

  throw new Error(message)
}

service.interceptors.request.use((config) => {
  const token = getToken()

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})

service.interceptors.response.use(undefined, (error: AxiosError<ApiResponse<never>>) => {
  const status = error.response?.status
  const message = error.response?.data?.message ?? error.message ?? '网络请求失败'

  if (status === 401) {
    redirectToLogin()
  } else {
    ElMessage.error(message)
  }

  return Promise.reject(error)
})

function unwrapResponse<T>(response: AxiosResponse<ApiResponse<T>>) {
  const payload = response.data

  if (payload.code !== 200) {
    throwBusinessError(payload)
  }

  return payload.data
}

async function get<T>(url: string, config?: AxiosRequestConfig) {
  const response = await service.get<ApiResponse<T>>(url, config)
  return unwrapResponse(response)
}

async function post<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
  const response = await service.post<ApiResponse<T>>(url, data, config)
  return unwrapResponse(response)
}

async function put<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
  const response = await service.put<ApiResponse<T>>(url, data, config)
  return unwrapResponse(response)
}

async function requestDelete<T>(url: string, config?: AxiosRequestConfig) {
  const response = await service.delete<ApiResponse<T>>(url, config)
  return unwrapResponse(response)
}

export const request = {
  get,
  post,
  put,
  delete: requestDelete,
}