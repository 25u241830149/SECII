import { request } from '@/api/request'
import type { FeedQuery, PageResponse, TaskDetail, TaskItem, TaskPayload } from '@/types'

export function createTask(data: TaskPayload) {
  return request.post<null>('/tasks', data)
}

export function getTaskDetail(taskId: number) {
  return request.get<TaskDetail>(`/tasks/${taskId}`)
}

export function updateTask(taskId: number, data: Partial<TaskPayload>) {
  return request.put<null>(`/tasks/${taskId}`, data)
}

export function getTaskList(params: FeedQuery) {
  return request.get<PageResponse<TaskItem>>('/tasks', { params })
}

export function deleteTask(taskId: number) {
  return request.delete<null>(`/tasks/${taskId}`)
}

export function favoriteTask(taskId: number, userId: number) {
  return request.post<null>(`/tasks/${taskId}/favorite`, null, { params: { userId } })
}

export function unfavoriteTask(taskId: number, userId: number) {
  return request.delete<null>(`/tasks/${taskId}/favorite`, { params: { userId } })
}

export function getFavoriteTasks(userId: number, page = 1, size = 10) {
  return request.get<PageResponse<TaskItem>>('/tasks/favorites', { params: { userId, page, size } })
}

export function removeTaskByAdmin(taskId: number) {
  return request.delete<null>(`/admin/tasks/${taskId}`)
}