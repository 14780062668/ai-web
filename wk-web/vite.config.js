import { createRequire } from 'node:module'
import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import Components from 'unplugin-vue-components/vite'
import { AntDesignVueResolver } from 'unplugin-vue-components/resolvers'

const require = createRequire(import.meta.url)

// 模板中使用到的 antd 组件（无前缀写法，如 <Button>）
const antdComponents = new Set([
  'Avatar', 'Alert', 'Breadcrumb', 'BreadcrumbItem', 'Button', 'CheckboxGroup', 'ConfigProvider', 'Dropdown', 'Tooltip',
  'Flex', 'Form', 'FormItem', 'Input', 'InputNumber', 'InputPassword', 'InputSearch', 'Layout', 'LayoutContent', 'LayoutHeader',
  'LayoutSider', 'Menu', 'MenuItem', 'Modal', 'Pagination', 'Popconfirm', 'RadioGroup', 'Table', 'Tabs', 'TabPane', 'Textarea'
])

const officialResolver = AntDesignVueResolver({ importStyle: 'css' })

// 官方 resolver 只识别 A 前缀（AButton），这里转发无前缀组件名，保持模板写法简洁
const antdResolver = {
  type: 'component',
  resolve(name) {
    const result = antdComponents.has(name)
      ? officialResolver.resolve(`A${name}`)
      : officialResolver.resolve(name)
    // 官方对无样式组件（如 ConfigProvider）也会生成 style/css 路径，校验存在性，不存在则去掉
    if (result && result.sideEffects) {
      try {
        require.resolve(result.sideEffects)
      } catch {
        const { sideEffects, ...rest } = result
        return rest
      }
    }
    return result
  }
}

export default defineConfig({
  plugins: [
    vue(),
    // antd 组件自动按需引入，模板中直接使用无需手动 import
    Components({
      resolvers: [antdResolver]
    })
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        api: 'modern-compiler' // 使用 Sass 现代 API，消除 legacy-js-api 弃用警告
      }
    }
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          vue: ['vue', 'vue-router'],
          antd: ['ant-design-vue'],
          icons: ['@ant-design/icons-vue']
        }
      }
    }
  },
  server: {
    port: 5173,
    proxy: {
      // 开发时代理到 Java 服务，避免浏览器跨域
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  test: {
    environment: 'jsdom', // 模拟浏览器环境（localStorage、DOM）
    globals: true,        // describe/it/expect 免导入
    restoreMocks: true    // 每个用例后自动还原 mock
  }
})
