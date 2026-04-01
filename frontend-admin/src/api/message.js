import request from '@/utils/request'

export function getMessageList(params) {
  return request.get('/message/list', { params })
}

export function markAsRead(id) {
  return request.put(`/message/read/${id}`)
}

export function markAllAsRead() {
  return request.put('/message/read-all')
}

export function getUnreadCount() {
  return request.get('/message/unread-count')
}
