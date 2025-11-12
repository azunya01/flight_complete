// src/api/orders.js
import http from '@/api/http'

// 分页查询订单：GET /api/user/orders/list?page=&size=
export function listOrders({ page = 1, size = 10 } = {}) {
    return http.get('/api/user/orders/list', { params: { page, size } })
}

// 取消订单：POST /api/user/orders/{orderId}/cancel
export function cancelOrder(orderId) {
    return http.post(`/api/user/orders/${orderId}/cancel`)
}

/** 状态文案（按你后端约定可自行调整）
 * 0=待支付, 1=已支付, 2=已取消, 3=已完成
 */
export function orderStatusText(s) {
    return ({
        0: '待支付',
        1: '已支付',
        2: '已取消',
        3: '已完成'
    })[s] ?? `状态${s}`
}

// 金额格式化
export function money(v) {
    if (v == null) return '-'
    const n = typeof v === 'string' ? Number(v) : v
    if (Number.isNaN(n)) return String(v)
    return '￥' + n.toFixed(2)
}

// createTime 可能是 ISO 字符串，也可能是 [yyyy,MM,dd,HH,mm,ss] 数组
export function formatDateTime(dt) {
    try {
        if (Array.isArray(dt)) {
            const [y, M, d, h = 0, m = 0, s = 0] = dt
            const pad = n => String(n).padStart(2, '0')
            return `${y}-${pad(M)}-${pad(d)} ${pad(h)}:${pad(m)}:${pad(s)}`
        }
        if (typeof dt === 'string') return dt.replace('T', ' ')
    } catch {}
    return String(dt ?? '-')
}
