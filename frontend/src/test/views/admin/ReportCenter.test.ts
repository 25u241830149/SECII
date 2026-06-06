import ElementPlus, { ElMessage } from 'element-plus'
import { flushPromises, shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { getAdminReports, handleReport } from '@/api/admin'
import { createPage, createReport } from '@/test/factories'
import ReportCenter from '@/views/admin/ReportCenter.vue'

vi.mock('@/api/admin', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/api/admin')>()
  return {
    ...actual,
    getAdminReports: vi.fn(),
    handleReport: vi.fn(),
  }
})

describe('Admin ReportCenter view', () => {
  beforeEach(() => {
    vi.mocked(getAdminReports).mockResolvedValue(createPage([createReport()], { total: 1 }))
    vi.mocked(handleReport).mockResolvedValue({} as any)
  })

  it('loads reports and reloads when filter changes', async () => {
    const wrapper = shallowMount(ReportCenter, {
      global: {
        plugins: [ElementPlus],
      },
    })
    await flushPromises()

    ;(wrapper.vm as any).statusFilter = 'HANDLED'
    ;(wrapper.vm as any).reload()
    await flushPromises()

    expect(getAdminReports).toHaveBeenLastCalledWith({
      status: 'HANDLED',
      page: 1,
      size: 10,
    })
  })

  it('opens handle dialog and submits result', async () => {
    const wrapper = shallowMount(ReportCenter, {
      global: {
        plugins: [ElementPlus],
      },
    })
    await flushPromises()

    ;(wrapper.vm as any).openHandleDialog(createReport())
    ;(wrapper.vm as any).handleForm.result = '  已完成处理  '
    await (wrapper.vm as any).submitHandle()
    await flushPromises()

    expect(handleReport).toHaveBeenCalledWith(1, {
      status: 'HANDLED',
      result: '已完成处理',
    })
  })

  it('warns when handle result is empty', async () => {
    const wrapper = shallowMount(ReportCenter, {
      global: {
        plugins: [ElementPlus],
      },
    })
    await flushPromises()

    ;(wrapper.vm as any).openHandleDialog(createReport())
    ;(wrapper.vm as any).handleForm.result = '   '
    await (wrapper.vm as any).submitHandle()

    expect(ElMessage.warning).toHaveBeenCalled()
    expect(handleReport).not.toHaveBeenCalled()
  })
})
