// src/api/passenger.js
import http from '@/api/http'

/**
 * 新增常用乘客
 * POST /api/user/passengers
 * @param {{name:string, gender:string, id_no:string, phone:string}} dto
 * @returns {Promise<Object>} PassengerVO
 */
export function addPassenger(dto) {
    return http.post('/api/user/passengers', dto)
}

/**
 * 更新常用乘客
 * PUT /api/user/passengers/{id}
 */
export function updatePassenger(id, dto) {
    return http.put(`/api/user/passengers/${id}`, dto)
}

/**
 * 删除常用乘客
 * DELETE /api/user/passengers/{id}
 */
export function deletePassenger(id) {
    return http.delete(`/api/user/passengers/${id}`)
}

/**
 * 获取单个常用乘客
 * GET /api/user/passengers/{id}
 */
export function getPassenger(id) {
    return http.get(`/api/user/passengers/${id}`)
}

/**
 * 分页查询常用乘客
 * GET /api/user/passengers?keyword=&page=&size=
 */
export function listPassengers({ keyword = '', page = 1, size = 10 } = {}) {
    return http.get('/api/user/passengers', { params: { keyword, page, size } })
}
