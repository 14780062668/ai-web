// 菜单数据源：同时驱动侧边菜单（App.vue）与路由表（router/index.js）
// category 是前后端约定值，新增分类时后端 ItemController.CATEGORIES 必须同步加白名单
import {
  FileTextOutlined,
  NotificationOutlined,
  ScheduleOutlined,
  UnorderedListOutlined
} from '@ant-design/icons-vue'

export const menus = [
  { key: 'record', path: '/items', title: '记录列表', category: 'record', icon: UnorderedListOutlined },
  { key: 'notice', path: '/notices', title: '通知公告', category: 'notice', icon: NotificationOutlined },
  { key: 'task', path: '/tasks', title: '工作任务', category: 'task', icon: ScheduleOutlined },
  { key: 'doc', path: '/docs', title: '资料文档', category: 'doc', icon: FileTextOutlined }
]
