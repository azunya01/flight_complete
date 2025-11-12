import http from '@/api/http'

// 机型选项：[{label, value}] => name / id
export async function fetchPlaneOptions() {
    const pr = await http.get('/api/admin/plane/list', { params: { page: 1, size: 1000 } })
    const list = pr?.records ?? pr ?? []
    return list.map(x => ({ label: x.name || x.planeName || `#${x.id}`, value: x.id }))
}

// 航线选项：[{label, value}] => "出发→到达" / id
export async function fetchAirlineOptions() {
    const pr = await http.get('/api/admin/airline/list', { params: { page: 1, size: 1000 } })
    const list = pr?.records ?? pr ?? []
    return list.map(x => ({
        label: `${x.fromAirport} → ${x.toAirport}`,
        value: x.id
    }))
}

// 舱位类型选项：[{label, value}] => name / id
export async function fetchSeatTypeOptions() {
    // 你实际的接口，如 /api/admin/seatType/list
    const list = await http.get('/api/admin/seatType/list')
    const arr = Array.isArray(list) ? list : (list?.records ?? [])
    return arr.map(s => ({ label: s.name, value: s.id }))
}
