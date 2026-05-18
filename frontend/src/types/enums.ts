export type UserRole = 'USER' | 'ADMIN'

export type VerificationStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

export type TaskCategory = 'EXPRESS' | 'STUDY' | 'SECOND_HAND' | 'TEAM_UP' | 'OTHER'

export type TaskStatus = 'OPEN' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' | 'OFFLINE'

export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED'

export type SortType = 'time' | 'hot'

export type PostSortType = 'latest' | 'views' | 'recommend'

export type PostCategory = 'HELP' | 'STUDY' | 'TRADE' | 'LOST_FOUND' | 'TEAM_UP' | 'OTHER'

export type MessageType = 'ORDER' | 'CHAT' | 'NOTICE' | 'SYSTEM'

export const userRoleLabels: Record<UserRole, string> = {
  USER: '普通用户',
  ADMIN: '管理员',
}

export const verificationStatusLabels: Record<VerificationStatus, string> = {
  PENDING: '待审核',
  APPROVED: '已通过',
  REJECTED: '已驳回',
}

export const taskCategoryLabels: Record<TaskCategory, string> = {
  EXPRESS: '快递代取',
  STUDY: '学习辅导',
  SECOND_HAND: '二手交易',
  TEAM_UP: '活动组队',
  OTHER: '其他',
}

export const taskStatusLabels: Record<TaskStatus, string> = {
  OPEN: '待接单',
  IN_PROGRESS: '进行中',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  OFFLINE: '已下架',
}

export const orderStatusLabels: Record<OrderStatus, string> = {
  PENDING: '待确认',
  CONFIRMED: '进行中',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
}
