import { ElMessage } from 'element-plus'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const axiosMock = vi.hoisted(() => ({
  requestInterceptor: undefined as ((config: any) => any) | undefined,
  responseFulfilledHandler: undefined as ((response: any) => any) | undefined,
  responseRejectedHandler: undefined as ((error: any) => any) | undefined,
  nextResponseData: undefined as unknown,
  nextError: null as any,
  lastRequestConfig: null as any,
}))

const axiosRequest = vi.hoisted(() => vi.fn(async (config: any) => {
  const resolvedConfig = axiosMock.requestInterceptor
    ? await axiosMock.requestInterceptor({ headers: {}, ...config })
    : config

  axiosMock.lastRequestConfig = resolvedConfig

  if (axiosMock.nextError) {
    return axiosMock.responseRejectedHandler?.(axiosMock.nextError)
  }

  const response = {
    data: axiosMock.nextResponseData,
    config: resolvedConfig,
  }

  return axiosMock.responseFulfilledHandler ? axiosMock.responseFulfilledHandler(response) : response
}))

vi.mock('axios', () => {
  const instance = {
    interceptors: {
      request: {
        use: vi.fn((handler: (config: any) => any) => {
          axiosMock.requestInterceptor = handler
        }),
      },
      response: {
        use: vi.fn((onFulfilled: (response: any) => any, onRejected: (error: any) => any) => {
          axiosMock.responseFulfilledHandler = onFulfilled
          axiosMock.responseRejectedHandler = onRejected
        }),
      },
    },
    request: axiosRequest,
  }

  const create = vi.fn(() => instance)

  return {
    default: { create },
    create,
    AxiosError: class AxiosError extends Error {},
  }
})

import {
  ApiError,
  TOKEN_STORAGE_KEY,
  USER_STORAGE_KEY,
  apiGet,
  apiPost,
} from '@/api/request'

describe('api request helpers', () => {
  beforeEach(() => {
    axiosMock.nextResponseData = undefined
    axiosMock.nextError = null
    axiosMock.lastRequestConfig = null
    axiosRequest.mockClear()
    window.localStorage.clear()
    window.sessionStorage.clear()
    history.pushState({}, '', '/')
  })

  it('injects bearer token into outgoing requests', async () => {
    window.localStorage.setItem(TOKEN_STORAGE_KEY, 'persisted-token')
    axiosMock.nextResponseData = { ok: true }

    await apiGet('/tasks')

    expect(axiosMock.lastRequestConfig.headers.Authorization).toBe('Bearer persisted-token')
    expect(axiosMock.lastRequestConfig.headers['X-Requested-With']).toBe('XMLHttpRequest')
  })

  it('unwraps successful ApiResponse payloads', async () => {
    axiosMock.nextResponseData = {
      code: 200,
      message: 'ok',
      data: { token: 'token-123' },
    }

    await expect(apiPost('/user/login', { studentId: '1', password: '123456' })).resolves.toEqual({
      token: 'token-123',
    })
  })

  it('throws ApiError for unsuccessful ApiResponse payloads', async () => {
    axiosMock.nextResponseData = {
      code: 400,
      message: '业务失败',
      data: null,
    }

    await expect(apiGet('/tasks')).rejects.toMatchObject({
      message: '业务失败',
      code: 400,
    })
  })

  it('shows login error message for login 401 responses', async () => {
    axiosMock.nextError = {
      response: {
        status: 401,
        data: {
          code: 401,
          message: '账号或密码错误',
        },
      },
      config: { url: '/user/login' },
      message: 'Unauthorized',
    }

    await expect(apiPost('/user/login', {})).rejects.toBeInstanceOf(ApiError)
    expect(ElMessage.error).toHaveBeenCalledWith('账号或密码错误')
  })

  it('clears session for non-login 401 responses', async () => {
    window.localStorage.setItem(TOKEN_STORAGE_KEY, 'token')
    window.localStorage.setItem(USER_STORAGE_KEY, JSON.stringify({ userId: 1 }))
    history.pushState({}, '', '/login')

    axiosMock.nextError = {
      response: {
        status: 401,
        data: {
          code: 401,
          message: '登录过期',
        },
      },
      config: { url: '/messages' },
      message: 'Unauthorized',
    }

    await expect(apiGet('/messages')).rejects.toBeInstanceOf(ApiError)

    expect(window.localStorage.getItem(TOKEN_STORAGE_KEY)).toBeNull()
    expect(window.localStorage.getItem(USER_STORAGE_KEY)).toBeNull()
    expect(ElMessage.warning).toHaveBeenCalledWith('登录状态已过期，请重新登录')
  })
})
