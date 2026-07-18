import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 120000
})

api.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  error => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default {
  // 语料
  getCorpus(params) { return api.get('/corpus', { params }) },
  createCorpus(data) { return api.post('/corpus', data) },
  updateCorpus(id, data) { return api.put(`/corpus/${id}`, data) },
  deleteCorpus(id) { return api.delete(`/corpus/${id}`) },
  importCorpus(formData) { return api.post('/corpus/import', formData, { headers: { 'Content-Type': 'multipart/form-data' } }) },
  batchDeleteCorpus(ids) { return api.post('/corpus/batch-delete', ids) },

  // 练习
  generateQuestions(data) { return api.post('/exercise/generate', data) },
  startSession(data) { return api.post('/session/start', data) },
  submitAnswer(sessionId, data) { return api.post(`/session/${sessionId}/evaluate`, data) },
  finishSession(sessionId) { return api.post(`/session/${sessionId}/finish`) },
  getSession(id) { return api.get(`/session/${id}`) },
  getSessionAnswers(id) { return api.get(`/session/${id}/answers`) },
  getSessions() { return api.get('/session') },

  // 统计
  getStatsOverview() { return api.get('/stats/overview') },
  getStatsTrend() { return api.get('/stats/trend') },
  getStatsByCategory() { return api.get('/stats/by-category') },
  getWeakCorpus() { return api.get('/stats/weak-corpus') },
  getAvgDuration() { return api.get('/stats/avg-duration') },
  exportCsv() { return api.get('/stats/export', { responseType: 'blob' }) },

  // AI 后端
  getProviders() { return api.get('/provider') },
  createProvider(data) { return api.post('/provider', data) },
  updateProvider(id, data) { return api.put(`/provider/${id}`, data) },
  deleteProvider(id) { return api.delete(`/provider/${id}`) },
  testProvider(id) { return api.post(`/provider/${id}/test`) },
}
