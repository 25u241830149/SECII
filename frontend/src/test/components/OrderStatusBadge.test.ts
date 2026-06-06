import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

import OrderStatusBadge from '@/components/OrderStatusBadge.vue'

describe('OrderStatusBadge', () => {
  it('maps statuses to the correct tone class and default label', () => {
    const confirmed = mount(OrderStatusBadge, { props: { status: 'CONFIRMED' } })
    const waitingReview = mount(OrderStatusBadge, { props: { status: 'WAITING_REVIEW' } })
    const pending = mount(OrderStatusBadge, { props: { status: 'PENDING' } })

    expect(confirmed.classes()).toContain('green')
    expect(confirmed.text()).toContain('进行中')
    expect(waitingReview.classes()).toContain('purple')
    expect(waitingReview.text()).toContain('待评价')
    expect(pending.classes()).toContain('orange')
  })

  it('supports label override and tone override', () => {
    const wrapper = mount(OrderStatusBadge, {
      props: {
        status: 'CANCELLED',
        label: '已关闭',
        toneOverride: 'blue',
      },
    })

    expect(wrapper.classes()).toContain('blue')
    expect(wrapper.classes()).not.toContain('red')
    expect(wrapper.text()).toBe('已关闭')
  })
})
