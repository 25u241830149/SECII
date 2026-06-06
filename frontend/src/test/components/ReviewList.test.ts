import ElementPlus from 'element-plus'
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

import { createReview } from '@/test/factories'
import ReviewList from '@/components/ReviewList.vue'

describe('ReviewList', () => {
  it('renders empty state when no reviews exist', () => {
    const wrapper = mount(ReviewList, {
      props: { reviews: [] },
      global: { plugins: [ElementPlus] },
    })

    expect(wrapper.text()).toContain('暂无评价')
  })

  it('renders review content and fallback avatar label', () => {
    const review = createReview({ reviewerAvatarUrl: null, content: null })
    const wrapper = mount(ReviewList, {
      props: { reviews: [review] },
      global: { plugins: [ElementPlus] },
    })

    expect(wrapper.text()).toContain(review.reviewerName)
    expect(wrapper.text()).toContain(review.targetUserName)
    expect(wrapper.text()).toContain('对方没有填写文字评价')
  })
})
