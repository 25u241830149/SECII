import { beforeEach, describe, expect, it } from 'vitest'

import {
  extractTaskKeywords,
  getRecommendationHint,
  rankSmartTaskRecommendations,
  readTaskViewProfile,
  rememberTaskView,
} from '@/utils/taskRecommendation'
import { createTaskListItem } from '@/test/factories'

describe('taskRecommendation utils', () => {
  beforeEach(() => {
    window.localStorage.clear()
  })

  it('extracts meaningful keywords from title and description', () => {
    const keywords = extractTaskKeywords({
      title: '高数辅导',
      description: '需要一对一高数考试辅导，今晚自习室见',
    })

    expect(keywords.length).toBeGreaterThan(0)
    expect(keywords.some((keyword) => keyword.includes('高数'))).toBe(true)
  })

  it('stores viewed tasks with de-duplication and latest-first ordering', () => {
    const first = createTaskListItem({ taskId: 1, title: '快递代取' })
    const second = createTaskListItem({ taskId: 2, title: '高数辅导', category: 'STUDY' })

    rememberTaskView(first, 1)
    const profile = rememberTaskView(second, 1)
    const updated = rememberTaskView(first, 1)

    expect(profile).toHaveLength(2)
    expect(updated[0].taskId).toBe(1)
    expect(updated.map((item) => item.taskId)).toEqual([1, 2])
    expect(readTaskViewProfile(1).map((item) => item.taskId)).toEqual([1, 2])
  })

  it('prefers unseen matching tasks over already viewed tasks', () => {
    const viewed = createTaskListItem({
      taskId: 1,
      category: 'STUDY',
      title: '高数辅导',
      description: '高数题目讲解',
      favoriteCount: 1,
    })
    const unseenMatch = createTaskListItem({
      taskId: 2,
      category: 'STUDY',
      title: '线代辅导',
      description: '高数与线代答疑',
      favoriteCount: 2,
    })
    const unrelated = createTaskListItem({
      taskId: 3,
      category: 'EXPRESS',
      title: '代取外卖',
      description: '食堂门口',
      favoriteCount: 20,
    })

    const profile = rememberTaskView(viewed, 1)
    const ranked = rankSmartTaskRecommendations([viewed, unseenMatch, unrelated], profile)

    expect(ranked[0].taskId).toBe(2)
    expect(ranked[ranked.length - 1].taskId).toBe(1)
  })

  it('returns a hot-task fallback hint when no profile exists', () => {
    expect(getRecommendationHint([])).toContain('热门')
  })
})
