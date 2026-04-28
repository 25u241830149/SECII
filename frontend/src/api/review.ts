import { request } from '@/api/request'
import type { ReviewItem, ReviewPayload } from '@/types'

export function createReview(data: ReviewPayload) {
  return request.post<null>('/reviews', data)
}

export function getReviewDetail(reviewId: number) {
  return request.get<ReviewItem>(`/reviews/${reviewId}`)
}

export function getUserReviews(userId: number) {
  return request.get<ReviewItem[]>(`/reviews/user/${userId}`)
}