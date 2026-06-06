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
  TaskCommentCreateRequest,
  TaskStatsDTO,
} from '@/types'

export function getTasks(query: TaskListQuery = {}) {
  return apiGet<PageResponse<TaskListDTO>>('/tasks', { params: query })
}

export type TaskCommentSort = 'time' | 'likes'

export function getTaskComments(taskId: EntityId, sort: TaskCommentSort = 'time') {
  return apiGet<TaskCommentDTO[]>(`/tasks/${taskId}/comments`, { params: { sort } })
}

export function createTaskComment(taskId: EntityId, payload: string | TaskCommentCreateRequest) {
  const body = typeof payload === 'string' ? { content: payload } : payload
  return apiPost<TaskCommentDTO[], TaskCommentCreateRequest>(`/tasks/${taskId}/comments`, body)
}

export function deleteTaskComment(taskId: EntityId, commentId: EntityId) {
  return apiDelete<void>(`/tasks/${taskId}/comments/${commentId}`)
}

export function likeTaskComment(taskId: EntityId, commentId: EntityId) {
  return apiPost<void>(`/tasks/${taskId}/comments/${commentId}/like`)
}

export function unlikeTaskComment(taskId: EntityId, commentId: EntityId) {
  return apiDelete<void>(`/tasks/${taskId}/comments/${commentId}/like`)
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
