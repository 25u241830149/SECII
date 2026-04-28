import { request } from '@/api/request'
import type { AdminStats } from '@/types'

export function getDashboardStats() {
  return request.get<AdminStats>('/admin/stats/dashboard')
}