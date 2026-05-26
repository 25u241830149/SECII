import { apiDelete, apiGet, apiPost, apiPut } from './request'
import type {
  ChatMessageDTO,
  ChatSendRequest,
  EntityId,
  MessageDTO,
  MessageListQuery,
  NoticeCreateRequest,
  NoticeDTO,
  PageResponse,
  UnreadCountDTO,
} from '@/types'

export function getMessages(query: MessageListQuery = {}) {
  return apiGet<PageResponse<MessageDTO>>('/messages', { params: query })
}

export function getUnreadMessageCount() {
  return apiGet<UnreadCountDTO>('/messages/unread-count')
}

export function markMessageRead(messageId: EntityId) {
  return apiPut<void>(`/messages/${messageId}/read`)
}

export function markAllMessagesRead() {
  return apiPut<void>('/messages/read-all')
}

export function deleteMessage(messageId: EntityId) {
  return apiDelete<void>(`/messages/${messageId}`)
}

export function getOrderChat(orderId: EntityId) {
  return apiGet<ChatMessageDTO[]>(`/orders/${orderId}/chat`)
}

export function sendOrderChat(orderId: EntityId, payload: ChatSendRequest) {
  return apiPost<ChatMessageDTO, ChatSendRequest>(`/orders/${orderId}/chat`, payload)
}

export function getLatestNotices(limit = 5) {
  return apiGet<NoticeDTO[]>('/notices/latest', { params: { limit } })
}

export function getAdminNotices(query: { page?: number; size?: number } = {}) {
  return apiGet<PageResponse<NoticeDTO>>('/admin/notices', { params: query })
}

export function createNotice(payload: NoticeCreateRequest) {
  return apiPost<NoticeDTO, NoticeCreateRequest>('/admin/notices', payload)
}

export function updateNotice(noticeId: EntityId, payload: NoticeCreateRequest) {
  return apiPut<NoticeDTO, NoticeCreateRequest>(`/admin/notices/${noticeId}`, payload)
}

export function deleteNotice(noticeId: EntityId) {
  return apiDelete<void>(`/admin/notices/${noticeId}`)
}
