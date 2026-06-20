import request from './index'

export const dashboardApi = {
  getStats() {
    return request.get('/dashboard/stats')
  },
  getTrends(days = 7) {
    return request.get('/dashboard/trends', { params: { days } })
  },
}
