// src/api/http.js
import axios from 'axios'

const http = axios.create({
    timeout: 15000,
    headers: { 'Content-Type': 'application/json' }
})

// 请求拦截：自动带 token
http.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) {
        config.headers = config.headers || {}
        config.headers.Authorization = `Bearer ${token}` // 按你后端约定
    }
    return config
})

// 响应拦截：统一解包 + 401 处理
http.interceptors.response.use(
    resp => {
        const r = resp.data
        // 兼容 Result 包装 / 直接返回对象 两种风格
        if (r && (r.code === 1 || r.code === 200 || r.success === true)) {
            return r.data ?? r
        }
        // 如果后端直接返回对象（登录成功直接 {id,name,token}）
        if (r && (r.token || r.id || r.name)) return r
        // 其它情况当错误处理
        return Promise.reject(new Error(r?.msg || r?.message || '请求失败'))
    },
    error => {
        if (error?.response?.status === 401) {
            const redirect = location.pathname + location.search
            localStorage.removeItem('token')
            localStorage.removeItem('userType')
            localStorage.removeItem('userName')
            window.location.replace(`/login?redirect=${encodeURIComponent(redirect)}`)
            return
        }
        return Promise.reject(error)
    }
)

export default http
