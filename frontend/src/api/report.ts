import { apiGet, apiPost } from './request'
import type {
  AdminDashboardStatsDTO,
  EntityId,
  PageResponse,
  ReportCreateRequest,
  ReportDTO,
  ReportHandleRequest,
  ReportListQuery,
} from '@/types'

export function createReport(payload: ReportCreateRequest) {
  return apiPost<ReportDTO, ReportCreateRequest>('/reports', payload)
}

export function getAdminReports(query: ReportListQuery = {}) {
  return apiGet<PageResponse<ReportDTO>>('/admin/reports', { params: query })
}

export function handleReport(reportId: EntityId, payload: ReportHandleRequest) {
  return apiPost<ReportDTO, ReportHandleRequest>(`/admin/reports/${reportId}/handle`, payload)
}

export function getDashboardStats() {
  return apiGet<AdminDashboardStatsDTO>('/admin/stats/dashboard')
}
