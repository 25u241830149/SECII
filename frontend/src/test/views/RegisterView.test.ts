import ElementPlus, { ElMessage } from 'element-plus'
import { flushPromises, mount } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import { describe, expect, it, vi } from 'vitest'

import { uploadStudentCard } from '@/api/upload'
import { registerUser } from '@/api/user'
import RegisterView from '@/views/auth/RegisterView.vue'

vi.mock('@/api/upload', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/api/upload')>()
  return {
    ...actual,
    uploadStudentCard: vi.fn(),
  }
})

vi.mock('@/api/user', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/api/user')>()
  return {
    ...actual,
    registerUser: vi.fn(),
  }
})

async function mountRegister() {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/register', component: RegisterView },
      { path: '/login', component: { template: '<div>login</div>' } },
    ],
  })
  await router.push('/register')
  await router.isReady()

  const wrapper = mount(RegisterView, {
    global: {
      plugins: [router, ElementPlus],
    },
  })
  await flushPromises()
  return { wrapper, router }
}

describe('RegisterView', () => {
  it('blocks submit when required fields are missing', async () => {
    const { wrapper } = await mountRegister()

    await wrapper.find('.primary-action').trigger('click')
    await flushPromises()

    expect(ElMessage.warning).toHaveBeenCalled()
    expect(uploadStudentCard).not.toHaveBeenCalled()
    expect(registerUser).not.toHaveBeenCalled()
  })

  it('rejects non-image and oversized student card files', async () => {
    const { wrapper } = await mountRegister()
    const upload = wrapper.findComponent({ name: 'ElUpload' })
    const onChange = upload.props('onChange') as (file: any, files: any[]) => void

    onChange(
      { uid: 1, raw: new File(['x'], 'a.txt', { type: 'text/plain' }) },
      [{ uid: 1, raw: new File(['x'], 'a.txt', { type: 'text/plain' }) }],
    )
    onChange(
      { uid: 2, raw: new File([new Uint8Array(6 * 1024 * 1024)], 'a.png', { type: 'image/png' }) },
      [{ uid: 2, raw: new File([new Uint8Array(6 * 1024 * 1024)], 'a.png', { type: 'image/png' }) }],
    )

    expect(ElMessage.warning).toHaveBeenCalledTimes(2)
  })

  it('uploads student card and registers successfully', async () => {
    vi.mocked(uploadStudentCard).mockResolvedValue({
      fileUrl: '/upload/student-card.png',
      contentType: 'image/png',
      size: 10,
    })
    vi.mocked(registerUser).mockResolvedValue()

    const { wrapper, router } = await mountRegister()
    const inputs = wrapper.findAll('input').filter((input) => input.attributes('type') !== 'file')
    await inputs[0].setValue('20230001')
    await inputs[1].setValue('昵称')
    await inputs[2].setValue('真实姓名')
    await inputs[3].setValue('软件工程')
    await inputs[4].setValue('123456')
    await inputs[5].setValue('123456')
    await wrapper.find('input[type="checkbox"]').setValue(true)

    const file = new File(['img'], 'card.png', { type: 'image/png' })
    const upload = wrapper.findComponent({ name: 'ElUpload' })
    const onChange = upload.props('onChange') as (file: any, files: any[]) => void
    onChange({ uid: 3, raw: file }, [{ uid: 3, raw: file }])

    await wrapper.find('.primary-action').trigger('click')
    await flushPromises()

    expect(uploadStudentCard).toHaveBeenCalledWith(file, '20230001')
    expect(registerUser).toHaveBeenCalledWith({
      studentId: '20230001',
      password: '123456',
      nickname: '昵称',
      realName: '真实姓名',
      department: '软件工程',
      studentCardImage: '/upload/student-card.png',
    })
    expect(router.currentRoute.value.path).toBe('/login')
  })
})
