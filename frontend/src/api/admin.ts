import { apiDelete, apiGet, apiPost } from './request'
import { getDashboardStats, getAdminReports, handleReport } from './report'
import {
  createNotice,
  deleteNotice,
  getAdminNotices,
  updateNotice,
} from './message'
import type { EntityId } from '@/types'

export { createNotice, deleteNotice, getAdminNotices, getAdminReports, getDashboardStats, handleReport, updateNotice }

export interface AdminBanRequest {
  reason: string
  days?: number
}

export interface AdminBanResultDTO {
  userId: EntityId
  status: string
  reason: string
}

export interface AdminVerifyRequest {
  approved: boolean
  remark?: string
}

export interface AdminVerifyResultDTO {
  userId: EntityId
  verificationStatus: string
  remark?: string
}

export type AdminUserRole = 'ALL' | 'USER' | 'ADMIN'

export interface AdminUserOptionDTO {
  userId: EntityId
  studentId: string
  nickname: string
  role: 'USER' | 'ADMIN'
  status: 'NORMAL' | 'PENDING_VERIFICATION' | 'BANNED'
  creditScore: number
}

export function searchAdminUsers(params: { keyword?: string; role?: AdminUserRole; size?: number } = {}) {
  return apiGet<AdminUserOptionDTO[]>('/admin/users', { params })
}

export function banUser(userId: EntityId, payload: AdminBanRequest) {
  return apiPost<AdminBanResultDTO, AdminBanRequest>(`/admin/users/${userId}/ban`, payload)
}

export function verifyUser(userId: EntityId, payload: AdminVerifyRequest) {
  return apiPost<AdminVerifyResultDTO, AdminVerifyRequest>(`/admin/users/${userId}/verify`, payload)
}

export function offlineTask(taskId: EntityId) {
  return apiDelete<void>(`/admin/tasks/${taskId}`)
}
