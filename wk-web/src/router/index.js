import { createRouter, createWebHistory } from 'vue-router'
import ItemList from '@/views/ItemList.vue'
import { menus } from '@/menus'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/items' },
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

export default router
