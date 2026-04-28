import { request } from '@/api/request'
import type { LoginRequest, LoginResponse, RegisterRequest, UserHome, UserInfo, VerificationRequest } from '@/types'

export function register(data: RegisterRequest) {
  return request.post<null>('/user/register', data)
}

export function login(data: LoginRequest) {
  return request.post<LoginResponse>('/user/login', data)
}

export function getUserProfile(userId: number) {
  return request.get<UserInfo>('/user/profile', { params: { userId } })
}

export function updateUserProfile(userId: number, data: Partial<UserInfo>) {
  return request.put<null>('/user/profile', data, { params: { userId } })
}

export function getUserCredit(userId: number) {
  return request.get<number>('/user/credit', { params: { userId } })
}

export function getPublicUserInfo(userId: number) {
  return request.get<UserInfo>(`/user/${userId}`)
}

export function getUserHome(userId: number) {
  return request.get<UserHome>(`/user/${userId}/home`)
}

export function submitVerification(data: VerificationRequest) {
  return request.post<null>('/user/verification/submit', data)
}

export function cancelAccount(userId: number) {
  return request.delete<null>('/user/account', { params: { userId } })
}

export function banUser(userId: number) {
  return request.post<null>(`/admin/users/${userId}/ban`)
}

export function reviewVerification(userId: number, approved: boolean) {
  return request.post<null>(`/admin/users/${userId}/verify`, null, { params: { approved } })
}