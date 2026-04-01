import request from '@/utils/request'

export function login(data) {
  return request.post('/user/login', data)
}

export function register(data) {
  return request.post('/user/register', data)
}

export function getUserInfo() {
  return request.get('/user/info')
}

export function updateUser(data) {
  return request.put('/user/update', data)
}

export function submitVerify(data) {
  return request.post('/user/verify', data)
}

export function getUserList(params) {
  return request.get('/user/list', { params })
}

export function updateUserStatus(userId, status) {
  return request.put('/user/status', null, { params: { userId, status } })
}
