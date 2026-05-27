import { taskCategoryLabels } from '@/types'
import type { EntityId, TaskCategory, TaskListDTO } from '@/types'

export interface TaskViewProfileEntry {
  taskId: EntityId
  category: TaskCategory
  title: string
  description: string
  keywords: string[]
  viewedAt: number
}

const PROFILE_STORAGE_PREFIX = 'campushub:task-view-profile'
const MAX_PROFILE_SIZE = 30
const STOP_WORDS = new Set([
  '一个',
  '可以',
  '任务',
  '需求',
  '帮忙',
  '同学',
  '校园',
  '查看',
  '详情',
  '发布',
])

const profileKey = (userId?: EntityId | null) => `${PROFILE_STORAGE_PREFIX}:${userId ?? 'guest'}`

const normalizeText = (value: string) => {
  return value
    .toLowerCase()
    .replace(/【[^】]+】/g, ' ')
    .replace(/[^\p{Script=Han}a-z0-9]+/gu, ' ')
    .trim()
}

export const extractTaskKeywords = (task: Pick<TaskListDTO, 'title' | 'description'>) => {
  const text = normalizeText(`${task.title || ''} ${task.description || ''}`)
  const latinTokens = text.match(/[a-z0-9]{2,}/g) || []
  const chineseChars = Array.from(text.replace(/[^\p{Script=Han}]/gu, ''))
  const chineseBigrams = chineseChars
    .slice(0, -1)
    .map((char, index) => `${char}${chineseChars[index + 1]}`)

  return Array.from(new Set([...latinTokens, ...chineseBigrams]))
    .filter((token) => token.length >= 2 && !STOP_WORDS.has(token))
    .slice(0, 40)
}

const isProfileEntry = (value: unknown): value is TaskViewProfileEntry => {
  if (!value || typeof value !== 'object') return false
  const entry = value as Partial<TaskViewProfileEntry>
  return (
    typeof entry.taskId === 'number' &&
    typeof entry.category === 'string' &&
    typeof entry.title === 'string' &&
    Array.isArray(entry.keywords) &&
    typeof entry.viewedAt === 'number'
  )
}

export const readTaskViewProfile = (userId?: EntityId | null): TaskViewProfileEntry[] => {
  try {
    const value = window.localStorage.getItem(profileKey(userId))
    if (!value) return []
    const parsed = JSON.parse(value)
    if (!Array.isArray(parsed)) return []
    return parsed.filter(isProfileEntry).slice(0, MAX_PROFILE_SIZE)
  } catch {
    return []
  }
}

export const rememberTaskView = (task: TaskListDTO, userId?: EntityId | null) => {
  const entry: TaskViewProfileEntry = {
    taskId: task.taskId,
    category: task.category,
    title: task.title,
    description: task.description || '',
    keywords: extractTaskKeywords(task),
    viewedAt: Date.now(),
  }
  const profile = readTaskViewProfile(userId)
  const nextProfile = [entry, ...profile.filter((item) => item.taskId !== task.taskId)].slice(0, MAX_PROFILE_SIZE)

  try {
    window.localStorage.setItem(profileKey(userId), JSON.stringify(nextProfile))
  } catch {
    // Local storage may be disabled; recommendations still fall back to hot tasks.
  }

  return nextProfile
}

const keywordSimilarity = (taskKeywords: Set<string>, profileKeywords: string[]) => {
  if (!taskKeywords.size || !profileKeywords.length) return 0
  const overlap = profileKeywords.filter((keyword) => taskKeywords.has(keyword)).length
  return overlap / Math.sqrt(taskKeywords.size * profileKeywords.length)
}

const recencyScore = (createdAt?: string) => {
  const createdTime = createdAt ? new Date(createdAt).getTime() : 0
  if (!createdTime || Number.isNaN(createdTime)) return 0
  const ageDays = Math.max(0, (Date.now() - createdTime) / 86_400_000)
  return Math.max(0, 1 - ageDays / 14)
}

export const rankSmartTaskRecommendations = (
  candidates: TaskListDTO[],
  profile: TaskViewProfileEntry[],
) => {
  const openCandidates = candidates.filter(
    (task) => task.status === 'OPEN' || (task.category === 'TEAM_UP' && task.status === 'IN_PROGRESS'),
  )
  const source = openCandidates.length ? openCandidates : candidates
  const viewedTaskIds = new Set(profile.map((item) => item.taskId))

  const scored = source
    .map((task, index) => {
      const taskKeywords = new Set(extractTaskKeywords(task))
      const favoriteScore = Math.min(Number(task.favoriteCount ?? 0), 20) * 0.2
      const hotOrderScore = Math.max(0, source.length - index) * 0.01
      const profileScore = profile.reduce((total, item, profileIndex) => {
        const decay = Math.pow(0.82, profileIndex)
        const categoryScore = item.category === task.category ? 18 * decay : 0
        const contentScore = keywordSimilarity(taskKeywords, item.keywords) * 32 * decay
        return total + categoryScore + contentScore
      }, 0)

      return {
        task,
        score: profileScore + favoriteScore + recencyScore(task.createdAt) + hotOrderScore,
        viewed: viewedTaskIds.has(task.taskId),
      }
    })
    .sort((left, right) => {
      if (right.score !== left.score) return right.score - left.score
      return new Date(right.task.createdAt).getTime() - new Date(left.task.createdAt).getTime()
    })

  if (!profile.length) return scored.map((item) => item.task)

  return [
    ...scored.filter((item) => !item.viewed),
    ...scored.filter((item) => item.viewed),
  ].map((item) => item.task)
}

export const getRecommendationHint = (profile: TaskViewProfileEntry[]) => {
  if (!profile.length) {
    return '根据热门需求和最新发布，以下需求可能适合你'
  }

  const categoryScores = profile.reduce<Record<TaskCategory, number>>((scores, item, index) => {
    scores[item.category] = (scores[item.category] || 0) + Math.pow(0.82, index)
    return scores
  }, {} as Record<TaskCategory, number>)
  const topCategory = Object.entries(categoryScores).sort((left, right) => right[1] - left[1])[0]?.[0] as
    | TaskCategory
    | undefined

  return topCategory
    ? `根据你最近查看的${taskCategoryLabels[topCategory]}和相似内容，以下需求可能适合你`
    : '根据你最近查看的需求内容，以下需求可能适合你'
}
