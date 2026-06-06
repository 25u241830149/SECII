import ElementPlus, { ElMessage, ElMessageBox } from 'element-plus'
import { createPinia } from 'pinia'
import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { createTaskComment, deleteTaskComment, getTaskComments } from '@/api/task'
import TaskCommentSection from '@/components/TaskCommentSection.vue'
import { useAuthStore } from '@/stores'
import { createUserInfo } from '@/test/factories'

vi.mock('@/api/task', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/api/task')>()
  return {
    ...actual,
    getTaskComments: vi.fn(),
    createTaskComment: vi.fn(),
    deleteTaskComment: vi.fn(),
  }
})

const ElDropdownStub = {
  name: 'ElDropdown',
  emits: ['command'],
  template: '<div class="el-dropdown-stub"><slot /><slot name="dropdown" /></div>',
}

const ElDropdownMenuStub = {
  name: 'ElDropdownMenu',
  template: '<div class="el-dropdown-menu-stub"><slot /></div>',
}

const ElDropdownItemStub = {
  name: 'ElDropdownItem',
  props: ['command'],
  template: '<button type="button" class="el-dropdown-item-stub"><slot /></button>',
}

async function mountComments(authenticated = true) {
  const pinia = createPinia()
  const authStore = useAuthStore(pinia)
  if (authenticated) {
    authStore.setSession({
      token: 'token',
      user: createUserInfo(),
    })
  }

  const wrapper = mount(TaskCommentSection, {
    props: { taskId: 1 },
    global: {
      plugins: [pinia, ElementPlus],
      stubs: {
        ElDropdown: ElDropdownStub,
        ElDropdownMenu: ElDropdownMenuStub,
        ElDropdownItem: ElDropdownItemStub,
      },
    },
  })

  await flushPromises()
  return { wrapper }
}

describe('TaskCommentSection', () => {
  beforeEach(() => {
    vi.mocked(ElMessageBox.confirm).mockResolvedValue('confirm' as never)
    vi.mocked(getTaskComments).mockResolvedValue([
      {
        commentId: 1,
        taskId: 1,
        authorId: 1,
        authorName: 'commenter',
        content: 'first comment',
        likeCount: 0,
        likedByMe: false,
        createdAt: '2026-06-06T10:00:00',
      },
    ])
  })

  it('loads comments and allows authenticated users to submit', async () => {
    vi.mocked(createTaskComment).mockResolvedValue([
      {
        commentId: 2,
        taskId: 1,
        authorId: 1,
        authorName: 'commenter',
        content: 'second comment',
        createdAt: '2026-06-06T10:10:00',
      },
    ] as never)

    const { wrapper } = await mountComments()
    await wrapper.find('textarea').setValue('second comment')
    await wrapper.find('.composer button').trigger('click')
    await flushPromises()

    expect(getTaskComments).toHaveBeenCalledWith(1, 'time')
    expect(createTaskComment).toHaveBeenCalledWith(1, {
      content: 'second comment',
      parentCommentId: null,
      replyToUserId: null,
    })
    expect(ElMessage.success).toHaveBeenCalled()
    expect((wrapper.find('textarea').element as HTMLTextAreaElement).value).toBe('')
  })

  it('hides composer for guests', async () => {
    const { wrapper } = await mountComments(false)
    expect(wrapper.find('.composer').exists()).toBe(false)
  })

  it('allows authors to delete their own comments', async () => {
    vi.mocked(deleteTaskComment).mockResolvedValue(undefined)

    const { wrapper } = await mountComments()
    wrapper.findComponent({ name: 'ElDropdown' }).vm.$emit('command', 'delete')
    await flushPromises()

    expect(deleteTaskComment).toHaveBeenCalledWith(1, 1)
    expect(getTaskComments).toHaveBeenCalledTimes(2)
  })

  it('ignores blank comment submissions', async () => {
    const { wrapper } = await mountComments()
    await wrapper.find('textarea').setValue('   ')
    await wrapper.find('.composer button').trigger('click')
    await flushPromises()

    expect(createTaskComment).not.toHaveBeenCalled()
  })
})
