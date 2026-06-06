import ElementPlus from 'element-plus'
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

import TaskCard from '@/components/TaskCard.vue'
import { taskCategoryLabels, taskStatusLabels } from '@/types'
import { createTaskListItem } from '@/test/factories'

describe('TaskCard', () => {
  it('renders task content and fallback location', () => {
    const task = createTaskListItem({
      reward: 10,
      location: null,
      favorited: true,
    })

    const wrapper = mount(TaskCard, {
      props: {
        task,
        showFavorite: true,
        showGrab: true,
      },
      global: {
        plugins: [ElementPlus],
      },
    })

    expect(wrapper.text()).toContain(task.publisherName)
    expect(wrapper.text()).toContain(String(task.publisherCreditScore))
    expect(wrapper.find('h3').text()).toBe(task.title)
    expect(wrapper.text()).toContain('10.00')
    expect(wrapper.text()).toContain('校内待定地点')
    expect(wrapper.find('.meta-line i').text()).toBe(taskCategoryLabels[task.category])
    expect(wrapper.find('.favorite-button').classes()).toContain('active')
  })

  it('emits favorite and view actions with the full task payload', async () => {
    const task = createTaskListItem()
    const wrapper = mount(TaskCard, {
      props: {
        task,
        showFavorite: true,
        showGrab: true,
      },
      global: {
        plugins: [ElementPlus],
      },
    })

    await wrapper.find('.favorite-button').trigger('click')
    await wrapper.findAll('.actions button')[1].trigger('click')

    expect(wrapper.emitted('favorite')?.[0]).toEqual([task])
    expect(wrapper.emitted('view')?.[0]).toEqual([task])
  })

  it('disables the grab button for non-joinable tasks and shows status text', () => {
    const task = createTaskListItem({
      category: 'EXPRESS',
      status: 'IN_PROGRESS',
    })

    const wrapper = mount(TaskCard, {
      props: {
        task,
        showGrab: true,
      },
      global: {
        plugins: [ElementPlus],
      },
    })

    const grabButton = wrapper.findAll('.actions button')[1]
    expect(grabButton.attributes('disabled')).toBeDefined()
    expect(grabButton.text()).toContain(taskStatusLabels.IN_PROGRESS)
  })

  it('keeps the grab button available for team-up tasks already in progress', async () => {
    const task = createTaskListItem({
      category: 'TEAM_UP',
      status: 'IN_PROGRESS',
    })

    const wrapper = mount(TaskCard, {
      props: {
        task,
        showGrab: true,
      },
      global: {
        plugins: [ElementPlus],
      },
    })

    const grabButton = wrapper.findAll('.actions button')[1]
    expect(grabButton.attributes('disabled')).toBeUndefined()

    await grabButton.trigger('click')
    expect(wrapper.emitted('grab')?.[0]).toEqual([task])
  })
})
