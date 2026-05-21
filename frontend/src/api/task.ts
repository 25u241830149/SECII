import { apiDelete, apiGet, apiPost } from './request'
import type { EntityId, PageQuery, PageResponse, TaskCategory, TaskDTO } from '@/types'

export interface FavoriteTaskQuery extends PageQuery {
  userId: EntityId
  category?: TaskCategory
}

export interface PublishedTaskQuery extends PageQuery {
  userId: EntityId
}

export function getFavoriteTasks(query: FavoriteTaskQuery) {
  return apiGet<PageResponse<TaskDTO>>('/tasks/favorites', {
    params: query,
  })
}

export function getPublishedTasks(query: PublishedTaskQuery) {
  return apiGet<PageResponse<TaskDTO>>('/tasks', {
    params: {
      ...query,
      publisherId: query.userId,
    },
  })
}

export function favoriteTask(taskId: EntityId) {
  return apiPost<{ taskId: EntityId; favorited: boolean }>(`/tasks/${taskId}/favorite`)
}

export function unfavoriteTask(taskId: EntityId) {
  return apiDelete<{ taskId: EntityId; favorited: boolean }>(`/tasks/${taskId}/favorite`)
}

