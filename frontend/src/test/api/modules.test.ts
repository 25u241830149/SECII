import { beforeEach, describe, expect, it, vi } from 'vitest'

const requestMocks = vi.hoisted(() => ({
  apiGet: vi.fn(),
  apiPost: vi.fn(),
  apiPut: vi.fn(),
  apiDelete: vi.fn(),
}))

vi.mock('@/api/request', () => requestMocks)

import * as adminApi from '@/api/admin'
import * as messageApi from '@/api/message'
import * as orderApi from '@/api/order'
import * as reportApi from '@/api/report'
import * as reviewApi from '@/api/review'
import * as taskApi from '@/api/task'
import * as uploadApi from '@/api/upload'
import * as userApi from '@/api/user'

describe('api modules', () => {
  beforeEach(() => {
    requestMocks.apiGet.mockReset()
    requestMocks.apiPost.mockReset()
    requestMocks.apiPut.mockReset()
    requestMocks.apiDelete.mockReset()
    requestMocks.apiGet.mockReturnValue('get-result')
    requestMocks.apiPost.mockReturnValue('post-result')
    requestMocks.apiPut.mockReturnValue('put-result')
    requestMocks.apiDelete.mockReturnValue('delete-result')
  })

  describe('user api', () => {
    it('maps user endpoints to request helpers', () => {
      const registerPayload = {
        studentId: '20230001',
        password: '123456',
        nickname: '昵称',
        realName: '真实姓名',
        studentCardImage: '/card.png',
      }
      const profilePayload = { nickname: '新昵称' }
      const passwordPayload = {
        currentPassword: 'old',
        newPassword: 'new123456',
        confirmPassword: 'new123456',
      }
      const verificationPayload = {
        userId: 1,
        realName: '真实姓名',
        studentId: '20230001',
        college: '软件工程',
        studentCardImage: '/card.png',
      }

      expect(userApi.registerUser(registerPayload as any)).toBe('post-result')
      expect(userApi.loginUser({ studentId: '20230001', password: '123456' })).toBe('post-result')
      expect(userApi.getUserProfile(1)).toBe('get-result')
      expect(userApi.updateUserProfile(1, profilePayload as any)).toBe('put-result')
      expect(userApi.changePassword(passwordPayload as any)).toBe('put-result')
      expect(userApi.getUserCredit(1)).toBe('get-result')
      expect(userApi.getPublicUser(1)).toBe('get-result')
      expect(userApi.getUserHome(1)).toBe('get-result')
      expect(userApi.submitVerification(verificationPayload as any)).toBe('post-result')
      expect(userApi.deleteAccount()).toBe('delete-result')

      expect(requestMocks.apiPost).toHaveBeenNthCalledWith(1, '/user/register', registerPayload)
      expect(requestMocks.apiPost).toHaveBeenNthCalledWith(2, '/user/login', {
        studentId: '20230001',
        password: '123456',
      })
      expect(requestMocks.apiGet).toHaveBeenNthCalledWith(1, '/user/profile', {
        params: { userId: 1 },
      })
      expect(requestMocks.apiPut).toHaveBeenNthCalledWith(1, '/user/profile', profilePayload, {
        params: { userId: 1 },
      })
      expect(requestMocks.apiPut).toHaveBeenNthCalledWith(2, '/user/password', passwordPayload)
      expect(requestMocks.apiGet).toHaveBeenNthCalledWith(2, '/user/credit', {
        params: { userId: 1 },
      })
      expect(requestMocks.apiGet).toHaveBeenNthCalledWith(3, '/user/1')
      expect(requestMocks.apiGet).toHaveBeenNthCalledWith(4, '/user/1/home')
      expect(requestMocks.apiPost).toHaveBeenNthCalledWith(
        3,
        '/user/verification/submit',
        verificationPayload,
      )
      expect(requestMocks.apiDelete).toHaveBeenNthCalledWith(1, '/user/account')
    })
  })

  describe('task api', () => {
    it('maps task endpoints to request helpers', () => {
      const query = { page: 2, size: 10, category: 'EXPRESS' }
      const mutationPayload = {
        title: '代取快递',
        description: '今晚帮忙取一下',
        category: 'EXPRESS',
        reward: 10,
      }

      expect(taskApi.getTasks(query as any)).toBe('get-result')
      expect(taskApi.getTaskComments(1)).toBe('get-result')
      expect(taskApi.createTaskComment(1, '新的评论')).toBe('post-result')
      expect(taskApi.deleteTaskComment(1, 2)).toBe('delete-result')
      expect(taskApi.getTaskStats()).toBe('get-result')
      expect(taskApi.getTaskDetail(1)).toBe('get-result')
      expect(taskApi.createTask(mutationPayload as any)).toBe('post-result')
      expect(taskApi.updateTask(1, mutationPayload as any)).toBe('put-result')
      expect(taskApi.deleteTask(1)).toBe('delete-result')
      expect(taskApi.getFavoriteTasks({ userId: 1, page: 1 } as any)).toBe('get-result')
      expect(taskApi.favoriteTask(1)).toBe('post-result')
      expect(taskApi.unfavoriteTask(1)).toBe('delete-result')
      expect(taskApi.getPublishedTasks({ userId: 1, page: 1, size: 5 } as any)).toBe('get-result')

      expect(requestMocks.apiGet).toHaveBeenNthCalledWith(1, '/tasks', { params: query })
      expect(requestMocks.apiGet).toHaveBeenNthCalledWith(2, '/tasks/1/comments', {
        params: { sort: 'time' },
      })
      expect(requestMocks.apiPost).toHaveBeenNthCalledWith(1, '/tasks/1/comments', {
        content: '新的评论',
      })
      expect(requestMocks.apiDelete).toHaveBeenNthCalledWith(1, '/tasks/1/comments/2')
      expect(requestMocks.apiGet).toHaveBeenNthCalledWith(3, '/tasks/stats')
      expect(requestMocks.apiGet).toHaveBeenNthCalledWith(4, '/tasks/1')
      expect(requestMocks.apiPost).toHaveBeenNthCalledWith(2, '/tasks', mutationPayload)
      expect(requestMocks.apiPut).toHaveBeenNthCalledWith(1, '/tasks/1', mutationPayload)
      expect(requestMocks.apiDelete).toHaveBeenNthCalledWith(2, '/tasks/1')
      expect(requestMocks.apiGet).toHaveBeenNthCalledWith(5, '/tasks/favorites', {
        params: { userId: 1, page: 1 },
      })
      expect(requestMocks.apiPost).toHaveBeenNthCalledWith(3, '/tasks/1/favorite')
      expect(requestMocks.apiDelete).toHaveBeenNthCalledWith(3, '/tasks/1/favorite')
      expect(requestMocks.apiGet).toHaveBeenNthCalledWith(6, '/tasks', {
        params: { userId: 1, page: 1, size: 5, publisherId: 1 },
      })
    })
  })

  describe('order api', () => {
    it('maps order endpoints to request helpers', () => {
      const grabPayload = { taskId: 1 }
      const query = { userId: 1, role: 'helper', page: 1, size: 10 }

      expect(orderApi.grabOrder(grabPayload)).toBe('post-result')
      expect(orderApi.confirmOrder(1)).toBe('post-result')
      expect(orderApi.rejectOrder(1)).toBe('post-result')
      expect(orderApi.abandonOrder(1)).toBe('post-result')
      expect(orderApi.completeOrder(1)).toBe('post-result')
      expect(orderApi.cancelOrder(1)).toBe('post-result')
      expect(orderApi.getOrders(query as any)).toBe('get-result')
      expect(orderApi.getOrderStats(1)).toBe('get-result')
      expect(orderApi.getOrderDetail(1)).toBe('get-result')

      expect(requestMocks.apiPost).toHaveBeenNthCalledWith(1, '/orders/grab', grabPayload)
      expect(requestMocks.apiPost).toHaveBeenNthCalledWith(2, '/orders/1/confirm')
      expect(requestMocks.apiPost).toHaveBeenNthCalledWith(3, '/orders/1/reject')
      expect(requestMocks.apiPost).toHaveBeenNthCalledWith(4, '/orders/1/abandon')
      expect(requestMocks.apiPost).toHaveBeenNthCalledWith(5, '/orders/1/complete')
      expect(requestMocks.apiPost).toHaveBeenNthCalledWith(6, '/orders/1/cancel')
      expect(requestMocks.apiGet).toHaveBeenNthCalledWith(1, '/orders', {
        params: query,
      })
      expect(requestMocks.apiGet).toHaveBeenNthCalledWith(2, '/orders/summary/stats', {
        params: { userId: 1 },
      })
      expect(requestMocks.apiGet).toHaveBeenNthCalledWith(3, '/orders/1')
    })
  })

  describe('report api', () => {
    it('maps report endpoints to request helpers', () => {
      const createPayload = {
        targetType: 'USER',
        targetId: 2,
        targetUserId: 2,
        reason: '违规',
      }
      const listQuery = { status: 'PENDING', page: 1 }
      const handlePayload = { status: 'HANDLED', result: '已处理' }

      expect(reportApi.createReport(createPayload as any)).toBe('post-result')
      expect(reportApi.getAdminReports(listQuery as any)).toBe('get-result')
      expect(reportApi.handleReport(1, handlePayload as any)).toBe('post-result')
      expect(reportApi.getDashboardStats()).toBe('get-result')

      expect(requestMocks.apiPost).toHaveBeenNthCalledWith(1, '/reports', createPayload)
      expect(requestMocks.apiGet).toHaveBeenNthCalledWith(1, '/admin/reports', {
        params: listQuery,
      })
      expect(requestMocks.apiPost).toHaveBeenNthCalledWith(
        2,
        '/admin/reports/1/handle',
        handlePayload,
      )
      expect(requestMocks.apiGet).toHaveBeenNthCalledWith(2, '/admin/stats/dashboard')
    })
  })

  describe('review api', () => {
    it('maps review endpoints to request helpers', () => {
      const payload = {
        orderId: 1,
        targetUserId: 2,
        rating: 5,
        content: '很好',
      }

      expect(reviewApi.createReview(payload as any)).toBe('post-result')
      expect(reviewApi.getReview(1)).toBe('get-result')
      expect(reviewApi.getUserReviews(2, { page: 1, size: 10 })).toBe('get-result')
      expect(reviewApi.getOrderReviews(1)).toBe('get-result')

      expect(requestMocks.apiPost).toHaveBeenNthCalledWith(1, '/reviews', payload)
      expect(requestMocks.apiGet).toHaveBeenNthCalledWith(1, '/reviews/1')
      expect(requestMocks.apiGet).toHaveBeenNthCalledWith(2, '/reviews/user/2', {
        params: { page: 1, size: 10 },
      })
      expect(requestMocks.apiGet).toHaveBeenNthCalledWith(3, '/reviews/order/1')
    })
  })

  describe('message api', () => {
    it('maps message and notice endpoints to request helpers', () => {
      const query = { page: 1, size: 10, unread: true }
      const noticePayload = { title: '公告', content: '内容' }
      const chatPayload = { content: '你好' }

      expect(messageApi.getMessages(query as any)).toBe('get-result')
      expect(messageApi.getUnreadMessageCount()).toBe('get-result')
      expect(messageApi.markMessageRead(1)).toBe('put-result')
      expect(messageApi.markAllMessagesRead()).toBe('put-result')
      expect(messageApi.deleteMessage(1)).toBe('delete-result')
      expect(messageApi.getOrderChat(2)).toBe('get-result')
      expect(messageApi.sendOrderChat(2, chatPayload)).toBe('post-result')
      expect(messageApi.getLatestNotices()).toBe('get-result')
      expect(messageApi.getAdminNotices({ page: 2, size: 5 })).toBe('get-result')
      expect(messageApi.createNotice(noticePayload as any)).toBe('post-result')
      expect(messageApi.updateNotice(3, noticePayload as any)).toBe('put-result')
      expect(messageApi.deleteNotice(3)).toBe('delete-result')

      expect(requestMocks.apiGet).toHaveBeenNthCalledWith(1, '/messages', { params: query })
      expect(requestMocks.apiGet).toHaveBeenNthCalledWith(2, '/messages/unread-count')
      expect(requestMocks.apiPut).toHaveBeenNthCalledWith(1, '/messages/1/read')
      expect(requestMocks.apiPut).toHaveBeenNthCalledWith(2, '/messages/read-all')
      expect(requestMocks.apiDelete).toHaveBeenNthCalledWith(1, '/messages/1')
      expect(requestMocks.apiGet).toHaveBeenNthCalledWith(3, '/orders/2/chat')
      expect(requestMocks.apiPost).toHaveBeenNthCalledWith(1, '/orders/2/chat', chatPayload)
      expect(requestMocks.apiGet).toHaveBeenNthCalledWith(4, '/notices/latest', {
        params: { limit: 5 },
      })
      expect(requestMocks.apiGet).toHaveBeenNthCalledWith(5, '/admin/notices', {
        params: { page: 2, size: 5 },
      })
      expect(requestMocks.apiPost).toHaveBeenNthCalledWith(2, '/admin/notices', noticePayload)
      expect(requestMocks.apiPut).toHaveBeenNthCalledWith(3, '/admin/notices/3', noticePayload)
      expect(requestMocks.apiDelete).toHaveBeenNthCalledWith(2, '/admin/notices/3')
    })
  })

  describe('upload api', () => {
    it('builds multipart form data for upload endpoints', () => {
      const avatar = new File(['avatar'], 'avatar.png', { type: 'image/png' })
      const taskImage = new File(['task'], 'task.png', { type: 'image/png' })
      const card = new File(['card'], 'card.png', { type: 'image/png' })

      expect(uploadApi.uploadAvatar(avatar)).toBe('post-result')
      expect(uploadApi.uploadStudentCard(card, '20230001')).toBe('post-result')
      expect(uploadApi.uploadTaskImage(taskImage)).toBe('post-result')

      const [avatarUrl, avatarData, avatarConfig] = requestMocks.apiPost.mock.calls[0]
      const [cardUrl, cardData, cardConfig] = requestMocks.apiPost.mock.calls[1]
      const [taskUrl, taskData, taskConfig] = requestMocks.apiPost.mock.calls[2]

      expect(avatarUrl).toBe('/upload/avatar')
      expect(cardUrl).toBe('/upload/student-card')
      expect(taskUrl).toBe('/upload/task-image')
      expect(avatarConfig).toEqual({ headers: { 'Content-Type': 'multipart/form-data' } })
      expect(cardConfig).toEqual({ headers: { 'Content-Type': 'multipart/form-data' } })
      expect(taskConfig).toEqual({ headers: { 'Content-Type': 'multipart/form-data' } })
      expect(avatarData).toBeInstanceOf(FormData)
      expect(cardData).toBeInstanceOf(FormData)
      expect(taskData).toBeInstanceOf(FormData)
      expect(avatarData.get('file')).toBe(avatar)
      expect(cardData.get('file')).toBe(card)
      expect(cardData.get('studentId')).toBe('20230001')
      expect(taskData.get('file')).toBe(taskImage)
    })
  })

  describe('admin api', () => {
    it('maps admin-specific endpoints and preserves re-exports', () => {
      expect(adminApi.banUser(2, { reason: '违规', days: 7 })).toBe('post-result')
      expect(adminApi.verifyUser(2, { approved: true, remark: '通过' })).toBe('post-result')
      expect(adminApi.offlineTask(9)).toBe('delete-result')

      expect(requestMocks.apiPost).toHaveBeenNthCalledWith(1, '/admin/users/2/ban', {
        reason: '违规',
        days: 7,
      })
      expect(requestMocks.apiPost).toHaveBeenNthCalledWith(2, '/admin/users/2/verify', {
        approved: true,
        remark: '通过',
      })
      expect(requestMocks.apiDelete).toHaveBeenNthCalledWith(1, '/admin/tasks/9')

      expect(adminApi.getAdminReports).toBe(reportApi.getAdminReports)
      expect(adminApi.handleReport).toBe(reportApi.handleReport)
      expect(adminApi.getDashboardStats).toBe(reportApi.getDashboardStats)
      expect(adminApi.getAdminNotices).toBe(messageApi.getAdminNotices)
      expect(adminApi.createNotice).toBe(messageApi.createNotice)
      expect(adminApi.updateNotice).toBe(messageApi.updateNotice)
      expect(adminApi.deleteNotice).toBe(messageApi.deleteNotice)
    })
  })
})
