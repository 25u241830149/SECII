import type { EntityId, PageQuery } from './common'
import type { OrderStatus, SortType, TaskCategory, TaskStatus } from './enums'

export type TaskStatusFilter = 'ALL' | 'OPEN' | 'PENDING_CONFIRM' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
export type RewardTypeFilter = 'ALL' | 'PAID' | 'FREE'
export type LocationTypeFilter = 'ALL' | 'WITH_LOCATION' | 'UNSPECIFIED'

export interface TaskListDTO {
  taskId: EntityId
  title: string
  description: string
  category: TaskCategory
  status: TaskStatus
  reward: number | string
  location?: string | null
  longitude?: number | null
  latitude?: number | null
  deadlineTime?: string | null
  itemImageUrl?: string | null
  originalPrice?: number | string | null
  teamTotalMembers?: number | null
  teamCurrentMembers?: number | null
  activityTime?: string | null
  activityNote?: string | null
  publisherId: EntityId
  publisherName: string
  publisherAvatarUrl?: string | null
  publisherCreditScore?: number | null
  favoriteCount?: number
  favorited?: boolean
  createdAt: string
  updatedAt?: string
}

export interface TaskDetailDTO extends TaskListDTO {
  publisherDepartment?: string | null
}

export interface TaskListQuery extends PageQuery {
  category?: TaskCategory
  sort?: SortType
  publisherId?: EntityId
  status?: Exclude<TaskStatusFilter, 'ALL'>
  rewardType?: Exclude<RewardTypeFilter, 'ALL'>
  locationType?: Exclude<LocationTypeFilter, 'ALL'>
  excludeCompleted?: boolean
}

export interface TaskStatsDTO {
  todayCreated: number
  inProgress: number
  completed: number
}

export interface FavoriteTaskQuery extends TaskListQuery {
  userId: EntityId
}

export interface TaskMutationPayload {
  title: string
  description: string
  category: TaskCategory
  reward: number | string
  location?: string
  longitude?: number
  latitude?: number
  deadlineTime?: string | Date | null
  itemImageUrl?: string | null
  originalPrice?: number | string | null
  teamTotalMembers?: number | null
  teamCurrentMembers?: number | null
  activityTime?: string | Date | null
  activityNote?: string | null
}

export interface TaskFavoriteResultDTO {
  taskId: EntityId
  favorited: boolean
}

export interface GrabOrderRequest {
  taskId: EntityId
}

export interface OrderListQuery extends PageQuery {
  userId: EntityId
  role?: 'poster' | 'helper'
  status?: OrderStatus
}

export interface OrderStatsDTO {
  waitingAcceptance: number
  pending: number
  inProgress: number
  waitingReview: number
  completed: number
}

export interface OrderListDTO {
  orderId: EntityId
  taskId: EntityId
  taskTitle: string
  taskCategory: TaskCategory
  taskStatus?: TaskStatus
  taskLocation?: string | null
  reward: number | string
  status: OrderStatus
  currentUserReviewed?: boolean
  teamTotalMembers?: number | null
  teamCurrentMembers?: number | null
  posterId: EntityId
  posterName: string
  posterAvatarUrl?: string | null
  helperId: EntityId
  helperName: string
  helperAvatarUrl?: string | null
  createdAt: string
  updatedAt?: string
}

export interface OrderDetailDTO extends OrderListDTO {
  taskDescription: string
  posterCreditScore?: number | null
  helperCreditScore?: number | null
}

export type TaskDTO = TaskListDTO
export type CreateTaskRequest = TaskMutationPayload
export type OrderDTO = OrderListDTO
