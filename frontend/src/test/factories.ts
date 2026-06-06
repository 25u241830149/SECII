import type {
  ChatMessageDTO,
  LoginResponseDTO,
  MessageDTO,
  NoticeDTO,
  OrderListDTO,
  PageResponse,
  ReportDTO,
  ReviewDTO,
  TaskListDTO,
  TaskStatsDTO,
  UserInfoDTO,
} from '@/types'

export function createUserInfo(overrides: Partial<UserInfoDTO> = {}): UserInfoDTO {
  return {
    userId: 1,
    studentId: '20230001',
    nickname: '测试用户',
    realName: '测试用户',
    department: '软件工程',
    avatarUrl: '/avatar.png',
    role: 'USER',
    creditScore: 98,
    verificationStatus: 'APPROVED',
    ...overrides,
  }
}

export function createLoginResponse(overrides: Partial<LoginResponseDTO> = {}): LoginResponseDTO {
  return {
    token: 'token-123',
    user: createUserInfo(),
    ...overrides,
  }
}

export function createTaskListItem(overrides: Partial<TaskListDTO> = {}): TaskListDTO {
  return {
    taskId: 1,
    title: '代取快递',
    description: '今晚帮忙取一下快递',
    category: 'EXPRESS',
    status: 'OPEN',
    reward: 10,
    location: '一食堂门口',
    longitude: 120.1,
    latitude: 30.2,
    publisherId: 2,
    publisherName: '发布者',
    publisherAvatarUrl: '/publisher.png',
    publisherCreditScore: 95,
    favoriteCount: 3,
    favorited: false,
    createdAt: '2026-06-06T10:00:00',
    updatedAt: '2026-06-06T10:10:00',
    ...overrides,
  }
}

export function createTaskPage(
  records: TaskListDTO[],
  overrides: Partial<PageResponse<TaskListDTO>> = {},
): PageResponse<TaskListDTO> {
  return {
    records,
    total: records.length,
    page: 1,
    size: 10,
    ...overrides,
  }
}

export function createPage<T>(
  records: T[],
  overrides: Partial<PageResponse<T>> = {},
): PageResponse<T> {
  return {
    records,
    total: records.length,
    page: 1,
    size: 10,
    ...overrides,
  }
}

export function createTaskStats(overrides: Partial<TaskStatsDTO> = {}): TaskStatsDTO {
  return {
    todayCreated: 3,
    inProgress: 4,
    completed: 5,
    ...overrides,
  }
}

export function createOrderListItem(overrides: Partial<OrderListDTO> = {}): OrderListDTO {
  return {
    orderId: 1,
    taskId: 1,
    taskTitle: '代取快递',
    taskCategory: 'EXPRESS',
    taskLocation: '一食堂门口',
    reward: 10,
    status: 'PENDING',
    currentUserReviewed: false,
    teamTotalMembers: null,
    teamCurrentMembers: null,
    posterId: 2,
    posterName: '发布者',
    helperId: 3,
    helperName: '接单者',
    createdAt: '2026-06-06T10:00:00',
    updatedAt: '2026-06-06T10:10:00',
    ...overrides,
  }
}

export function createMessage(overrides: Partial<MessageDTO> = {}): MessageDTO {
  return {
    messageId: 1,
    receiverId: 1,
    type: 'ORDER',
    title: '订单状态更新',
    content: '你的订单已确认',
    read: false,
    createdAt: '2026-06-06T10:00:00',
    ...overrides,
  }
}

export function createNotice(overrides: Partial<NoticeDTO> = {}): NoticeDTO {
  return {
    noticeId: 1,
    publisherId: 1,
    publisherName: '管理员',
    title: '系统公告',
    content: '请关注最新通知',
    createdAt: '2026-06-06T10:00:00',
    updatedAt: '2026-06-06T10:00:00',
    ...overrides,
  }
}

export function createChatMessage(overrides: Partial<ChatMessageDTO> = {}): ChatMessageDTO {
  return {
    messageId: 1,
    orderId: 1,
    senderId: 1,
    senderName: '发送者',
    receiverId: 2,
    receiverName: '接收者',
    content: '你好',
    read: false,
    createdAt: '2026-06-06T10:00:00',
    ...overrides,
  }
}

export function createReport(overrides: Partial<ReportDTO> = {}): ReportDTO {
  return {
    reportId: 1,
    reporterId: 1,
    reporterName: '举报人',
    targetUserId: 2,
    targetUserName: '被举报人',
    handlerId: null,
    handlerName: null,
    taskId: null,
    orderId: null,
    postId: null,
    commentId: null,
    targetType: 'USER',
    targetId: 2,
    reason: '违规内容',
    status: 'PENDING',
    result: null,
    createdAt: '2026-06-06T10:00:00',
    updatedAt: '2026-06-06T10:00:00',
    ...overrides,
  }
}

export function createReview(overrides: Partial<ReviewDTO> = {}): ReviewDTO {
  return {
    reviewId: 1,
    orderId: 1,
    reviewerId: 1,
    reviewerName: '评价人',
    reviewerAvatarUrl: '/reviewer.png',
    targetUserId: 2,
    targetUserName: '被评价人',
    targetUserAvatarUrl: '/target.png',
    rating: 5,
    content: '很好',
    createdAt: '2026-06-06T10:00:00',
    ...overrides,
  }
}
