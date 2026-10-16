import { del, get, post, put } from './request'

// 分页查询列表，keyword 为空时 request 层自动不拼该参数
export function fetchItems({ category, page, pageSize, keyword }) {
  return get('/api/items', { category, page, pageSize, keyword })
}

export function fetchItem(id) {
  return get(`/api/items/${id}`)
}

// 大屏聚合数据：一次请求拿回各分类总数 + 最近 5 条
export function fetchStats() {
  return get('/api/items/stats')
}

export function createItem(data) {
  return post('/api/items', data)
}

export function updateItem(id, data) {
  return put(`/api/items/${id}`, data)
}

export function deleteItem(id) {
  return del(`/api/items/${id}`)
}
