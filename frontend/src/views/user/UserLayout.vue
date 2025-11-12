<!-- src/views/user/UserLayout.vue -->
<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="brand" @click="$router.push('/user')">
        <strong>飞行助手</strong>
        <span class="tag">用户端</span>
      </div>

      <el-menu
          class="menu"
          mode="horizontal"
          :default-active="activePath"
          @select="onSelect"
          router
      >
        <el-menu-item index="/user/search">查询航班</el-menu-item>
        <el-menu-item index="/user/booking">订票</el-menu-item>
        <el-menu-item index="/user/orders">我的订单</el-menu-item>
        <el-menu-item index="/user/passengers">常用乘客</el-menu-item>
      </el-menu>

      <div class="right">
        <el-space>
          <span class="user">你好，{{ userName }}</span>
          <el-dropdown>
            <span class="el-dropdown-link">
              操作
              <span style="display:inline-block; transform: translateY(1px);">▾</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click.native="toSearch">查询航班</el-dropdown-item>
                <el-dropdown-item @click.native="toOrders">我的订单</el-dropdown-item>
                <el-dropdown-item @click.native="toPassengers">常用乘客</el-dropdown-item>
                <el-dropdown-item divided @click.native="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-space>
      </div>
    </el-header>

    <el-main class="main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// 直接用当前路径，激活态更准确
const activePath = computed(() => route.path || '/user/search')
const userName = computed(() => localStorage.getItem('userName') || '用户')

function onSelect(index) {
  router.push(index)
}
function toSearch() { router.push('/user/search') }
function toOrders() { router.push('/user/orders') }
function toPassengers() { router.push('/user/passengers') }
function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('userType')
  localStorage.removeItem('userName')
  router.replace('/login')
}
</script>

<style scoped>
.layout { height: 100vh; }
.header {
  display: flex; align-items: center;
  padding: 0 16px; border-bottom: 1px solid #eee;
  gap: 16px;
}
.brand { display:flex; align-items:center; gap:8px; cursor:pointer; }
.brand .tag {
  font-size: 12px; color:#409eff; border:1px solid #409eff;
  border-radius: 999px; padding: 1px 8px;
}
.menu { flex: 1; border-bottom: none; }
.right { display:flex; align-items:center; }
.user { color:#555; }
.main { background: #f7f8fa; padding: 16px; }
</style>
