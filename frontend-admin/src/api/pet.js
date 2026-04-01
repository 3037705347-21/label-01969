import request from '@/utils/request'

export function createPet(data) {
  return request.post('/pet/create', data)
}

export function updatePet(data) {
  return request.put('/pet/update', data)
}

export function getPetDetail(id) {
  return request.get(`/pet/detail/${id}`)
}

export function getPetList(params) {
  return request.get('/pet/list', { params })
}

export function updatePetStatus(id, status) {
  return request.put('/pet/status', null, { params: { id, status } })
}

export function deletePet(id) {
  return request.delete(`/pet/delete/${id}`)
}

export function getMyPetList(params) {
  return request.get('/pet/my-list', { params })
}

export function getMyAdoptedPets(params) {
  return request.get('/pet/my-adopted', { params })
}

// 获取宠物的推荐领养人列表
export function getMatchedAdopters(petId, limit = 10) {
  return request.get(`/match/adopters/${petId}`, { params: { limit } })
}

// 获取首页统计数据
export function getPetStats() {
  return request.get('/pet/stats')
}
