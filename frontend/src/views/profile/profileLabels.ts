import type { OrderStatus, TaskCategory, TaskStatus } from '@/types'

export const profileTaskCategoryLabels: Record<TaskCategory, string> = {
  EXPRESS: '代取快递',
  STUDY: '学习辅导',
  SECOND_HAND: '二手交易',
  TEAM_UP: '活动组队',
  OTHER: '其他',
}

export const profileTaskStatusLabels: Record<TaskStatus, string> = {
  OPEN: '待接单',
  PENDING_CONFIRM: '待确认',
  IN_PROGRESS: '进行中',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  OFFLINE: '即将截止',
}

export const profileOrderStatusLabels: Record<OrderStatus, string> = {
  PENDING: '待确认',
  CONFIRMED: '进行中',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  WAITING_REVIEW: '待评价',
}

export const statusToneMap: Record<TaskStatus | OrderStatus, string> = {
  OPEN: 'blue',
  PENDING_CONFIRM: 'orange',
  IN_PROGRESS: 'blue',
  OFFLINE: 'orange',
  PENDING: 'orange',
  CONFIRMED: 'blue',
  COMPLETED: 'green',
  CANCELLED: 'red',
  WAITING_REVIEW: 'orange',
}

