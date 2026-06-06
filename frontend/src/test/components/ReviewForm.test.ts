import ElementPlus, { ElMessage } from 'element-plus'
import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'

import { createReview } from '@/api/review'
import { createReview as createReviewFactory } from '@/test/factories'
import ReviewForm from '@/components/ReviewForm.vue'

vi.mock('@/api/review', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/api/review')>()
  return {
    ...actual,
    createReview: vi.fn(),
  }
})

describe('ReviewForm', () => {
  it('warns when no rating is selected', async () => {
    const wrapper = mount(ReviewForm, {
      props: {
        orderId: 1,
        targetUserId: 2,
        targetUserName: '被评价人',
      },
      global: {
        plugins: [ElementPlus],
      },
    })

    wrapper.findComponent({ name: 'ElRate' }).vm.$emit('update:modelValue', 0)
    await flushPromises()
    await wrapper.find('form').trigger('submit')

    expect(ElMessage.warning).toHaveBeenCalled()
    expect(createReview).not.toHaveBeenCalled()
  })

  it('submits review payload and emits submitted event', async () => {
    const review = createReviewFactory({ content: '很棒' })
    vi.mocked(createReview).mockResolvedValue(review)

    const wrapper = mount(ReviewForm, {
      props: {
        orderId: 1,
        targetUserId: 2,
        targetUserName: '被评价人',
      },
      global: {
        plugins: [ElementPlus],
      },
    })

    await wrapper.find('textarea').setValue('  很棒  ')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(createReview).toHaveBeenCalledWith({
      orderId: 1,
      targetUserId: 2,
      rating: 5,
      content: '很棒',
    })
    expect(wrapper.emitted('submitted')?.[0]).toEqual([review])
    expect((wrapper.find('textarea').element as HTMLTextAreaElement).value).toBe('')
  })
})
