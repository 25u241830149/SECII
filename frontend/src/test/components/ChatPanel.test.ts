import ElementPlus, { ElMessage, ElMessageBox } from 'element-plus'
import { createPinia } from 'pinia'
import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'

import { getOrderChat, sendOrderChat } from '@/api/message'
import { useAuthStore } from '@/stores'
import { createChatMessage, createUserInfo } from '@/test/factories'
import ChatPanel from '@/components/ChatPanel.vue'

vi.mock('@/api/message', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/api/message')>()
  return {
    ...actual,
    getOrderChat: vi.fn(),
    sendOrderChat: vi.fn(),
  }
})

async function mountChat(disabled = false) {
  const pinia = createPinia()
  useAuthStore(pinia).setSession({
    token: 'token',
    user: createUserInfo(),
  })

  const wrapper = mount(ChatPanel, {
    props: {
      orderId: 1,
      disabled,
      disabledReason: '当前沟通已关闭',
    },
    global: {
      plugins: [pinia, ElementPlus],
    },
  })
  await flushPromises()
  return wrapper
}

describe('ChatPanel', () => {
  it('loads existing chat messages and sends a new one', async () => {
    vi.mocked(getOrderChat).mockResolvedValue([createChatMessage()])
    vi.mocked(sendOrderChat).mockResolvedValue(createChatMessage({
      messageId: 2,
      content: '新的消息',
    }))

    const wrapper = await mountChat()
    await wrapper.find('input').setValue('新的消息')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(getOrderChat).toHaveBeenCalledWith(1)
    expect(sendOrderChat).toHaveBeenCalledWith(1, { content: '新的消息' })
    expect(wrapper.text()).toContain('新的消息')
  })

  it('shows alert instead of sending when disabled', async () => {
    vi.spyOn(ElMessageBox, 'alert').mockResolvedValue(undefined as never)
    vi.mocked(getOrderChat).mockResolvedValue([])

    const wrapper = await mountChat(true)
    await wrapper.find('form').trigger('submit')

    expect(ElMessageBox.alert).toHaveBeenCalled()
    expect(sendOrderChat).not.toHaveBeenCalled()
  })

  it('warns instead of sending blank messages', async () => {
    vi.mocked(getOrderChat).mockResolvedValue([])

    const wrapper = await mountChat()
    await wrapper.find('form').trigger('submit')

    expect(ElMessage.warning).toHaveBeenCalled()
    expect(sendOrderChat).not.toHaveBeenCalled()
  })
})
