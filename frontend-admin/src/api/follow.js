import request from '@/utils/request'

export function submitFollowUp(data) {
  return request.post('/follow/submit', data)
}

export function getMyRecords(params) {
  return request.get('/follow/my-records', { params })
}

export function getPetRecords(petId, params) {
  return request.get(`/follow/pet-records/${petId}`, { params })
}

export function getPendingList(params) {
  return request.get('/follow/pending', { params })
}

export function reviewFollowUp(id, status, comment) {
  return request.put('/follow/review', null, { params: { id, status, comment } })
}

export function reclaimPet(petId, reason) {
  return request.post('/follow/reclaim', null, { params: { petId, reason } })
}
