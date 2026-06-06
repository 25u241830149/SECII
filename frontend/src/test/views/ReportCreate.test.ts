import ElementPlus, { ElMessage } from 'element-plus'
import { flushPromises, mount } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import { describe, expect, it, vi } from 'vitest'

import { createReport } from '@/api/report'
import ReportCreate from '@/views/report/ReportCreate.vue'

vi.mock('@/api/report', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/api/report')>()
  return {
    ...actual,
    createReport: vi.fn(),
  }
})

async function mountReport(initialPath = '/reports/create') {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/reports/create', component: ReportCreate },
      { path: '/messages', component: { template: '<div>messages</div>' } },
    ],
  })
  await router.push(initialPath)
  await router.isReady()

  const wrapper = mount(ReportCreate, {
    global: {
      plugins: [router, ElementPlus],
      stubs: {
        ElSelect: true,
        ElOption: true,
      },
    },
  })
  await flushPromises()
  return { wrapper, router }
}

describe('ReportCreate view', () => {
  it('requires a target user id when reporting non-user targets', async () => {
    const { wrapper } = await mountReport('/reports/create?targetType=TASK&targetId=10')
    await wrapper.find('textarea').setValue('举报原因')
    await wrapper.find('.actions .el-button--primary').trigger('click')
    await flushPromises()

    expect(ElMessage.warning).toHaveBeenCalled()
    expect(createReport).not.toHaveBeenCalled()
  })

  it('requires non-empty reason text', async () => {
    const { wrapper } = await mountReport('/reports/create?targetType=USER&targetId=10')
    await wrapper.find('.actions .el-button--primary').trigger('click')
    await flushPromises()

    expect(ElMessage.warning).toHaveBeenCalled()
    expect(createReport).not.toHaveBeenCalled()
  })

  it('submits trimmed report payload and redirects to messages', async () => {
    vi.mocked(createReport).mockResolvedValue({} as any)
    const { wrapper, router } = await mountReport(
      '/reports/create?targetType=ORDER&targetId=10&targetUserId=20',
    )

    await wrapper.find('textarea').setValue('  订单存在违规行为  ')
    await wrapper.find('.actions .el-button--primary').trigger('click')
    await flushPromises()

    expect(createReport).toHaveBeenCalledWith({
      targetType: 'ORDER',
      targetId: 10,
      targetUserId: 20,
      reason: '订单存在违规行为',
    })
    expect(router.currentRoute.value.path).toBe('/messages')
  })
})
