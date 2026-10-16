import { defineComponent, nextTick } from 'vue'
import { mount } from '@vue/test-utils'
import { useClock } from '@/composables/useClock'

// 挂一个宿主组件，让 composable 拿到组件生命周期
function mountClock() {
  let result
  const Comp = defineComponent({
    setup() {
      result = useClock()
      return () => null
    }
  })
  const wrapper = mount(Comp)
  return { wrapper, nowText: result }
}

describe('useClock', () => {
  beforeEach(() => vi.useFakeTimers())
  afterEach(() => vi.useRealTimers())

  it('挂载后输出格式化时间文本', () => {
    vi.setSystemTime(new Date(2026, 9, 16, 9, 5, 3))
    const { wrapper, nowText } = mountClock()
    expect(nowText.value).toBe('2026-10-16 09:05:03')
    wrapper.unmount()
  })

  it('每秒更新一次', async () => {
    vi.setSystemTime(new Date(2026, 9, 16, 9, 5, 3))
    const { wrapper, nowText } = mountClock()
    // advanceTimersByTime 会顺带把 fake 系统时间推进 1s，无需再 setSystemTime
    vi.advanceTimersByTime(1000)
    await nextTick()
    expect(nowText.value).toBe('2026-10-16 09:05:04')
    wrapper.unmount()
  })

  it('卸载后清除定时器，不再更新', async () => {
    vi.setSystemTime(new Date(2026, 9, 16, 9, 5, 3))
    const { wrapper, nowText } = mountClock()
    wrapper.unmount()
    vi.setSystemTime(new Date(2026, 9, 16, 9, 5, 10))
    vi.advanceTimersByTime(2000)
    await nextTick()
    expect(nowText.value).toBe('2026-10-16 09:05:03')
  })
})
