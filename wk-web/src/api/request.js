// 统一请求封装：超时控制、query 拼接、请求头注入、JSON 处理、业务码校验、错误规范化

const DEFAULT_TIMEOUT = 15000 // 默认超时 15s

// 常见 HTTP 状态码的中文错误文案
const STATUS_TEXT = {
  401: '未登录或登录已过期',
  403: '没有访问权限',
  404: '请求的资源不存在',
  500: '服务器内部错误',
  502: '网关错误',
  503: '服务暂不可用'
}

// 拼接 query：忽略 undefined / null / 空字符串，已有 ? 时用 & 续接
function buildUrl(url, params) {
  if (!params) return url
  const qs = new URLSearchParams(
    Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== '')
  ).toString()
  if (!qs) return url
  return `${url}${url.includes('?') ? '&' : '?'}${qs}`
}

// 超时控制：到点 abort，由外层统一转成超时错误
function fetchWithTimeout(url, options, timeout) {
  const controller = new AbortController()
  const timer = setTimeout(() => controller.abort(), timeout)
  return fetch(url, { ...options, signal: controller.signal }).finally(() => clearTimeout(timer))
}

/**
 * 核心请求方法
 * @param {string} url 请求地址
 * @param {object} [config]
 * @param {string} [config.method='GET'] 请求方法
 * @param {object} [config.params] query 参数，自动拼接，空值忽略
 * @param {object} [config.data] body 数据，自动 JSON.stringify
 * @param {number} [config.timeout=15000] 超时毫秒
 * @param {object} [config.headers] 额外请求头
 */
export async function request(url, { method = 'GET', params, data, timeout = DEFAULT_TIMEOUT, headers = {} } = {}) {
  // 有登录体系时 localStorage 写入 token 即自动携带，无则不带头
  const token = localStorage.getItem('token')
  const finalHeaders = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...headers
  }

  // 网络层：超时与断网统一转成中文错误
  let res
  try {
    res = await fetchWithTimeout(
      buildUrl(url, params),
      { method, headers: finalHeaders, ...(data !== undefined ? { body: JSON.stringify(data) } : {}) },
      timeout
    )
  } catch (e) {
    if (e.name === 'AbortError') throw new Error('请求超时，请稍后重试')
    throw new Error('网络异常，请检查网络连接')
  }

  // 解析层：响应非 JSON（如网关 HTML 错误页）按状态码报错
  let body
  try {
    body = await res.json()
  } catch {
    throw new Error(STATUS_TEXT[res.status] || `请求失败（${res.status}）`)
  }

  // HTTP 层错误：优先用后端 msg，其次状态码文案
  if (!res.ok) {
    throw new Error(body?.msg || STATUS_TEXT[res.status] || `请求失败（${res.status}）`)
  }
  // 业务层错误：body 为 null（如响应体是 "null"）时视为异常响应，code !== 0 抛业务错误
  if (!body || body.code !== 0) {
    throw new Error(body?.msg || '请求失败')
  }
  return body.data
}

// 便捷方法：调用方不再关心 method 与 body 序列化
export const get = (url, params, config) => request(url, { ...config, params })
export const post = (url, data, config) => request(url, { ...config, method: 'POST', data })
export const put = (url, data, config) => request(url, { ...config, method: 'PUT', data })
export const del = (url, config) => request(url, { ...config, method: 'DELETE' })
