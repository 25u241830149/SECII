import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

import CampusHubLogo from '@/components/CampusHubLogo.vue'

describe('CampusHubLogo', () => {
  it('renders the default medium variant', () => {
    const wrapper = mount(CampusHubLogo)

    expect(wrapper.classes()).toContain('campushub-logo--md')
    expect(wrapper.attributes('aria-hidden')).toBe('true')
    expect(wrapper.find('svg').exists()).toBe(true)
  })

  it('switches size classes from props', async () => {
    const wrapper = mount(CampusHubLogo, {
      props: {
        size: 'sm',
      },
    })

    expect(wrapper.classes()).toContain('campushub-logo--sm')

    await wrapper.setProps({ size: 'lg' })
    expect(wrapper.classes()).toContain('campushub-logo--lg')
    expect(wrapper.classes()).not.toContain('campushub-logo--sm')
  })
})
