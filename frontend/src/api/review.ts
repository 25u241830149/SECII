import { apiGet, apiPost } from './request'
import type { EntityId, PageResponse, ReviewCreateRequest, ReviewDTO } from '@/types'

export function createReview(payload: ReviewCreateRequest) {
  return apiPost<ReviewDTO, ReviewCreateRequest>('/reviews', payload)
}

export function getReview(reviewId: EntityId) {
  return apiGet<ReviewDTO>(`/reviews/${reviewId}`)
}

export function getUserReviews(userId: EntityId, query: { page?: number; size?: number } = {}) {
  return apiGet<PageResponse<ReviewDTO>>(`/reviews/user/${userId}`, { params: query })
}

export function getOrderReviews(orderId: EntityId) {
  return apiGet<ReviewDTO[]>(`/reviews/order/${orderId}`)
}
