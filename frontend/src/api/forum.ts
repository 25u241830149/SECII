import { request } from '@/api/request'
import type { CommentPayload, PageResponse, PostDetail, PostItem, PostPayload } from '@/types'

export function createPost(data: PostPayload) {
  return request.post<null>('/posts', data)
}

export function getPostDetail(postId: number) {
  return request.get<PostDetail>(`/posts/${postId}`)
}

export function getPostList(page = 1, size = 10) {
  return request.get<PageResponse<PostItem>>('/posts', { params: { page, size } })
}

export function updatePost(postId: number, data: PostPayload) {
  return request.put<null>(`/posts/${postId}`, data)
}

export function deletePost(postId: number) {
  return request.delete<null>(`/posts/${postId}`)
}

export function addComment(postId: number, data: CommentPayload) {
  return request.post<null>(`/posts/${postId}/comments`, data)
}

export function likePost(postId: number, userId: number) {
  return request.post<null>(`/posts/${postId}/like`, null, { params: { userId } })
}

export function favoritePost(postId: number, userId: number) {
  return request.post<null>(`/posts/${postId}/favorite`, null, { params: { userId } })
}

export function pinPost(postId: number) {
  return request.post<null>(`/admin/posts/${postId}/pin`)
}

export function recommendPost(postId: number) {
  return request.post<null>(`/admin/posts/${postId}/recommend`)
}