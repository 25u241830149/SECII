import ElementPlus from 'element-plus'
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

import LegalDocumentDialog from '@/views/system/LegalDocumentDialog.vue'

describe('LegalDocumentDialog', () => {
  it('uses the rounded legal dialog layout defaults', () => {
    const wrapper = mount(LegalDocumentDialog, {
      props: {
        modelValue: true,
        documentKey: 'privacy-policy',
      },
      global: {
        plugins: [ElementPlus],
      },
    })

    const dialog = wrapper.getComponent({ name: 'ElDialog' })

    expect(dialog.props('width')).toBe('min(700px, calc(100vw - 48px))')
    expect(dialog.props('top')).toBe('14vh')
  })
})
