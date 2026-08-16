/* ============================================
   路由配置
   ============================================ */

import { createRouter, createWebHashHistory } from 'vue-router'
import { useUserStore } from '@stores/user'

// 布局组件（懒加载）
const TabLayout = () => import('@layouts/TabLayout.vue')
const DefaultLayout = () => import('@layouts/DefaultLayout.vue')
const AuthLayout = () => import('@layouts/AuthLayout.vue')

const routes = [
  // ── TabBar 布局页面 ──
  {
    path: '/',
    component: TabLayout,
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@pages/home/index.vue'),
        meta: { title: '首页', keepAlive: true },
      },
      {
        path: 'trade',
        name: 'Trade',
        component: () => import('@pages/trade/index.vue'),
        meta: { title: '二手市场', keepAlive: true },
      },
      {
        path: 'message',
        name: 'Message',
        component: () => import('@pages/message/index.vue'),
        meta: { title: '消息', requiresAuth: true },
      },
      {
        path: 'user',
        name: 'UserCenter',
        component: () => import('@pages/user/Profile.vue'),
        meta: { title: '我的', requiresAuth: true },
      },
    ],
  },

  // ── DefaultLayout 页面 ──
  {
    path: '/search',
    component: DefaultLayout,
    children: [
      {
        path: '',
        name: 'Search',
        component: () => import('@pages/home/Search.vue'),
        meta: { title: '搜索' },
      },
    ],
  },
  {
    path: '/category/:type',
    component: DefaultLayout,
    children: [
      {
        path: '',
        name: 'Category',
        component: () => import('@pages/home/Category.vue'),
        meta: { title: '分类' },
      },
    ],
  },
  {
    path: '/post',
    component: DefaultLayout,
    children: [
      {
        path: ':id',
        name: 'PostDetail',
        component: () => import('@pages/post/Detail.vue'),
        meta: { title: '帖子详情' },
      },
      {
        path: 'create',
        name: 'CreatePost',
        component: () => import('@pages/post/Create.vue'),
        meta: { title: '发布帖子', requiresAuth: true },
      },
      {
        path: ':id/edit',
        name: 'EditPost',
        component: () => import('@pages/post/Edit.vue'),
        meta: { title: '编辑帖子', requiresAuth: true },
      },
    ],
  },
  {
    path: '/trade-item',
    component: DefaultLayout,
    children: [
      {
        path: ':id',
        name: 'TradeDetail',
        component: () => import('@pages/trade/Detail.vue'),
        meta: { title: '商品详情' },
      },
      {
        path: 'create',
        name: 'CreateTrade',
        component: () => import('@pages/trade/Create.vue'),
        meta: { title: '发布商品', requiresAuth: true },
      },
      {
        path: ':id/edit',
        name: 'EditTrade',
        component: () => import('@pages/trade/Edit.vue'),
        meta: { title: '编辑商品', requiresAuth: true },
      },
    ],
  },
  {
    path: '/chat',
    component: DefaultLayout,
    children: [
      {
        path: ':userId',
        name: 'Chat',
        component: () => import('@pages/message/Chat.vue'),
        meta: { title: '聊天', requiresAuth: true },
      },
    ],
  },
  {
    path: '/notification',
    component: DefaultLayout,
    children: [
      {
        path: '',
        name: 'Notification',
        component: () => import('@pages/message/Notification.vue'),
        meta: { title: '通知中心', requiresAuth: true },
      },
    ],
  },

  // ── 用户相关 ──
  {
    path: '/user-profile',
    component: DefaultLayout,
    children: [
      {
        path: ':id',
        name: 'UserProfile',
        component: () => import('@pages/user/Profile.vue'),
        meta: { title: '个人主页' },
      },
      {
        path: 'edit',
        name: 'EditProfile',
        component: () => import('@pages/user/EditProfile.vue'),
        meta: { title: '编辑资料', requiresAuth: true },
      },
      {
        path: 'posts',
        name: 'MyPosts',
        component: () => import('@pages/user/MyPosts.vue'),
        meta: { title: '我的帖子', requiresAuth: true },
      },
      {
        path: 'trades',
        name: 'MyTrades',
        component: () => import('@pages/user/MyTrades.vue'),
        meta: { title: '我的商品', requiresAuth: true },
      },
      {
        path: 'favorites',
        name: 'Favorites',
        component: () => import('@pages/user/Favorites.vue'),
        meta: { title: '我的收藏', requiresAuth: true },
      },
      {
        path: 'follows',
        name: 'MyFollows',
        component: () => import('@pages/user/FollowList.vue'),
        meta: { title: '关注与粉丝', requiresAuth: true },
      },
      {
        path: ':id/follows',
        name: 'UserFollows',
        component: () => import('@pages/user/FollowList.vue'),
        meta: { title: '关注与粉丝' },
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@pages/user/Settings.vue'),
        meta: { title: '设置', requiresAuth: true },
      },
    ],
  },

  // ── 认证页面 ──
  {
    path: '/auth',
    component: AuthLayout,
    children: [
      {
        path: 'login',
        name: 'Login',
        component: () => import('@pages/auth/Login.vue'),
        meta: { title: '登录', transition: 'auth' },
      },
      {
        path: 'register',
        name: 'Register',
        component: () => import('@pages/auth/Register.vue'),
        meta: { title: '注册', transition: 'auth' },
      },
      {
        path: 'forgot-password',
        name: 'ForgotPassword',
        component: () => import('@pages/auth/ForgotPassword.vue'),
        meta: { title: '找回密码' },
      },
    ],
  },

  // ── 404 ──
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

// 导航守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title
    ? `${to.meta.title} - 校园互助`
    : '校园互助 — 连接每一份温暖'

  // 需要登录的页面
  if (to.meta.requiresAuth) {
    const userStore = useUserStore()
    if (!userStore.isLogin) {
      return next({ name: 'Login', query: { redirect: to.fullPath } })
    }
  }

  // 已登录用户访问登录/注册页，重定向到首页
  if (to.name === 'Login' || to.name === 'Register') {
    const userStore = useUserStore()
    if (userStore.isLogin) {
      return next({ path: '/' })
    }
  }

  next()
})

export default router
