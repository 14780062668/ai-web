import { get, post, del } from '@/api/request'

// 构造一个 fetch 风格的 Response
function mockResponse({ ok = true, status = 200, body = { code: 0, data: null } }) {
  return { ok, status, json: () => Promise.resolve(body) }
}

beforeEach(() => {
  localStorage.clear()
  vi.stubGlobal('fetch', vi.fn())
})

describe('request', () => {
  it('GET 拼接 query 并忽略空值', async () => {
    fetch.mockResolvedValue(mockResponse({ body: { code: 0, data: [1, 2] } }))
    const data = await get('/api/items', { page: 1, keyword: '', extra: null })
    expect(fetch).toHaveBeenCalledWith(
      '/api/items?page=1',
      expect.objectContaining({ method: 'GET' })
    )
    expect(data).toEqual([1, 2])
  })

  it('POST 序列化 body 并带 JSON 头', async () => {
    fetch.mockResolvedValue(mockResponse({}))
    await post('/api/items', { name: 'test' })
    const [, options] = fetch.mock.calls[0]
    expect(options.method).toBe('POST')
    expect(options.body).toBe('{"name":"test"}')
    expect(options.headers['Content-Type']).toBe('application/json')
  })

  it('有 token 时自动携带 Authorization', async () => {
    localStorage.setItem('token', 'abc')
    fetch.mockResolvedValue(mockResponse({}))
    await get('/api/items')
    expect(fetch.mock.calls[0][1].headers.Authorization).toBe('Bearer abc')
  })

  it('HTTP 错误优先使用后端 msg', async () => {
    fetch.mockResolvedValue(mockResponse({ ok: false, status: 500, body: { msg: '服务炸了' } }))
    await expect(get('/api/items')).rejects.toThrow('服务炸了')
  })

  it('业务码非 0 抛业务错误', async () => {
    fetch.mockResolvedValue(mockResponse({ body: { code: 1001, msg: '参数错误' } }))
    await expect(get('/api/items')).rejects.toThrow('参数错误')
  })

  it('响应非 JSON 时按状态码报错', async () => {
    fetch.mockResolvedValue({ ok: false, status: 404, json: () => Promise.reject(new Error('not json')) })
    await expect(get('/api/items')).rejects.toThrow('请求的资源不存在')
  })

  it('网络异常转中文错误', async () => {
    fetch.mockRejectedValue(new TypeError('Failed to fetch'))
    await expect(del('/api/items/1')).rejects.toThrow('网络异常，请检查网络连接')
  })

  it('超时报超时错误', async () => {
    // 永不 resolve 的 fetch，靠 abort 触发
    fetch.mockImplementation((url, { signal }) =>
      new Promise((_, reject) => signal.addEventListener('abort', () => {
        const e = new Error('aborted')
        e.name = 'AbortError'
        reject(e)
      }))
    )
    await expect(get('/api/items', {}, { timeout: 50 })).rejects.toThrow('请求超时，请稍后重试')
  })
})
