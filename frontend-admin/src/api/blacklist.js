import request from '@/utils/request'

export function addToBlacklist(userId, type, reason) {
  return request.post('/blacklist/add', null, { params: { userId, type, reason } })
}

export function removeFromBlacklist(id) {
  return request.delete(`/blacklist/remove/${id}`)
}

export function getBlacklistPage(params) {
  return request.get('/blacklist/list', { params })
}
