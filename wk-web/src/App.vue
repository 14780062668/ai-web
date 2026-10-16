<script setup>
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  FundOutlined,
  FullscreenExitOutlined,
  FullscreenOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  UserOutlined
} from '@ant-design/icons-vue'
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import { menus } from '@/menus'
import { useClock } from '@/composables/useClock'

const route = useRoute()
const router = useRouter()
const nowText = useClock() // 头部时钟，每秒更新

// 面包屑第二级：来自路由 meta.title（router/index.js 中配置）
const pageTitle = computed(() => route.meta.title || '')

// 菜单点击：大屏是独立全屏路由，普通菜单从 menus 里查 path 跳转
function onMenu({ key }) {
  if (key === 'screen') {
    router.push('/screen')
    return
  }
  const item = menus.find((menu) => menu.key === key)
  if (item) {
    router.push(item.path)
  }
}

// 页签栏：记录打开过的页面（去重），可切换/关闭
const tabs = reactive([])

watch(
  () => route.path,
  () => {
    // 全屏页（登录页）不进页签
    if (route.meta.title && !route.meta.fullscreen && !tabs.some((t) => t.path === route.path)) {
      tabs.push({ path: route.path, title: route.meta.title })
    }
  },
  { immediate: true }
)

function onTabChange(path) {
  router.push(path)
}

// 退出登录：清令牌回登录页
function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('tokenExpires')
  router.push('/login')
}

// 用户下拉菜单点击
function onUserMenu({ key }) {
  if (key === 'logout') {
    logout()
  }
}

// 侧边栏折叠状态（头部按钮与断点自动折叠共用 v-model 同步）
const collapsed = ref(false)

// 全屏切换：跟随 fullscreenchange 同步图标，兼容 ESC 退出
const isFullscreen = ref(false)
const syncFullscreen = () => {
  isFullscreen.value = !!document.fullscreenElement
}
function toggleFullscreen() {
  if (document.fullscreenElement) {
    document.exitFullscreen()
  } else {
    document.documentElement.requestFullscreen()
  }
}
onMounted(() => document.addEventListener('fullscreenchange', syncFullscreen))
onUnmounted(() => document.removeEventListener('fullscreenchange', syncFullscreen))

function onTabEdit(path, action) {
  if (action !== 'remove') return
  const i = tabs.findIndex((t) => t.path === path)
  if (i === -1) return
  tabs.splice(i, 1)
  // 关闭当前页时跳到相邻页签，没有则回首页
  if (path === route.path) {
    const next = tabs[i - 1] || tabs[0]
    router.push(next ? next.path : '/items')
  }
}
</script>

<template>
  <ConfigProvider :locale="zhCN" :auto-insert-space-in-button="false">
    <!-- 登录页等全屏页面：脱离后台布局直接渲染 -->
    <router-view v-if="route.meta.fullscreen" :key="route.path" />
    <Layout v-else class="admin">
      <LayoutSider
        v-model:collapsed="collapsed"
        class="sider"
        theme="dark"
        width="220"
        collapsible
        :trigger="null"
        breakpoint="lg"
        :collapsed-width="80"
      >
        <div class="logo">{{ collapsed ? 'WK' : '后台管理' }}</div>
        <!-- inline-collapsed 收起为纯图标，悬停菜单项自动弹出名称提示 -->
        <Menu
          theme="dark"
          mode="inline"
          :inline-collapsed="collapsed"
          :selected-keys="[route.meta.menu]"
          @click="onMenu"
        >
          <MenuItem key="screen">
            <template #icon><FundOutlined /></template>
            数据大屏
          </MenuItem>
          <MenuItem v-for="item in menus" :key="item.key">
            <template #icon><component :is="item.icon" /></template>
            {{ item.title }}
          </MenuItem>
        </Menu>
      </LayoutSider>
      <Layout class="main">
        <LayoutHeader class="header">
          <div class="header-left">
            <!-- 折叠侧边栏按钮：贴菜单栏无背景图标按钮 -->
            <Button type="text" class="header-icon-btn collapse-btn" @click="collapsed = !collapsed">
              <template #icon>
                <MenuUnfoldOutlined v-if="collapsed" />
                <MenuFoldOutlined v-else />
              </template>
            </Button>
            <Breadcrumb>
              <BreadcrumbItem>后台管理</BreadcrumbItem>
              <BreadcrumbItem>{{ pageTitle }}</BreadcrumbItem>
            </Breadcrumb>
          </div>
          <div class="header-right">
            <!-- 全屏切换 -->
            <Tooltip :title="isFullscreen ? '退出全屏' : '全屏'">
              <Button type="text" class="header-icon-btn" @click="toggleFullscreen">
                <template #icon>
                  <FullscreenExitOutlined v-if="isFullscreen" />
                  <FullscreenOutlined v-else />
                </template>
              </Button>
            </Tooltip>
            <span class="clock">{{ nowText }}</span>
            <!-- 用户菜单 -->
            <Dropdown placement="bottomRight" :trigger="['click']">
              <div class="user">
                <Avatar :size="28">
                  <template #icon><UserOutlined /></template>
                </Avatar>
                <span class="username">管理员</span>
              </div>
              <template #overlay>
                <Menu @click="onUserMenu">
                  <MenuItem key="logout">
                    <template #icon><LogoutOutlined /></template>
                    退出登录
                  </MenuItem>
                </Menu>
              </template>
            </Dropdown>
          </div>
        </LayoutHeader>
        <!-- 页签栏：显示打开过的页面，卡片式可关闭（最后一个不可关） -->
        <Tabs
          class="tabs-bar"
          type="editable-card"
          hide-add
          size="small"
          :active-key="route.path"
          @change="onTabChange"
          @edit="onTabEdit"
        >
          <TabPane v-for="t in tabs" :key="t.path" :tab="t.title" :closable="tabs.length > 1" />
        </Tabs>
        <LayoutContent class="content">
          <!-- 不加 key：菜单各分类共用 ItemList，切换时复用组件，由组件内 watch category 刷新 -->
          <router-view />
        </LayoutContent>
      </Layout>
    </Layout>
  </ConfigProvider>
</template>

<style lang="scss">
html,
body,
#app {
  height: 100%;
  margin: 0;
}

.admin {
  height: 100vh;
}

.sider {
  overflow: hidden auto; // 只保留纵向滚动，横向裁剪掉避免出横向滚动条

  /* 小屏：收起态完全隐藏（桌面端收起为 80px 图标栏） */
  @media (max-width: 768px) {
    &.ant-layout-sider-collapsed {
      flex: 0 0 0 !important;
      width: 0 !important;
      min-width: 0 !important;
      max-width: 0 !important;
    }
  }
}

.logo {
  height: 64px;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  line-height: 64px;
  text-align: center;
}

.main {
  min-width: 0;
  background: #f0f2f5;
}

/* .admin 前缀提权：压过 antd LayoutHeader 默认的 0 50px 内边距 */
.admin .header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;

  // 小屏适配：缩小留白
  @media (max-width: 768px) {
    height: 56px;
    padding: 0 12px;
  }
}

.header-left {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #1f2937;
}

/* 头部图标按钮：悬停浅灰底 */
.header-icon-btn {
  font-size: 16px;

  &:hover {
    background: rgba(0, 0, 0, 0.04);
  }
}

/* 折叠按钮：无背景图标按钮，抵消 header 左内边距贴着菜单栏 */
.collapse-btn {
  height: 64px;
  margin-left: -24px;
  padding: 0 20px;
  border-radius: 0;

  &:hover,
  &:focus {
    background: rgba(0, 0, 0, 0.04) !important;
  }

  @media (max-width: 768px) {
    height: 56px;
    margin-left: -12px;
    padding: 0 14px;
  }
}

/* 用户区：可点击，悬停浅灰底 */
.user {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: rgba(0, 0, 0, 0.04);
  }
}

.username {
  @media (max-width: 768px) {
    display: none; // 小屏只留头像
  }
}

.clock {
  color: #6b7280;
  font-variant-numeric: tabular-nums;

  // 小屏隐藏时钟，避免头部拥挤
  @media (max-width: 768px) {
    display: none;
  }
}

/* 页签栏：固定 40px，贴合 header 下方 */
.tabs-bar {
  height: 40px;
  padding: 0 16px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;

  .ant-tabs-nav {
    margin-bottom: 0;
  }
}

.content {
  padding: 16px;
  overflow: auto;

  @media (max-width: 768px) {
    padding: 8px;
  }
}

.panel {
  min-height: 100%;
  padding: 16px 24px;
  background: #fff;

  @media (max-width: 768px) {
    padding: 12px;
  }
}
</style>
