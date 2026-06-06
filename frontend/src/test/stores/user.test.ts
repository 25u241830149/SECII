import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { getUserHome, getUserProfile, updateUserProfile } from '@/api/user'
import { useUserStore } from '@/stores'
import { createUserInfo } from '@/test/factories'

vi.mock('@/api/user', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/api/user')>()
  return {
    ...actual,
    getUserProfile: vi.fn(),
    updateUserProfile: vi.fn(),
    getUserHome: vi.fn(),
  }
})

describe('useUserStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('fetches profile and resets loading afterwards', async () => {
    const profile = createUserInfo({ nickname: '新的昵称' })
    vi.mocked(getUserProfile).mockResolvedValue(profile)
    const store = useUserStore()

    await expect(store.fetchProfile(1)).resolves.toEqual(profile)
    expect(store.profile).toEqual(profile)
    expect(store.loading).toBe(false)
  })

  it('updates the stored profile after profile update', async () => {
    const updated = createUserInfo({ nickname: '已更新' })
    vi.mocked(updateUserProfile).mockResolvedValue(updated)
    const store = useUserStore()

    await expect(store.updateProfile(1, { nickname: '已更新' })).resolves.toEqual(updated)
    expect(store.profile).toEqual(updated)
  })

  it('loads user home summary', async () => {
    const home = {
      userId: 1,
      nickname: '首页用户',
      avatarUrl: '/avatar.png',
      creditScore: 100,
      creditLevel: '优秀',
      publishedTaskCount: 2,
      completedOrderCount: 3,
      reviewCount: 4,
      averageRating: 4.8,
    }
    vi.mocked(getUserHome).mockResolvedValue(home)
    const store = useUserStore()

    await expect(store.fetchHome(1)).resolves.toEqual(home)
    expect(store.home).toEqual(home)
  })
})
