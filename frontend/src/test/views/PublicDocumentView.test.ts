import ElementPlus from 'element-plus'
import { flushPromises, mount } from '@vue/test-utils'
import { createMemoryHistory, createRouter } from 'vue-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import PublicLayout from '@/layouts/PublicLayout.vue'
import PublicDocumentView from '@/views/system/PublicDocumentView.vue'
import RegisterView from '@/views/auth/RegisterView.vue'

async function mountPublicLayout(initialPath: string) {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      {
        path: '/',
        component: PublicLayout,
        children: [
          {
            path: 'login',
            component: { template: '<div>login</div>' },
          },
          {
            path: 'register',
            component: RegisterView,
          },
        ],
      },
    ],
  })

  await router.push(initialPath)
  await router.isReady()

  const wrapper = mount(PublicLayout, {
    global: {
      plugins: [router, ElementPlus],
      stubs: {
        CampusHubLogo: { template: '<div>logo</div>' },
      },
    },
  })

  await flushPromises()

  return { wrapper, router }
}

async function mountHelpCenter(initialPath = '/help-center') {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      {
        path: '/help-center',
        name: 'help-center',
        component: PublicDocumentView,
        meta: {
          title: '帮助中心',
          publicDocumentKey: 'help-center',
        },
      },
      {
        path: '/login',
        component: { template: '<div>login</div>' },
      },
    ],
  })

  await router.push(initialPath)
  await router.isReady()

  const wrapper = mount(PublicDocumentView, {
    attachTo: document.body,
    global: {
      plugins: [router, ElementPlus],
      stubs: {
        CampusHubLogo: { template: '<div>logo</div>' },
      },
    },
  })

  await flushPromises()

  return { wrapper, router }
}

describe('PublicDocumentView', () => {
  beforeEach(() => {
    HTMLElement.prototype.scrollIntoView = vi.fn()
  })

  it('renders help center as a standalone handbook-style page', async () => {
    const { wrapper } = await mountHelpCenter()

    expect(wrapper.find('.help-manual-page').exists()).toBe(true)
    expect(wrapper.find('.help-manual-sidebar').exists()).toBe(true)
    expect(wrapper.find('.help-manual-hero--auth-surface').exists()).toBe(true)
    expect(wrapper.find('.help-manual-hero-illustration').exists()).toBe(true)
    expect(wrapper.findAll('.help-toc-link')).toHaveLength(10)
    expect(wrapper.text()).toContain('帮助中心')
    expect(wrapper.text()).toContain('常见问题')
    expect(wrapper.text()).toContain('返回登录')
  })

  it('scrolls to the matching section when the hash changes', async () => {
    const { wrapper, router } = await mountHelpCenter()
    const targetHref = wrapper.findAll('.help-toc-link')[3]?.attributes('href') ?? ''
    const targetHash = targetHref.split('#')[1] ?? ''

    await router.push({
      name: 'help-center',
      hash: `#${targetHash}`,
    })
    await flushPromises()

    expect(HTMLElement.prototype.scrollIntoView).toHaveBeenCalled()
  })

  it('opens service terms dialog from register view actions', async () => {
    const { wrapper, router } = await mountPublicLayout('/register')

    const legalButtons = wrapper.findAll('.inline-link-button')
    expect(legalButtons).toHaveLength(2)

    await legalButtons[0].trigger('click')
    await flushPromises()

    expect(router.currentRoute.value.query.dialog).toBe('terms-of-service')
    expect(wrapper.text()).toContain('平台使用规则与双方责任说明')
  })
})
