import type { EntityId, Nullable } from './common'
import type { UserRole, VerificationStatus } from './enums'

export interface LoginRequest {
  studentNo: string
  password: string
}

export interface RegisterRequest {
  studentNo: string
  nickname: string
  college: string
  phone: string
  password: string
  confirmPassword?: string
  studentCardUrl?: string
  agreementAccepted?: boolean
}

export interface LoginResponseDTO {
  token: string
  tokenType?: 'Bearer'
  expiresIn?: number
  user: UserInfoDTO
}

export interface UserInfoDTO {
  userId: EntityId
  studentNo: string
  nickname: string
  avatarUrl?: string
  role: UserRole
  creditScore: number
  verificationStatus: VerificationStatus
}

export interface UserProfileDTO extends UserInfoDTO {
  college?: string
  major?: string
  grade?: string
  phone?: string
  email?: string
  bio?: string
  createdAt?: string
  updatedAt?: string
}

export interface UserPublicDTO {
  userId: EntityId
  nickname: string
  avatarUrl?: string
  college?: string
  creditScore: number
  completedOrderCount: number
  averageRating?: number
  verificationStatus: VerificationStatus
}

export interface UserHomeDTO {
  profile: UserPublicDTO
  publishedTaskCount: number
  acceptedOrderCount: number
  completedOrderCount: number
  latestReviews?: ReviewSummaryDTO[]
}

export interface VerificationSubmitRequest {
  userId: EntityId
  realName: string
  studentNo: string
  college: string
  studentCardUrl: string
}

export interface ReviewSummaryDTO {
  reviewId: EntityId
  rating: number
  content: string
  reviewerName: string
  createdAt: string
}

export interface AuthSession {
  token: string
  user: Nullable<UserInfoDTO>
}
