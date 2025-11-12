import http from '@/api/http'

// 全部（分页）
export function pagePlanes(page = 1, size = 10) {
    return http.get('/api/admin/plane/list', { params: { page, size } })
    // 期望返回 PageResult: { total, page, size, records: Plane[] }
}

// 条件（name 可选）+ 分页
export function searchPlanes(name = '', page = 1, size = 10) {
    const params = { page, size }
    if (name) params.name = name
    return http.get('/api/admin/planes', { params })
}

// 新增
export function addPlane(payload) {
    // payload: { name }
    return http.post('/api/admin/plane/add', payload)
}

// 修改
export function updatePlane(payload) {
    // payload: { id, name }
    return http.post('/api/admin/plane/update', payload)
}

// 删除
export function deletePlane(id) {
    return http.get(`/api/admin/plane/delete/${id}`)
}
export function getPlane(id) {
    return http.get(`/api/admin/planes/detail/${id}`)
}