// src/store/user.js
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
    state: () => ({
        id: null,
        name: '',
        type: 0,     // 1=admin, 2=user
        token: ''
    }),
    actions: {
        setAuth({ id, name, type, token }) {
            this.id = id; this.name = name; this.type = type; this.token = token
            localStorage.setItem('token', token)
            localStorage.setItem('userType', String(type || 0))
            localStorage.setItem('userName', name || '')
        },
        logout() {
            this.$reset()
            localStorage.removeItem('token')
            localStorage.removeItem('userType')
            localStorage.removeItem('userName')
        }
    }
})
