import { ref } from 'vue'
import { defineStore } from 'pinia'
import type { UserInfo } from '@/types'

export const useUserStore = defineStore('user', () => {
  const profile = ref<UserInfo | null>(null)
  const creditScore = ref(0)
  const verificationStatus = ref('UNVERIFIED')

  function setProfile(user: UserInfo) {
    profile.value = user
    creditScore.value = user.creditScore
    verificationStatus.value = user.verificationStatus
  }

  function clearProfile() {
    profile.value = null
    creditScore.value = 0
    verificationStatus.value = 'UNVERIFIED'
  }

  return {
    profile,
    creditScore,
    verificationStatus,
    setProfile,
    clearProfile,
  }
})