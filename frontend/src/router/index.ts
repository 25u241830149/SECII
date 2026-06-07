import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

import AdminLayout from '@/layouts/AdminLayout.vue'
import MainLayout from '@/layouts/MainLayout.vue'
import PublicLayout from '@/layouts/PublicLayout.vue'
import { useAuthStore } from '@/stores'
import HomeView from '@/views/home/Home.vue'
import ForbiddenView from '@/views/system/ForbiddenView.vue'
import NotFoundView from '@/views/system/NotFoundView.vue'

declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    module?: string
    requiresAuth?: boolean
    requiresAdmin?: boolean
    guestOnly?: boolean
    publicTitle?: string
    publicEyebrow?: string
<<<<<<< HEAD
    publicDocumentKey?: 'help-center' | 'privacy-policy' | 'terms-of-service' | 'credit-rules'
=======
    publicDocumentKey?: 'help-center' | 'privacy-policy' | 'terms-of-service'
>>>>>>> 11effb30dd69de51337014e8d3d48cf9ddf31a5c
  }
}

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: MainLayout,
    children: [
      {
        path: '',
        name: 'home',
        component: HomeView,
        meta: { title: '首页', module: '任务与订单' },
      },
      {
        path: 'tasks',
        name: 'task-list',
        component: HomeView,
        meta: { title: '任务广场', module: '任务模块' },
      },
      {
        path: 'tasks/publish',
        name: 'task-publish',
        component: () => import('@/views/publish/PublishTask.vue'),
        meta: { title: '发布任务', module: '任务模块', requiresAuth: true },
      },
      {
        path: 'tasks/:taskId',
        name: 'task-detail',
        component: () => import('@/views/task/TaskDetail.vue'),
        meta: { title: '任务详情', module: '任务模块' },
      },
      {
        path: 'orders',
        name: 'orders',
        component: () => import('@/views/order/OrderList.vue'),
        meta: { title: '订单列表', module: '订单模块', requiresAuth: true },
      },
      {
        path: 'orders/:orderId',
        name: 'order-detail',
        component: () => import('@/views/order/OrderDetail.vue'),
        meta: { title: '订单详情', module: '订单模块', requiresAuth: true },
      },
      {
        path: 'messages',
        name: 'messages',
        component: () => import('@/views/message/MessageCenter.vue'),
        meta: { title: '消息中心', module: '消息模块', requiresAuth: true },
      },
      {
        path: 'profile',
        component: () => import('@/layouts/ProfileLayout.vue'),
        meta: { requiresAuth: true },
        children: [
          {
            path: '',
            name: 'profile',
            component: () => import('@/views/profile/ProfileHome.vue'),
            meta: { title: '个人主页', module: '用户模块', requiresAuth: true },
          },
          {
            path: 'favorites',
            name: 'profile-favorites',
            component: () => import('@/views/profile/ProfileFavorites.vue'),
            meta: { title: '我的收藏', module: '用户模块', requiresAuth: true },
          },
          {
            path: 'published',
            name: 'profile-published',
            component: () => import('@/views/profile/ProfilePublished.vue'),
            meta: { title: '我的发布', module: '用户模块', requiresAuth: true },
          },
          {
            path: 'orders',
            name: 'profile-orders',
            component: () => import('@/views/profile/ProfileOrders.vue'),
            meta: { title: '我的接单', module: '用户模块', requiresAuth: true },
          },
          {
            path: 'credit',
            name: 'profile-credit',
            component: () => import('@/views/profile/ProfileCredit.vue'),
            meta: { title: '信用分', module: '用户模块', requiresAuth: true },
          },
          {
            path: 'edit',
            name: 'profile-edit',
            component: () => import('@/views/profile/ProfileEdit.vue'),
            meta: { title: '资料编辑', module: '用户模块', requiresAuth: true },
          },
          {
            path: 'delete',
            name: 'profile-delete',
            component: () => import('@/views/profile/AccountDeletion.vue'),
            meta: { title: '账号注销', module: '用户模块', requiresAuth: true },
          },
        ],
      },
      {
        path: 'account/delete',
        name: 'account-delete',
        redirect: '/profile/delete',
      },
      {
        path: 'reports/create',
        name: 'report-create',
        component: () => import('@/views/report/ReportCreate.vue'),
        meta: { title: '提交举报', module: '举报模块', requiresAuth: true },
      },
    ],
  },
  {
    path: '/',
    component: PublicLayout,
    meta: { guestOnly: true },
    children: [
      {
        path: 'login',
        name: 'login',
        component: () => import('@/views/auth/LoginView.vue'),
        meta: {
          title: '登录',
          publicEyebrow: '欢迎回来',
          publicTitle: '校园互助，让学习更轻松，让生活更高效',
        },
      },
      {
        path: 'register',
        name: 'register',
        component: () => import('@/views/auth/RegisterView.vue'),
        meta: {
          title: '注册',
          publicEyebrow: '加入 CampusHub',
          publicTitle: '创建你的校园协作身份，开始发布任务和参与抢单',
        },
      },
    ],
  },
  {
<<<<<<< HEAD
    path: '/help/credit',
    name: 'help-credit',
    component: () => import('@/views/system/PublicDocumentView.vue'),
    meta: {
      title: '信用分规则',
      publicDocumentKey: 'credit-rules',
    },
  },
  {
=======
>>>>>>> 11effb30dd69de51337014e8d3d48cf9ddf31a5c
    path: '/help-center',
    alias: ['/help'],
    name: 'help-center',
    component: () => import('@/views/system/PublicDocumentView.vue'),
    meta: {
      title: '帮助中心',
      publicDocumentKey: 'help-center',
    },
  },
  {
    path: '/privacy',
    redirect: {
      name: 'help-center',
      query: { dialog: 'privacy-policy' },
    },
  },
  {
    path: '/privacy-policy',
    redirect: {
      name: 'help-center',
      query: { dialog: 'privacy-policy' },
    },
  },
  {
    path: '/agreement',
    redirect: {
      name: 'help-center',
      query: { dialog: 'terms-of-service' },
    },
  },
  {
    path: '/terms-of-service',
    redirect: {
      name: 'help-center',
      query: { dialog: 'terms-of-service' },
    },
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        name: 'admin-dashboard',
        component: () => import('@/views/admin/StatsDashboard.vue'),
        meta: { title: '数据看板', module: '管理后台' },
      },
      {
        path: 'users',
        name: 'admin-users',
        component: () => import('@/views/admin/UserManagement.vue'),
        meta: { title: '用户管理', module: '管理后台' },
      },
      {
        path: 'tasks',
        name: 'admin-tasks',
        component: () => import('@/views/admin/TaskModeration.vue'),
        meta: { title: '任务管理', module: '管理后台' },
      },
      {
        path: 'reports',
        name: 'admin-reports',
        component: () => import('@/views/admin/ReportCenter.vue'),
        meta: { title: '举报处理', module: '管理后台' },
      },
      {
        path: 'notices',
        name: 'admin-notices',
        component: () => import('@/views/admin/NoticeManagement.vue'),
        meta: { title: '公告管理', module: '管理后台' },
      },
    ],
  },
  {
    path: '/403',
    name: 'forbidden',
    component: ForbiddenView,
    meta: { title: '暂无访问权限' },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: NotFoundView,
    meta: { title: '页面不存在' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const authStore = useAuthStore()

  if (!authStore.initialized) {
    authStore.restoreSession()
  }

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    authStore.setRedirectPath(to.fullPath)
    return {
      name: 'login',
      query: { redirect: to.fullPath },
    }
  }

  if (to.meta.guestOnly && authStore.isAuthenticated) {
    return authStore.redirectPath || '/'
  }

  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    return { name: 'forbidden' }
  }

  return true
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} - CampusHub` : 'CampusHub'
})

export default router
