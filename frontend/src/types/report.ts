import type { EntityId, PageQuery } from './common'

export type ReportTargetType = 'USER' | 'TASK' | 'ORDER' | 'POST' | 'COMMENT'
export type ReportStatus = 'PENDING' | 'HANDLED' | 'REJECTED'

export interface ReportCreateRequest {
  targetType: ReportTargetType
  targetId: EntityId
  targetUserId?: EntityId
  reason: string
}

export interface ReportHandleRequest {
  status: Exclude<ReportStatus, 'PENDING'>
  result: string
}

export interface ReportDTO {
  reportId: EntityId
  reporterId: EntityId
  reporterName: string
  targetUserId: EntityId
  targetUserName: string
  handlerId?: EntityId | null
  handlerName?: string | null
  taskId?: EntityId | null
  orderId?: EntityId | null
  postId?: EntityId | null
  commentId?: EntityId | null
  targetType: ReportTargetType
  targetId: EntityId
  reason: string
  status: ReportStatus
  result?: string | null
  createdAt: string
  updatedAt: string
}

export interface ReportListQuery extends PageQuery {
  status?: ReportStatus
}

export interface AdminDashboardStatsDTO {
  totalUsers: number
  pendingVerifications: number
  bannedUsers: number
  openTasks: number
  inProgressTasks: number
  completedTasks: number
  totalOrders: number
  completedOrders: number
  pendingReports: number
  totalReports: number
  unreadMessages: number
  totalReviews: number
  averageRating?: number | string | null
}

export const reportTargetTypeLabels: Record<ReportTargetType, string> = {
  USER: '用户',
  TASK: '任务',
  ORDER: '订单',
  POST: '帖子',
  COMMENT: '评论',
}

export const reportStatusLabels: Record<ReportStatus, string> = {
  PENDING: '待处理',
  HANDLED: '已处理',
  REJECTED: '已驳回',
}
