import type { EntityId, Nullable } from './common'
import type { UserRole, VerificationStatus } from './enums'

export interface LoginRequest {
  studentId: string
  password: string
}

export interface RegisterRequest {
  studentId: string
  password: string
  nickname: string
  realName: string
  department?: string
  studentCardImage: string
}

export interface LoginResponseDTO {
  token: string
  user: UserInfoDTO
}

export interface UserInfoDTO {
  userId: EntityId
  studentId: string
  email?: string
  phone?: string
  nickname: string
  realName?: string
  department?: string
  avatarUrl?: string
  role: UserRole
  creditScore: number
  verificationStatus: VerificationStatus
}

export interface UserProfileDTO extends UserInfoDTO {
  createTime?: string
  updateTime?: string
}

export interface UserProfileUpdateRequest {
  email?: string
  phone?: string
  nickname: string
  avatarUrl?: string
}

export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
  confirmPassword: string
}

export interface UploadResultDTO {
  fileUrl: string
  originalFilename?: string
  contentType: string
  size: number
}

export interface UserPublicDTO {
  userId: EntityId
  nickname: string
  avatarUrl?: string
  department?: string | null
  creditScore: number
  creditLevel: string
}

export interface UserHomeDTO {
  userId: EntityId
  nickname: string
  avatarUrl?: string
  creditScore: number
  creditLevel: string
  publishedTaskCount: number
  completedOrderCount: number
  reviewCount: number
  averageRating: number | null
}

export interface CreditRecordDTO {
  recordId: EntityId
  reason: string
  delta: number
  scoreAfter: number
  createdAt: string
}

export interface CreditDTO {
  creditScore: number
  creditLevel: string
  completedRate: number
  cancelledRate: number
  averageRating: number | null
  reviewCount: number
  publishedTaskCount: number
  completedOrderCount: number
  cancelledOrderCount: number
  recentRecords: CreditRecordDTO[]
}

export interface VerificationSubmitRequest {
  userId: EntityId
  realName: string
  studentId: string
  college: string
  studentCardImage: string
}

export interface VerificationStatusDTO {
  verificationStatus: VerificationStatus
}

export interface AuthSession {
  token: string
  user: Nullable<UserInfoDTO>
}
