import { createRouter, createWebHistory } from 'vue-router'
import toast from '@/utils/toast'
import { useUserStore } from '@/stores/userManageStore'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/HomeView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { requiresGuest: true } // 明确标记不需要登录
    },
    {
      path: '/test',
      name: 'test',
      component: () => import('@/views/TestView.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/error',
      name: 'error',
      component: () => import('@/components/error/500.vue'),
      meta: {
        title: '500 - 服务器错误'
      }
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/components/error/404.vue'),
      meta: {
        title: '404 - 页面不存在'
      }
    }
  ],
})

router.beforeEach(async (to, from, next) => {
  // 1. 调试信息（仅开发环境显示）
  if (import.meta.env.DEV) {
    console.log('[路由守卫]', `从 ${from.path} 跳转到 ${to.path}`, to.meta)
  }

  // 2. 获取用户状态（Pinia Store）
  const userStore = useUserStore()

  // 3. 路由鉴权逻辑
  const isAuthenticated = userStore.isLoggedIn

  // 情况1：需要登录但未认证
  if (to.meta.requiresAuth && !isAuthenticated) {
    toast.warning('请先登录', '您需要登录才能访问该页面')
    return next({
      path: '/login',
      query: { redirect: to.fullPath }, // 记录来源页以便登录后跳回
      replace: true // 替换历史记录，避免后退按钮问题
    })
  }

  // 情况2：已登录但访问游客专属页（如登录/注册页）
  if (to.meta.requiresGuest && isAuthenticated) {
    toast.success('您已登录', '请先退出登录')
    return next('/') // 或 from.path（根据业务需求）
  }

  // 情况3：正常放行
  next()
})

export default router