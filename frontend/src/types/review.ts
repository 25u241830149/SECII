import type { EntityId } from './common'

export interface ReviewDTO {
  reviewId: EntityId
  orderId: EntityId
  reviewerId: EntityId
  reviewerName: string
  reviewerAvatarUrl?: string | null
  targetUserId: EntityId
  targetUserName: string
  targetUserAvatarUrl?: string | null
  rating: number
  content?: string | null
  createdAt: string
}

export interface ReviewCreateRequest {
  orderId: EntityId
  targetUserId: EntityId
  rating: number
  content?: string
}
