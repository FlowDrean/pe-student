import { createRouter, createWebHashHistory } from 'vue-router'
import { useAdminStore } from '@/stores/admin'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/pages/Login.vue'),
    meta: { title: '管理员登录' },
  },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/pages/Dashboard.vue'),
        meta: { title: '仪表盘', icon: 'Odometer' },
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/pages/user/UserList.vue'),
        meta: { title: '用户管理', icon: 'User' },
      },
      {
        path: 'posts',
        name: 'Posts',
        component: () => import('@/pages/post/PostList.vue'),
        meta: { title: '帖子管理', icon: 'Document' },
      },
      {
        path: 'trades',
        name: 'Trades',
        component: () => import('@/pages/trade/TradeList.vue'),
        meta: { title: '商品管理', icon: 'ShoppingBag' },
      },
      {
        path: 'comments',
        name: 'Comments',
        component: () => import('@/pages/comment/CommentList.vue'),
        meta: { title: '评论管理', icon: 'ChatLineSquare' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - PE-Student` : 'PE-Student 后台管理'
  const adminStore = useAdminStore()
  if (to.path === '/login') {
    if (adminStore.token) {
      next('/')
    } else {
      next()
    }
  } else {
    if (!adminStore.token) {
      next('/login')
    } else {
      next()
    }
  }
})

export default router
