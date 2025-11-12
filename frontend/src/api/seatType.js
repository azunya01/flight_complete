import http from '@/api/http'

// 列表（一次取全）
export function listSeatTypes() {
    return http.get('/api/admin/seatType/list')
}

// 详情（如需）
export function getSeatType(id) {
    return http.get(`/api/admin/seatType/${id}`)
}

// 新增
export function addSeatType(payload) {
    // payload: { name, code }
    return http.post('/api/admin/seatType/add', payload)
}

// 修改
export function updateSeatType(payload) {
    // payload: { id, name, code }
    return http.post('/api/admin/seatType/update', payload)
}

// 删除
export function deleteSeatType(id) {
    return http.get(`/api/admin/seatType/delete/${id}`)
}

// 拉取可选舱位类型（用于下拉）
export async function fetchSeatTypeOptions() {
    // 假设接口：/api/admin/seatType/list  → SeatType[]（至少有 name 字段）
    const list = await http.get('/api/admin/seatType/list')
    // 返回 Element Plus 选项数据
    return (Array.isArray(list) ? list : []).map(st => ({
        label: st.name,
        value: st.name     // 你的 DTO 用 seatTypeName，所以直接用 name 作为 value
    }))
}
