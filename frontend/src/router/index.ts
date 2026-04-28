import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const PublicLayout = () => import('@/layouts/PublicLayout.vue')
const MainLayout = () => import('@/layouts/MainLayout.vue')
const AdminLayout = () => import('@/layouts/AdminLayout.vue')
const LoginView = () => import('@/views/auth/LoginView.vue')
const RegisterView = () => import('@/views/auth/RegisterView.vue')
const HomeView = () => import('@/views/home/HomeView.vue')
const CategoryView = () => import('@/views/home/CategoryView.vue')
const PublishView = () => import('@/views/publish/PublishView.vue')
const TaskDetailView = () => import('@/views/task/TaskDetailView.vue')
const OrderCenterView = () => import('@/views/order/OrderCenterView.vue')
const MessageCenterView = () => import('@/views/message/MessageCenterView.vue')
const ForumView = () => import('@/views/forum/ForumView.vue')
const MeLayout = () => import('@/views/profile/MeLayout.vue')
const ProfileView = () => import('@/views/profile/ProfileView.vue')
const TracksView = () => import('@/views/profile/TracksView.vue')
const FavoritesView = () => import('@/views/profile/FavoritesView.vue')
const MyTasksView = () => import('@/views/profile/MyTasksView.vue')
const MyOrdersView = () => import('@/views/profile/MyOrdersView.vue')
const CreditView = () => import('@/views/profile/CreditView.vue')
const AccountView = () => import('@/views/profile/AccountView.vue')
const ForbiddenView = () => import('@/views/system/ForbiddenView.vue')
const NotFoundView = () => import('@/views/system/NotFoundView.vue')
const AdminStatsView = () => import('@/views/admin/AdminStatsView.vue')
const AdminUsersView = () => import('@/views/admin/AdminUsersView.vue')
const AdminTasksView = () => import('@/views/admin/AdminTasksView.vue')
const AdminPostsView = () => import('@/views/admin/AdminPostsView.vue')

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    component: PublicLayout,
    children: [
      {
        path: '',
        name: 'login',
        component: LoginView,
        meta: { guestOnly: true, title: '登录' },
      },
    ],
  },
  {
    path: '/register',
    component: PublicLayout,
    children: [
      {
        path: '',
        name: 'register',
        component: RegisterView,
        meta: { guestOnly: true, title: '注册' },
      },
    ],
  },
  {
    path: '/',
    component: MainLayout,
    children: [
      {
        path: '',
        name: 'home',
        component: HomeView,
        meta: { title: '首页' },
      },
      {
        path: 'category/:category',
        name: 'category',
        component: CategoryView,
        meta: { title: '分类信息流' },
      },
      {
        path: 'publish/:category',
        name: 'publish',
        component: PublishView,
        meta: { requiresAuth: true, title: '发布任务' },
      },
      {
        path: 'tasks/:id',
        name: 'task-detail',
        component: TaskDetailView,
        meta: { title: '任务详情' },
      },
      {
        path: 'orders',
        name: 'orders',
        component: OrderCenterView,
        meta: { requiresAuth: true, title: '订单中心' },
      },
      {
        path: 'messages',
        name: 'messages',
        component: MessageCenterView,
        meta: { requiresAuth: true, title: '消息中心' },
      },
      {
        path: 'forum',
        name: 'forum',
        component: ForumView,
        meta: { title: '论坛中心' },
      },
      {
        path: 'me',
        component: MeLayout,
        meta: { requiresAuth: true, title: '个人中心' },
        children: [
          {
            path: '',
            redirect: { name: 'me-profile' },
          },
          {
            path: 'profile',
            name: 'me-profile',
            component: ProfileView,
            meta: { title: '个人资料' },
          },
          {
            path: 'tracks',
            name: 'me-tracks',
            component: TracksView,
            meta: { title: '我的足迹' },
          },
          {
            path: 'favorites',
            name: 'me-favorites',
            component: FavoritesView,
            meta: { title: '我的收藏' },
          },
          {
            path: 'tasks',
            name: 'me-tasks',
            component: MyTasksView,
            meta: { title: '我的发单' },
          },
          {
            path: 'orders',
            name: 'me-orders',
            component: MyOrdersView,
            meta: { title: '我的接单' },
          },
          {
            path: 'credit',
            name: 'me-credit',
            component: CreditView,
            meta: { title: '信用等级' },
          },
          {
            path: 'account',
            name: 'me-account',
            component: AccountView,
            meta: { title: '账户设置' },
          },
        ],
      },
      {
        path: '403',
        name: 'forbidden',
        component: ForbiddenView,
        meta: { title: '无权访问' },
      },
    ],
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, requiresAdmin: true, title: '管理台' },
    children: [
      {
        path: '',
        redirect: { name: 'admin-stats' },
      },
      {
        path: 'stats',
        name: 'admin-stats',
        component: AdminStatsView,
        meta: { title: '数据总览' },
      },
      {
        path: 'users',
        name: 'admin-users',
        component: AdminUsersView,
        meta: { title: '用户管理' },
      },
      {
        path: 'tasks',
        name: 'admin-tasks',
        component: AdminTasksView,
        meta: { title: '任务管理' },
      },
      {
        path: 'posts',
        name: 'admin-posts',
        component: AdminPostsView,
        meta: { title: '帖子管理' },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: NotFoundView,
    meta: { title: '页面未找到' },
  },
]

export const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

router.beforeEach((to) => {
  const authStore = useAuthStore()

  if (!authStore.initialized) {
    authStore.restoreSession()
  }

  const requiresAuth = to.matched.some((record) => record.meta.requiresAuth)
  const requiresAdmin = to.matched.some((record) => record.meta.requiresAdmin)
  const guestOnly = to.matched.some((record) => record.meta.guestOnly)

  if (guestOnly && authStore.isLoggedIn) {
    return authStore.role === 'ADMIN' ? '/admin/stats' : '/'
  }

  if (requiresAuth && !authStore.isLoggedIn) {
    return {
      name: 'login',
      query: { redirect: to.fullPath },
    }
  }

  if (requiresAdmin && authStore.role !== 'ADMIN') {
    return '/403'
  }

  if (typeof to.meta.title === 'string') {
    document.title = `${to.meta.title} - CampusHub`
  }

  return true
})