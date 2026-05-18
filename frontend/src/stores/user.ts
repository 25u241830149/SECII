import { defineStore } from 'pinia'

import { apiGet, apiPut } from '@/api/request'
import type { EntityId, UserHomeDTO, UserProfileDTO } from '@/types'

interface UserState {
  profile: UserProfileDTO | null
  home: UserHomeDTO | null
  loading: boolean
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    profile: null,
    home: null,
    loading: false,
  }),

  actions: {
    setProfile(profile: UserProfileDTO | null) {
      this.profile = profile
    },

    async fetchProfile(userId: EntityId) {
      this.loading = true

      try {
        this.profile = await apiGet<UserProfileDTO>('/user/profile', {
          params: { userId },
        })
        return this.profile
      } finally {
        this.loading = false
      }
    },

    async updateProfile(userId: EntityId, payload: Partial<UserProfileDTO>) {
      const profile = await apiPut<UserProfileDTO, Partial<UserProfileDTO>>('/user/profile', payload, {
        params: { userId },
      })
      this.profile = profile
      return profile
    },

    async fetchHome(userId: EntityId) {
      this.home = await apiGet<UserHomeDTO>(`/user/${userId}/home`)
      return this.home
    },
  },
})
