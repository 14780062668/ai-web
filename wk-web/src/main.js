import { createApp } from 'vue'
// antd 全局重置样式；组件本身由 unplugin-vue-components 按需自动引入（见 vite.config.js）
import 'ant-design-vue/dist/reset.css'
import App from './App.vue'
import router from './router'

createApp(App).use(router).mount('#app')
