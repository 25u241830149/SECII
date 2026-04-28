import { request } from '@/api/request'
import type { ChatMessage, ChatSendPayload, MessageItem, NoticeItem, PageResponse } from '@/types'

export function getMessages(userId: number, page = 1, size = 10) {
  return request.get<PageResponse<MessageItem>>('/messages', { params: { userId, page, size } })
}

export function getUnreadCount(userId: number) {
  return request.get<number>('/messages/unread-count', { params: { userId } })
}

export function markMessageRead(messageId: number) {
  return request.put<null>(`/messages/${messageId}/read`)
}

export function deleteMessage(messageId: number) {
  return request.delete<null>(`/messages/${messageId}`)
}

export function sendChatMessage(data: ChatSendPayload) {
  return request.post<null>('/messages/chats', data)
}

export function getChatMessages(orderId: number, userId: number) {
  return request.get<ChatMessage[]>('/messages/chats', { params: { orderId, userId } })
}

export function getNotices(userId: number) {
  return request.get<NoticeItem[]>('/messages/notices', { params: { userId } })
}

export function markNoticeRead(noticeId: number) {
  return request.put<null>(`/messages/notices/${noticeId}/read`)
}