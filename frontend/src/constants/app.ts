import type { PublishCategory } from '@/types'

export const categoryLabelMap: Record<PublishCategory, string> = {
  EXPRESS: '快递代取',
  STUDY: '学习辅导',
  SECOND_HAND: '二手交易',
  TEAM_UP: '活动组队',
  OTHER: '其他互助',
}

export const categoryOptions = Object.entries(categoryLabelMap).map(([value, label]) => ({
  value: value as PublishCategory,
  label,
  path: `/publish/${value}`,
}))

export const mainNavItems = [
  { label: '首页', path: '/' },
  { label: '我的', path: '/me/profile' },
  { label: '论坛', path: '/forum' },
  { label: '消息', path: '/messages' },
]

export const profileNavItems = [
  { label: '个人资料', path: '/me/profile' },
  { label: '我的足迹', path: '/me/tracks' },
  { label: '我的收藏', path: '/me/favorites' },
  { label: '我的发单', path: '/me/tasks' },
  { label: '我的接单', path: '/me/orders' },
  { label: '信用等级', path: '/me/credit' },
  { label: '账户设置', path: '/me/account' },
]

export const adminNavItems = [
  { label: '数据总览', path: '/admin/stats' },
  { label: '用户管理', path: '/admin/users' },
  { label: '任务管理', path: '/admin/tasks' },
  { label: '帖子管理', path: '/admin/posts' },
]

export function getCategoryLabel(category?: string) {
  if (!category) {
    return '全部任务'
  }

  return categoryLabelMap[category as PublishCategory] ?? '全部任务'
}