import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

import AdminLayout from '@/layouts/AdminLayout.vue'
import MainLayout from '@/layouts/MainLayout.vue'
import PublicLayout from '@/layouts/PublicLayout.vue'
import { useAuthStore } from '@/stores'
import HomeView from '@/views/HomeView.vue'
import ForbiddenView from '@/views/system/ForbiddenView.vue'
import NotFoundView from '@/views/system/NotFoundView.vue'
import PlaceholderView from '@/views/system/PlaceholderView.vue'

declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    module?: string
    requiresAuth?: boolean
    requiresAdmin?: boolean
    guestOnly?: boolean
    publicTitle?: string
    publicEyebrow?: string
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
        component: PlaceholderView,
        meta: { title: '需求广场', module: '任务模块', requiresAuth: true },
      },
      {
        path: 'tasks/publish',
        name: 'task-publish',
        component: PlaceholderView,
        meta: { title: '发布需求', module: '任务模块', requiresAuth: true },
      },
      {
        path: 'tasks/:taskId',
        name: 'task-detail',
        component: PlaceholderView,
        meta: { title: '需求详情', module: '任务模块', requiresAuth: true },
      },
      {
        path: 'orders',
        name: 'orders',
        redirect: '/profile/orders',
      },
      {
        path: 'messages',
        name: 'messages',
        component: PlaceholderView,
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
            meta: { title: '我的发单', module: '用户模块', requiresAuth: true },
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
            meta: { title: '信用分与等级', module: '用户模块', requiresAuth: true },
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
        component: PlaceholderView,
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
          publicTitle: '校园互助，让学习更轻松，让生活更美好',
        },
      },
      {
        path: 'register',
        name: 'register',
        component: () => import('@/views/auth/RegisterView.vue'),
        meta: {
          title: '注册',
          publicEyebrow: '加入 CampusHub',
          publicTitle: '开启你的校园互助之旅，让学习更轻松，让生活更美好',
        },
      },
    ],
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        name: 'admin-dashboard',
        component: PlaceholderView,
        meta: { title: '数据看板', module: '管理后台' },
      },
      {
        path: 'users',
        name: 'admin-users',
        component: PlaceholderView,
        meta: { title: '用户管理', module: '管理后台' },
      },
      {
        path: 'tasks',
        name: 'admin-tasks',
        component: PlaceholderView,
        meta: { title: '任务管理', module: '管理后台' },
      },
      {
        path: 'reports',
        name: 'admin-reports',
        component: PlaceholderView,
        meta: { title: '举报处理', module: '管理后台' },
      },
      {
        path: 'notices',
        name: 'admin-notices',
        component: PlaceholderView,
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
  const title = to.meta.title ? `${to.meta.title} - CampusHub` : 'CampusHub'
  document.title = title
})

export default router
