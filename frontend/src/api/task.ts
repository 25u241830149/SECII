import { apiDelete, apiGet, apiPost, apiPut } from './request'
import type {
  EntityId,
  FavoriteTaskQuery,
  PageResponse,
  TaskDetailDTO,
  TaskFavoriteResultDTO,
  TaskListDTO,
  TaskListQuery,
  TaskMutationPayload,
  TaskCommentDTO,
  TaskStatsDTO,
} from '@/types'

export function getTasks(query: TaskListQuery = {}) {
  return apiGet<PageResponse<TaskListDTO>>('/tasks', { params: query })
}

export function getTaskComments(taskId: EntityId) {
  return apiGet<TaskCommentDTO[]>(`/tasks/${taskId}/comments`)
}

export function createTaskComment(taskId: EntityId, content: string) {
  return apiPost<TaskCommentDTO[], { content: string }>(`/tasks/${taskId}/comments`, { content })
}

export function deleteTaskComment(taskId: EntityId, commentId: EntityId) {
  return apiDelete<void>(`/tasks/${taskId}/comments/${commentId}`)
}

export function getTaskStats() {
  return apiGet<TaskStatsDTO>('/tasks/stats')
}

export function getTaskDetail(taskId: EntityId) {
  return apiGet<TaskDetailDTO>(`/tasks/${taskId}`)
}

export function createTask(payload: TaskMutationPayload) {
  return apiPost<TaskDetailDTO, TaskMutationPayload>('/tasks', payload)
}

export function updateTask(taskId: EntityId, payload: TaskMutationPayload) {
  return apiPut<TaskDetailDTO, TaskMutationPayload>(`/tasks/${taskId}`, payload)
}

export function deleteTask(taskId: EntityId) {
  return apiDelete<void>(`/tasks/${taskId}`)
}

export function getFavoriteTasks(query: FavoriteTaskQuery) {
  return apiGet<PageResponse<TaskListDTO>>('/tasks/favorites', { params: query })
}

export function favoriteTask(taskId: EntityId) {
  return apiPost<TaskFavoriteResultDTO>(`/tasks/${taskId}/favorite`)
}

export function unfavoriteTask(taskId: EntityId) {
  return apiDelete<TaskFavoriteResultDTO>(`/tasks/${taskId}/favorite`)
}

export function getPublishedTasks(query: Omit<TaskListQuery, 'publisherId'> & { userId: EntityId }) {
  return getTasks({ ...query, publisherId: query.userId })
}
