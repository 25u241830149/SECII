import type { EntityId, PageQuery } from './common'
import type { MessageType } from './enums'

export interface MessageDTO {
  messageId: EntityId
  receiverId: EntityId
  type: MessageType
  title: string
  content?: string | null
  read: boolean
  createdAt: string
}

export interface MessageListQuery extends PageQuery {
  type?: MessageType
  unread?: boolean
}

export interface UnreadCountDTO {
  count: number
}

export interface ChatMessageDTO {
  messageId: EntityId
  orderId: EntityId
  senderId: EntityId
  senderName: string
  senderAvatarUrl?: string | null
  receiverId: EntityId
  receiverName: string
  receiverAvatarUrl?: string | null
  content: string
  read: boolean
  createdAt: string
}

export interface ChatSendRequest {
  content: string
}

export interface NoticeDTO {
  noticeId: EntityId
  publisherId: EntityId
  publisherName: string
  title: string
  content: string
  createdAt: string
  updatedAt: string
}

export interface NoticeCreateRequest {
  title: string
  content: string
}
