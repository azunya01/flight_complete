// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'

// Admin 相关（已存在）
import AdminAirlineList from "@/views/admin/AdminAirlineList.vue";
import AdminAirportList from "@/views/admin/AdminAirportList.vue";
import AdminPlaneList from "@/views/admin/AdminPlaneList.vue";
import AdminSeatTypeList from "@/views/admin/AdminSeatTypeList.vue";
import AdminFlightList from "@/views/admin/AdminFlightList.vue";
import Booking from "@/views/user/Booking.vue";
import UserPassenger from "@/views/user/UserPassenger.vue";

const Login = () => import('@/pages/Login.vue')
const Register = () => import('@/pages/Register.vue')

// Admin 布局
const AdminLayout = () => import('@/views/admin/AdminLayout.vue')
const AdminDashboard = () => import('@/views/admin/AdminDashboard.vue')
const AdminAirlineAdd = () => import('@/views/admin/AdminAirlineAdd.vue')

// User 布局 + 子页
const UserLayout = () => import('@/views/user/UserLayout.vue')
const UserSearchFlights = () => import('@/views/user/SearchFlights.vue')
const UserOrders = () => import('@/views/user/UserOrders.vue')
const UserBooking = () => import('@/views/user/Booking.vue')

const router = createRouter({
    history: createWebHistory(),
    routes: [
        { path: '/', redirect: '/login' },

        { path: '/login', name: 'Login', component: Login, meta: { public: true } },
        { path: '/register', name: 'Register', component: Register, meta: { public: true } },

        // 用户端：顶栏菜单 + 子页面
        {
            path: '/user',
            component: UserLayout,
            meta: { requiresAuth: true },          // 普通用户 & 管理员都可访问（一般给用户用）
            children: [
                { path: '', redirect: '/user/search' },
                { path: 'search', name: 'UserSearch', component: UserSearchFlights, meta: { title: '查询航班' } },
                { path: 'booking', name: 'UserBooking', component: UserBooking, meta: { title: '订票' } },
                { path: 'orders', name: 'UserOrders', component: UserOrders, meta: { title: '我的订单' } } ,// ★ 新增
                { path: 'book', name: 'UserBooking', component: Booking, meta: { title: '在线订票' } },
                { path: 'passengers', name: 'UserPassengers', component: UserPassenger, meta: { title: '常用乘客' } },
            ]
        },

        // 管理后台：壳 + 子路由
        {
            path: '/admin',
            component: AdminLayout,
            meta: { requiresAuth: true, adminOnly: true },
            children: [
                { path: '', component: AdminDashboard, meta: { title: '首页' } },
                { path: 'airline/add', component: AdminAirlineAdd, meta: { title: '航线新增' } },
                { path: 'airline/list', component: AdminAirlineList, meta: { title: '航线' } },
                { path: 'airport/list', component: AdminAirportList, meta: { title: '机场' } },
                { path: 'plane/list', component: AdminPlaneList, meta: { title: '飞机' } },
                { path: 'seatType/list', component: AdminSeatTypeList, meta: { title: '舱位类型' } },
                { path: 'flight/list', component: AdminFlightList, meta: { title: '航班' } }
            ]
        },

        { path: '/:pathMatch(.*)*', redirect: '/login' }
    ]
})

// 全局守卫：区分 admin / user 登录落点
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    const userType = Number(localStorage.getItem('userType') || 0) // 1=admin, 2=user

    // 已登录访问登录/注册：优先 redirect，否则进各自首页
    if ((to.path === '/login' || to.path === '/register') && token) {
        const target =
            (to.query.redirect && String(to.query.redirect)) ||
            (userType === 1 ? '/admin' : '/user')
        if (to.query.redirect) return next(target)
        return next(target)
    }

    // 需要登录
    if (to.meta?.requiresAuth || to.meta?.adminOnly) {
        if (!token) {
            return next({ path: '/login', query: { redirect: to.fullPath } })
        }
        if (to.meta?.adminOnly && userType !== 1) {
            return next('/user')
        }
    }

    next()
})

export default router
