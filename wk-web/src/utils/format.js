// 后端时间字符串格式化为 'YYYY-MM-DD HH:mm:ss'
export function formatTime(value) {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 19)
}
