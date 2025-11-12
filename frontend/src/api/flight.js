import http from '@/api/http'

// 分页列表：GET /admin/flight/list?page=&size=
export function pageFlights(page = 1, size = 10) {
    return http.get('/api/admin/flight/list', { params: { page, size } })
}

// 详情：GET /admin/flight/{id}
export function getFlight(id) {
    return http.get(`/api/admin/flight/${id}`)
}

// 新增：POST /admin/flight/add
export function addFlight(dto) {
    // dto: FlightDTO
    return http.post('/api/admin/flight/add', dto)
}

// 修改：POST /admin/flight/update
export function updateFlight(dto) {
    // dto: FlightUpdateDTO
    return http.post('/api/admin/flight/update', dto)
}

// 删除：POST /admin/flight/delete/{id}
export function deleteFlight(id) {
    return http.post(`/api/admin/flight/delete/${id}`)
}
