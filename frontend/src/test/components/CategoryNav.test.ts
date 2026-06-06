import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

import CategoryNav from '@/components/CategoryNav.vue'

describe('CategoryNav', () => {
  it('renders categories and emits model updates', async () => {
    const wrapper = mount(CategoryNav, {
      props: {
        modelValue: 'ALL',
      },
    })

    expect(wrapper.findAll('button')).toHaveLength(6)
    expect(wrapper.findAll('button')[0].classes()).toContain('active')
    await wrapper.findAll('button')[1].trigger('click')

    expect(wrapper.emitted('update:modelValue')?.[0]).toEqual(['EXPRESS'])
  })
})
