import { formatTime } from '@/utils/format'

describe('formatTime', () => {
  it('标准 ISO 时间转为 YYYY-MM-DD HH:mm:ss', () => {
    expect(formatTime('2026-10-16T09:43:00')).toBe('2026-10-16 09:43:00')
  })

  it('带毫秒/时区后缀时截断到 19 位', () => {
    expect(formatTime('2026-10-16T09:43:00.123Z')).toBe('2026-10-16 09:43:00')
  })

  it('空值返回空字符串', () => {
    expect(formatTime('')).toBe('')
    expect(formatTime(null)).toBe('')
    expect(formatTime(undefined)).toBe('')
  })
})
