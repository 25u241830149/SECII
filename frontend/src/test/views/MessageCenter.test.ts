import ElementPlus from 'element-plus'
import { flushPromises, mount } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import {
  deleteMessage,
  getLatestNotices,
  getMessages,
  getUnreadMessageCount,
  markAllMessagesRead,
  markMessageRead,
} from '@/api/message'
import { createMessage, createNotice, createPage } from '@/test/factories'
import MessageCenter from '@/views/message/MessageCenter.vue'

vi.mock('@/api/message', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/api/message')>()
  return {
    ...actual,
    getMessages: vi.fn(),
    getUnreadMessageCount: vi.fn(),
    getLatestNotices: vi.fn(),
    markMessageRead: vi.fn(),
    markAllMessagesRead: vi.fn(),
    deleteMessage: vi.fn(),
  }
})

async function mountMessages() {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/messages', component: MessageCenter },
      { path: '/', component: { template: '<div>home</div>' } },
    ],
  })
  await router.push('/messages')
  await router.isReady()

  const wrapper = mount(MessageCenter, {
    global: {
      plugins: [router, ElementPlus],
      stubs: {
        ElSelect: true,
        ElOption: true,
      },
    },
  })
  await flushPromises()
  return wrapper
}

describe('MessageCenter view', () => {
  beforeEach(() => {
    vi.mocked(getMessages).mockResolvedValue(createPage([createMessage()], { total: 1 }))
    vi.mocked(getUnreadMessageCount).mockResolvedValue({ count: 1 })
    vi.mocked(getLatestNotices).mockResolvedValue([createNotice()])
    vi.mocked(markMessageRead).mockResolvedValue()
    vi.mocked(markAllMessagesRead).mockResolvedValue()
    vi.mocked(deleteMessage).mockResolvedValue()
  })

  it('loads messages, notices, and unread count on mount', async () => {
    const wrapper = await mountMessages()

    expect(getMessages).toHaveBeenCalledWith({
      page: 1,
      size: 10,
      type: undefined,
      unread: undefined,
    })
    expect(getUnreadMessageCount).toHaveBeenCalled()
    expect(getLatestNotices).toHaveBeenCalledWith(5)
    expect(wrapper.findAll('.message-row')).toHaveLength(1)
  })

  it('marks one message as read and emits unread update event', async () => {
    const dispatchSpy = vi.spyOn(window, 'dispatchEvent')
    const wrapper = await mountMessages()

    await wrapper.find('.row-actions button').trigger('click')
    await flushPromises()

    expect(markMessageRead).toHaveBeenCalledWith(1)
    expect(dispatchSpy).toHaveBeenCalled()
  })

  it('marks all as read and supports deleting messages', async () => {
    const wrapper = await mountMessages()

    await wrapper.find('.toolbar-actions .el-button--primary').trigger('click')
    await flushPromises()
    expect(markAllMessagesRead).toHaveBeenCalled()

    await wrapper.findAll('.row-actions button').at(-1)!.trigger('click')
    await flushPromises()
    expect(deleteMessage).toHaveBeenCalledWith(1)
  })
})
