import type { EntityId, PageQuery } from './common'
import type { OrderStatus, SortType, TaskCategory, TaskStatus } from './enums'

export interface TaskDTO {
  taskId: EntityId
  title: string
  description: string
  category: TaskCategory
  status: TaskStatus
  reward: string
  location?: string
  deadline?: string
  publisherId: EntityId
  publisherName: string
  publisherAvatarUrl?: string
  publisherCreditScore?: number
  createdAt: string
  updatedAt?: string
}

export interface TaskQuery extends PageQuery {
  category?: TaskCategory
  status?: TaskStatus
  sort?: SortType
  locationScope?: 'nearby' | 'college' | 'school'
}

export interface CreateTaskRequest {
  title: string
  description: string
  category: TaskCategory
  reward: string
  location?: string
  deadline?: string
}

export interface OrderDTO {
  orderId: EntityId
  taskId: EntityId
  taskTitle: string
  status: OrderStatus
  requesterId: EntityId
  helperId: EntityId
  createdAt: string
  updatedAt?: string
}
