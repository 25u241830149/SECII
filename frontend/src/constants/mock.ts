import type { AdminStats, MessageItem, NoticeItem, OrderItem, PostItem, ReviewItem, TaskItem, UserHome, UserInfo } from '@/types'

export const mockCurrentUser: UserInfo = {
  userId: 10001,
  nickname: '林同学',
  avatar: 'https://dummyimage.com/96x96/f2f0ea/1f2937&text=LH',
  email: 'lin@example.com',
  phone: '13800000000',
  realName: '林海',
  department: '计算机学院',
  role: 'USER',
  creditScore: 98,
  verificationStatus: 'APPROVED',
}

export const mockUserHome: UserHome = {
  userId: 10001,
  nickname: '林同学',
  avatar: 'https://dummyimage.com/96x96/f2f0ea/1f2937&text=LH',
  department: '计算机学院',
  creditScore: 98,
  level: '校园王牌互助者',
  completedOrders: 46,
  publishedTasks: 21,
  favoriteCount: 18,
  postCount: 12,
  bio: '常驻图书馆和东区驿站，接单快、回复快。',
}

export const mockTasks: TaskItem[] = [
  {
    taskId: 1,
    title: '今晚帮忙代取东区驿站快递',
    category: 'EXPRESS',
    reward: 6,
    status: 'OPEN',
    publisherName: '苏同学',
    publisherCredit: 96,
    publishTime: '10 分钟前',
    description: '有两个小件，宿舍在 8 号楼，晚上 9 点前送达即可。',
    location: '东区驿站',
    favorite: true,
    hotScore: 91,
  },
  {
    taskId: 2,
    title: '高数期中复习求一对一讲解',
    category: 'STUDY',
    reward: 35,
    status: 'OPEN',
    publisherName: '姜同学',
    publisherCredit: 92,
    publishTime: '25 分钟前',
    description: '主要是导数和积分应用，今晚图书馆面聊 1 小时。',
    location: '图书馆二楼',
    hotScore: 87,
  },
  {
    taskId: 3,
    title: '出闲置台灯，自提优先',
    category: 'SECOND_HAND',
    reward: 48,
    status: 'OPEN',
    publisherName: '周同学',
    publisherCredit: 90,
    publishTime: '1 小时前',
    description: '九成新，可现场试灯，支持小刀。',
    location: '北苑 3 号楼',
    hotScore: 73,
  },
  {
    taskId: 4,
    title: '周末羽毛球双打缺 1 人',
    category: 'TEAM_UP',
    reward: 0,
    status: 'OPEN',
    publisherName: '何同学',
    publisherCredit: 95,
    publishTime: '2 小时前',
    description: '周六下午体育馆，水平不限，主打轻松开黑。',
    location: '体育馆',
    hotScore: 69,
  },
]

export const mockOrders: OrderItem[] = [
  {
    orderId: 6001,
    taskTitle: '今晚帮忙代取东区驿站快递',
    status: 'DELIVERING',
    role: 'helper',
    counterpartName: '苏同学',
    amount: 6,
    updatedAt: '今天 18:20',
  },
  {
    orderId: 6002,
    taskTitle: '高数期中复习求一对一讲解',
    status: 'PENDING_CONFIRM',
    role: 'poster',
    counterpartName: '陈同学',
    amount: 35,
    updatedAt: '今天 17:45',
  },
]

export const mockReviews: ReviewItem[] = [
  {
    reviewId: 8001,
    reviewerName: '苏同学',
    rating: 5,
    content: '取件速度很快，沟通也很及时。',
    createdAt: '昨天 21:14',
  },
  {
    reviewId: 8002,
    reviewerName: '陈同学',
    rating: 4,
    content: '讲解很清楚，还顺便补充了几个例题。',
    createdAt: '3 天前',
  },
]

export const mockMessages: MessageItem[] = [
  {
    messageId: 9001,
    title: '订单状态更新',
    content: '你接取的快递代拿订单已进入配送中。',
    isRead: false,
    createdAt: '5 分钟前',
  },
  {
    messageId: 9002,
    title: '评价提醒',
    content: '你有一笔已完成订单待评价。',
    isRead: true,
    createdAt: '昨天',
  },
]

export const mockNotices: NoticeItem[] = [
  {
    noticeId: 9101,
    title: '五一期间驿站营业调整',
    content: '平台建议快递类任务提前备注取件时间，避免驿站关门。',
    createdAt: '今天',
    isRead: false,
  },
  {
    noticeId: 9102,
    title: '校园互助守则更新',
    content: '新增二手交易安全提醒，请优先选择当面验货。',
    createdAt: '2 天前',
    isRead: true,
  },
]

export const mockPosts: PostItem[] = [
  {
    postId: 7001,
    title: '求推荐期末复习时间管理方法',
    authorName: '叶同学',
    content: '这学期任务堆在一起了，想看看大家怎么安排冲刺周。',
    likeCount: 42,
    favoriteCount: 15,
    commentCount: 18,
    createdAt: '30 分钟前',
    isRecommended: true,
  },
  {
    postId: 7002,
    title: '东区驿站附近二手书交换贴',
    authorName: '沈同学',
    content: '把自己不用的教材、笔记留给下一位同学，也欢迎蹲需要的书。',
    likeCount: 27,
    favoriteCount: 11,
    commentCount: 9,
    createdAt: '1 小时前',
    isPinned: true,
  },
]

export const mockAdminStats: AdminStats = {
  totalUsers: 1240,
  activeTasks: 128,
  activeOrders: 56,
  forumPosts: 342,
  pendingVerifications: 7,
}