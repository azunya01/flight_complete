// src/api/admin.js
import http from '@/api/http'

// 新增航线
export function addAirline(payload) {
    // payload: { fromAirport: 'PEK', toAirport: 'SHA' }
    return http.post('/admin/airline/add', payload)
}

// 删除航线（后端是 GET + path 变量）
export function deleteAirline(airlineId) {
    return http.get(`/admin/airline/delete/${airlineId}`)
}
