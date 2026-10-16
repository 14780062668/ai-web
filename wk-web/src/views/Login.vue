<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { LockOutlined, UserOutlined } from '@ant-design/icons-vue'
import { post } from '@/api/request'

const route = useRoute()
const router = useRouter()

const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

// 登录成功：存令牌与过期时间（1 天），回跳来源页或首页
async function submit() {
  error.value = ''
  if (!username.value.trim() || !password.value) {
    error.value = '请输入用户名和密码'
    return
  }
  loading.value = true
  try {
    const data = await post('/api/auth/login', { username: username.value.trim(), password: password.value })
    localStorage.setItem('token', data.token)
    localStorage.setItem('tokenExpires', String(data.expiresAt))
    router.push(typeof route.query.redirect === 'string' ? route.query.redirect : '/')
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <!-- 背景光斑，纯装饰 -->
    <div class="glow glow-a" />
    <div class="glow glow-b" />

    <div class="login-card">
      <div class="brand">
        <div class="brand-logo">WK</div>
        <h1 class="brand-title">后台管理系统</h1>
        <p class="brand-sub">内容管理平台 · 欢迎登录</p>
      </div>

      <Alert v-if="error" class="login-alert" type="error" :message="error" show-icon />

      <Input
        v-model:value="username"
        size="large"
        placeholder="用户名"
        :maxlength="50"
        @keyup.enter="submit"
      >
        <template #prefix><UserOutlined class="input-icon" /></template>
      </Input>
      <InputPassword
        v-model:value="password"
        size="large"
        placeholder="密码"
        :maxlength="50"
        @keyup.enter="submit"
      >
        <template #prefix><LockOutlined class="input-icon" /></template>
      </InputPassword>

      <Button type="primary" size="large" block :loading="loading" @click="submit">登 录</Button>

      <p class="tip">演示账号：admin / admin123</p>
    </div>
  </div>
</template>

<style scoped lang="scss">
/* 全屏深蓝渐变 + 光斑，居中卡片 */
.login-page {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  overflow: hidden;
  background: linear-gradient(135deg, #0d1b3e 0%, #1a2f6b 55%, #0a1430 100%);
}

.glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(90px);
  opacity: 0.5;
  pointer-events: none;
}

.glow-a {
  width: 480px;
  height: 480px;
  top: -120px;
  right: -80px;
  background: radial-gradient(circle, #3b6cff, transparent 70%);
}

.glow-b {
  width: 420px;
  height: 420px;
  bottom: -140px;
  left: -100px;
  background: radial-gradient(circle, #7b4dff, transparent 70%);
}

.login-card {
  position: relative;
  width: 380px;
  max-width: calc(100vw - 32px);
  padding: 40px 36px 28px;
  background: rgba(255, 255, 255, 0.97);
  border-radius: 14px;
  box-shadow: 0 24px 64px rgba(4, 12, 40, 0.45);
  backdrop-filter: blur(8px);
}

.brand {
  margin-bottom: 28px;
  text-align: center;
}

.brand-logo {
  width: 56px;
  height: 56px;
  margin: 0 auto 14px;
  font-size: 22px;
  font-weight: 700;
  line-height: 56px;
  color: #fff;
  letter-spacing: 2px;
  border-radius: 14px;
  background: linear-gradient(135deg, #3b6cff, #1d39c4);
  box-shadow: 0 8px 20px rgba(59, 108, 255, 0.4);
}

.brand-title {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #1f2937;
  letter-spacing: 2px;
}

.brand-sub {
  margin: 8px 0 0;
  font-size: 13px;
  color: #9ca3af;
  letter-spacing: 1px;
}

.login-alert {
  margin-bottom: 16px;
}

/* 输入框间距 */
.login-card :deep(.ant-input-affix-wrapper) {
  margin-bottom: 16px;
}

.input-icon {
  color: #bfbfbf;
}

.login-card :deep(.ant-btn-block) {
  margin-top: 4px;
  letter-spacing: 6px;
}

.tip {
  margin: 20px 0 0;
  font-size: 12px;
  color: #c0c4cc;
  text-align: center;
}
</style>
