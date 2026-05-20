import { apiDelete, apiGet, apiPost, apiPut } from './request'
import type {
  CreditDTO,
  EntityId,
  LoginRequest,
  LoginResponseDTO,
  RegisterRequest,
  UserHomeDTO,
  UserProfileDTO,
  UserProfileUpdateRequest,
  UserPublicDTO,
  VerificationStatusDTO,
  VerificationSubmitRequest,
} from '@/types'

export function registerUser(payload: RegisterRequest) {
  return apiPost<void, RegisterRequest>('/user/register', payload)
}

export function loginUser(payload: LoginRequest) {
  return apiPost<LoginResponseDTO, LoginRequest>('/user/login', payload)
}

export function getUserProfile(userId: EntityId) {
  return apiGet<UserProfileDTO>('/user/profile', {
    params: { userId },
  })
}

export function updateUserProfile(userId: EntityId, payload: UserProfileUpdateRequest) {
  return apiPut<UserProfileDTO, UserProfileUpdateRequest>('/user/profile', payload, {
    params: { userId },
  })
}

export function getUserCredit(userId: EntityId) {
  return apiGet<CreditDTO>('/user/credit', {
    params: { userId },
  })
}

export function getPublicUser(userId: EntityId) {
  return apiGet<UserPublicDTO>(`/user/${userId}`)
}

export function getUserHome(userId: EntityId) {
  return apiGet<UserHomeDTO>(`/user/${userId}/home`)
}

export function submitVerification(payload: VerificationSubmitRequest) {
  return apiPost<VerificationStatusDTO, VerificationSubmitRequest>('/user/verification/submit', payload)
}

export function deleteAccount(userId: EntityId) {
  return apiDelete<void>('/user/account', {
    params: { userId },
  })
}
