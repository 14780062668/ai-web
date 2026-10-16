import { createRouter, createWebHistory } from 'vue-router'
import ItemList from '@/views/ItemList.vue'
import { menus } from '@/menus'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/items' },
    // 登录页：全屏独立页面，不带后台布局
    {
      path: '/login',
      component: () => import('@/views/Login.vue'),
      meta: { title: '登录', fullscreen: true }
    },
    // 数据大屏：懒加载不进入后台首屏包，menu 用于侧边栏高亮
    {
      path: '/screen',
      component: () => import('@/views/Screen.vue'),
      meta: { title: '数据大屏', menu: 'screen' }
    },
    // 各分类共用 ItemList 组件，通过 meta.category 区分；组件内 watch category 换数据
    ...menus.map((item) => ({
      path: item.path,
      component: ItemList,
      meta: { title: item.title, category: item.category, menu: item.key }
    })),
    // 兜底：未匹配路径重定向回首页，避免白屏
    { path: '/:pathMatch(.*)*', redirect: '/items' }
  ]
})

// 登录守卫：无令牌或已过期（1 天）一律去登录页；已登录访问登录页则回首页
router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  const expires = Number(localStorage.getItem('tokenExpires') || 0)
  const valid = !!token && Date.now() < expires
  if (!valid) {
    localStorage.removeItem('token')
    localStorage.removeItem('tokenExpires')
    if (to.path !== '/login') {
      // 记录来源页，登录成功后回跳
      return to.fullPath === '/' ? { path: '/login' } : { path: '/login', query: { redirect: to.fullPath } }
    }
  } else if (to.path === '/login') {
    return { path: '/' }
  }
})

export default router
