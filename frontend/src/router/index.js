import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/system/login-log'
  },
  {
    path: '/system/login-log',
    name: 'LoginLog',
    component: () => import('@/views/system/login-log/index.vue'),
    meta: { title: '登录日志' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - IoT 平台` : 'IoT 平台'
  next()
})

export default router
