import { onMounted, onUnmounted, ref } from 'vue'

function pad(value) {
  return String(value).padStart(2, '0')
}

// 每秒更新的当前时间文本，组件卸载时自动清理定时器
export function useClock() {
  const nowText = ref('')
  let timer = 0

  function tick() {
    const now = new Date()
    nowText.value = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
  }

  onMounted(() => {
    tick()
    timer = setInterval(tick, 1000)
  })
  onUnmounted(() => clearInterval(timer))

  return nowText
}
