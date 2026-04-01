import request from '@/utils/request'

export function createKnowledge(data) {
  return request.post('/knowledge/create', data)
}

export function updateKnowledge(data) {
  return request.put('/knowledge/update', data)
}

export function getKnowledgeList(params) {
  return request.get('/knowledge/list', { params })
}

export function getKnowledgeDetail(id) {
  return request.get(`/knowledge/detail/${id}`)
}

export function deleteKnowledge(id) {
  return request.delete(`/knowledge/delete/${id}`)
}
