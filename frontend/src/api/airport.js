import http from '@/api/http'

// 列表（服务端分页）
export function listAirports(page = 1, size = 10) {
    return http.get('/api/admin/airport/list', { params: { page, size } })
    // 预期返回 PageResult：{ records, total, page, size }
}

// 搜索（name/city/address 任意组合；后端返回 List）→ 前端本地分页
export function searchAirports(params = {}) {
    // params: { name?, city?, address?, page?, size? } 传不传 page/size 都行
    return http.get('/api/admin/airports', { params })
}

// 新增
export function addAirport(payload) {
    // payload: { name, city, address, tel? }
    return http.post('/api/admin/airport/add', payload)
}

// 修改
export function updateAirport(payload) {
    // payload: { id, name, city, address, tel? }
    return http.post('/api/admin/airport/update', payload)
}

// 删除
export function deleteAirport(id) {
    return http.get(`/api/admin/airport/delete/${id}`)
}
import request from '@/api/http.js'

// 拉全量机场做下拉（后端有分页，这里取 size 大一点；也可让后端做 options 接口）
export function listAirportOptions({ page = 1, size = 1000 } = {}) {
    return request({
        url: '/admin/airport/list',
        method: 'get',
        params: { page, size }
    }).then(res => res.data) // { total, records }
}

// 作为下拉选项：一次性拉全（后端返回 List<Airport>）
export async function fetchAirportOptions() {
    const list = await http.get('/api/admin/airports', { params: { page: 1, size: 1000 } })
    // list: Airport[]  ->  转为 [{label, value}]
    return (Array.isArray(list) ? list : []).map(a => ({
        label: a.name || `${a.city ?? ''} ${a.address ?? ''}`.trim(),
        value: a.name   // 你实体只有 name/city/address/tel，没有 code，这里用 name 作为 value
    }))
}
