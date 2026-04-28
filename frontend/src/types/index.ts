export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface PageResponse<T> {
  records: T[]
  total: number
  page: number
  size: number
}

export type UserRole = 'USER' | 'ADMIN'
export type PublishCategory = 'EXPRESS' | 'STUDY' | 'SECOND_HAND' | 'TEAM_UP' | 'OTHER'
export type FeedSort = 'time' | 'hot'
export type OrderRole = 'poster' | 'helper'

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  confirmPassword: string
  nickname: string
  email: string
}

export interface LoginResponse {
  token: string
  userId: number
  role: UserRole
}

export interface VerificationRequest {
  userId: number
  realName: string
  studentId: string
  cardImages: string[]
}

export interface UserInfo {
  userId: number
  nickname: string
  avatar: string
  email: string
  phone: string
  realName?: string
  department?: string
  role: UserRole
  creditScore: number
  verificationStatus: string
}

export interface UserHome {
  userId: number
  nickname: string
  avatar: string
  department: string
  creditScore: number
  level: string
  completedOrders: number
  publishedTasks: number
  favoriteCount: number
  postCount: number
  bio?: string
}

export interface FeedQuery {
  category?: PublishCategory
  keyword?: string
  sort?: FeedSort
  page?: number
  size?: number
}

export interface UserMenuState {
  loggedIn: boolean
  displayName: string
  avatar: string
}

export interface TaskItem {
  taskId: number
  title: string
  category: PublishCategory
  reward: number
  status: string
  publisherName: string
  publisherCredit: number
  publishTime: string
  description: string
  location: string
  favorite?: boolean
  hotScore?: number
}

export interface TaskDetail extends TaskItem {
  content: string
  tags: string[]
}

export interface TaskPayload {
  title: string
  category: PublishCategory
  description: string
  reward: number
  location: string
  publisherId: number
}

export interface OrderItem {
  orderId: number
  taskTitle: string
  status: string
  role: OrderRole
  counterpartName: string
  amount: number
  updatedAt: string
}

export interface OrderDetail extends OrderItem {
  orderNo: string
  chatEnabled: boolean
  pickupCode?: string
  taskDescription: string
}

export interface GrabOrderPayload {
  taskId: number
  userId: number
}

export interface ReviewItem {
  reviewId: number
  reviewerName: string
  rating: number
  content: string
  createdAt: string
}

export interface ReviewPayload {
  orderId: number
  targetUserId: number
  rating: number
  content: string
}

export interface MessageItem {
  messageId: number
  title: string
  content: string
  isRead: boolean
  createdAt: string
}

export interface ChatMessage {
  messageId: number
  senderName: string
  content: string
  sentAt: string
  self: boolean
}

export interface ChatSendPayload {
  orderId: number
  senderId: number
  receiverId: number
  content: string
}

export interface NoticeItem {
  noticeId: number
  title: string
  content: string
  createdAt: string
  isRead: boolean
}

export interface PostItem {
  postId: number
  title: string
  authorName: string
  content: string
  likeCount: number
  favoriteCount: number
  commentCount: number
  createdAt: string
  isRecommended?: boolean
  isPinned?: boolean
}

export interface PostDetail extends PostItem {
  mediaUrls: string[]
}

export interface PostPayload {
  title: string
  content: string
  mediaUrls?: string[]
}

export interface CommentPayload {
  userId: number
  content: string
}

export interface AdminStats {
  totalUsers: number
  activeTasks: number
  activeOrders: number
  forumPosts: number
  pendingVerifications: number
}