import request from '@/utils/request'

export function applyAdoption(data) {
  return request.post('/adoption/apply', data)
}

export function getMyApplications(params) {
  return request.get('/adoption/my-applications', { params })
}

export function getReceivedApplications(params) {
  return request.get('/adoption/received', { params })
}

export function getPendingReviewList(params) {
  return request.get('/adoption/pending-review', { params })
}

export function rescueReview(data) {
  return request.put('/adoption/rescue-review', data)
}

export function adminReview(data) {
  return request.put('/adoption/admin-review', data)
}

export function updateHomeVisit(data) {
  return request.put('/adoption/home-visit', data)
}

export function getApplicationDetail(id) {
  return request.get(`/adoption/detail/${id}`)
}
