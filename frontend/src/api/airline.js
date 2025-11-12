import http from '@/api/http'

// 服务端分页：航线列表
export function pageAirlines(page = 1, size = 10) {
    return http.get('/api/admin/airline/list', { params: { page, size } })
    // 期望返回：{ total, page, size, records: Airline[] }
}

// 服务端分页：按出发 + 到达搜索
export function pageAirlinesByFromTo(fromAirport, toAirport, page = 1, size = 10) {
    return http.get('/api/admin/airline', { params: { fromAirport, toAirport, page, size } })
}

export function addAirline(payload) {
    return http.post('/api/admin/airline/add', payload) // { fromAirport, toAirport }
}

export function updateAirline(payload) {
    return http.post('/api/admin/airline/update', payload) // { id, fromAirport, toAirport }
}

export function deleteAirline(airlineId) {
    return http.get(`/api/admin/airline/delete/${airlineId}`)
}
