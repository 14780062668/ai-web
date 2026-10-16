<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { FundOutlined, UserOutlined } from '@ant-design/icons-vue'
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
</script>

<template>
  <ConfigProvider :locale="zhCN" :auto-insert-space-in-button="false">
    <Layout class="admin">
      <LayoutSider
        class="sider"
        theme="dark"
        width="220"
        collapsible
        breakpoint="lg"
        :collapsed-width="0"
      >
        <div class="logo">后台管理</div>
        <Menu theme="dark" mode="inline" :selected-keys="[route.meta.menu]" @click="onMenu">
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
          <Breadcrumb>
            <BreadcrumbItem>后台管理</BreadcrumbItem>
            <BreadcrumbItem>{{ pageTitle }}</BreadcrumbItem>
          </Breadcrumb>
          <div class="header-right">
            <span class="clock">{{ nowText }}</span>
            <Avatar :size="28">
              <template #icon><UserOutlined /></template>
            </Avatar>
            <span>管理员</span>
          </div>
        </LayoutHeader>
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

.header {
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

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #1f2937;
}

.clock {
  color: #6b7280;
  font-variant-numeric: tabular-nums;

  // 小屏隐藏时钟，避免头部拥挤
  @media (max-width: 768px) {
    display: none;
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
