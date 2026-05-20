import { defineStore } from 'pinia'

import { getUserHome, getUserProfile, updateUserProfile } from '@/api/user'
import type { EntityId, UserHomeDTO, UserProfileDTO, UserProfileUpdateRequest } from '@/types'

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
        this.profile = await getUserProfile(userId)
        return this.profile
      } finally {
        this.loading = false
      }
    },

    async updateProfile(userId: EntityId, payload: UserProfileUpdateRequest) {
      const profile = await updateUserProfile(userId, payload)
      this.profile = profile
      return profile
    },

    async fetchHome(userId: EntityId) {
      this.home = await getUserHome(userId)
      return this.home
    },
  },
})
