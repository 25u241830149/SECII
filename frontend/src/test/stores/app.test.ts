import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it } from 'vitest'

import { useAppStore } from '@/stores'

describe('useAppStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('toggles sidebar state', () => {
    const store = useAppStore()
    expect(store.sidebarCollapsed).toBe(false)

    store.toggleSidebar()
    expect(store.sidebarCollapsed).toBe(true)
  })

  it('updates task category, unread count, and page loading state', () => {
    const store = useAppStore()

    store.setActiveTaskCategory('TEAM_UP')
    store.setPageLoading(true)
    store.markNoticesRead()

    expect(store.activeTaskCategory).toBe('TEAM_UP')
    expect(store.pageLoading).toBe(true)
    expect(store.unreadNoticeCount).toBe(0)
  })
})
