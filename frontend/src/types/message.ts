import type { EntityId } from './common'
import type { MessageType } from './enums'

export interface NoticeDTO {
  noticeId: EntityId
  title: string
  content: string
  type: MessageType
  read: boolean
  createdAt: string
}

export interface ChatMessageDTO {
  messageId: EntityId
  conversationId: EntityId
  senderId: EntityId
  receiverId: EntityId
  content: string
  createdAt: string
  read: boolean
}
