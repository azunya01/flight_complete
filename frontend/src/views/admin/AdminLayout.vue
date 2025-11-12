<template>
  <el-container class="fullscreen">
    <el-aside :width="collapsed ? '64px' : '220px'" class="aside">
      <div class="brand" @click="$router.push('/admin')">
        <el-icon><Promotion /></el-icon>
        <span v-if="!collapsed" class="brand-text">飞行后台</span>
      </div>

      <el-menu :default-active="activeMenu" :collapse="collapsed" router class="menu">
        <el-menu-item index="/admin"><el-icon><House /></el-icon><span>首页</span></el-menu-item>
        <el-menu-item index="/admin/airline/list">
          <el-icon><MapLocation /></el-icon><span>航线</span>
        </el-menu-item>
        <el-menu-item index="/admin/airport/list">
          <el-icon><OfficeBuilding /></el-icon><span>机场</span>
        </el-menu-item>
        <el-menu-item index="/admin/plane/list">
          <el-icon><Tickets /></el-icon><span>飞机</span>
        </el-menu-item>
        <!-- AdminLayout.vue 内的菜单 -->
        <el-menu-item index="/admin/seatType/list">
          <el-icon><Collection /></el-icon><span>舱位类型</span>
        </el-menu-item>
        <el-menu-item index="/admin/flight/list">
          <el-icon><Tickets /></el-icon>
          <span>航班</span>
        </el-menu-item>
      </el-menu>

      <div class="collapse" @click="collapsed = !collapsed" :title="collapsed ? '展开' : '收起'">
        <el-icon v-if="collapsed"><ArrowRight /></el-icon>
        <el-icon v-else><ArrowLeft /></el-icon>
      </div>
    </el-aside>

    <el-container>
      <el-header class="topbar">
        <div class="left">
          <el-button class="only-mobile" text @click="collapsed = !collapsed"><el-icon><Menu /></el-icon></el-button>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item to="/admin">后台</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="right">
          <el-tag type="success" effect="light">管理员</el-tag>
          <el-dropdown>
            <span class="user">
              <el-icon><UserFilled /></el-icon>{{ userName }}
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="copyToken">复制 Token</el-dropdown-item>
                <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import {createRouter as $router, useRoute, useRouter} from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft,
  ArrowRight, Collection,
  House,
  MapLocation, Menu, OfficeBuilding,
  Promotion,
  Setting,
  Ship,
  TakeawayBox,
  Ticket, Tickets,
  User, UserFilled
} from "@element-plus/icons-vue";

const router = useRouter()
const route  = useRoute()
const store  = useUserStore()

const collapsed = ref(window.innerWidth <= 900)
window.addEventListener('resize', () => { if (window.innerWidth <= 900) collapsed.value = true })

const userName = computed(() => store.name || localStorage.getItem('userName') || '管理员')
const userType = computed(() => Number(store.type ?? localStorage.getItem('userType') ?? 1))
const token    = computed(() => store.token || localStorage.getItem('token') || '')

const activeMenu = computed(() => route.matched.at(-1)?.path || route.path)
const titleMap = {
  '/admin': '首页',
  '/admin/airline/add': '航线',
  '/admin/flight/list': '航班',
  '/admin/cabin/list': '客舱',
  '/admin/order/list': '订单',
  '/admin/user/list': '用户',
  '/admin/settings': '配置',
  '/admin/stats': '统计'
}
const currentTitle = computed(() => titleMap[route.matched.at(-1)?.path || route.path] || '首页')

function copyToken() {
  if (!token.value) return
  navigator.clipboard?.writeText(token.value)
      .then(() => ElMessage.success('Token 已复制'))
      .catch(() => ElMessage.error('复制失败'))
}

function logout() {
  store.logout()
  router.replace('/login')
}

onMounted(() => {
  if (userType.value !== 1) router.replace('/home')
})
</script>

<style scoped>
.fullscreen { min-height: 100vh; }
.aside { position: relative; border-right: 1px solid var(--el-border-color-lighter); background: var(--el-bg-color-overlay); }
.brand { height: 56px; display: flex; align-items: center; gap: 8px; padding: 0 12px; border-bottom: 1px solid var(--el-border-color-lighter); cursor: pointer; font-weight: 700; }
.brand-text { white-space: nowrap; }
.menu { border-right: none; }
.collapse { position: absolute; bottom: 12px; right: 12px; width: 28px; height: 28px; display: grid; place-items: center; border-radius: 6px; background: var(--el-fill-color); cursor: pointer; color: var(--el-text-color-regular); }
.topbar { display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid var(--el-border-color-lighter); background: var(--el-bg-color-overlay); }
.left { display: flex; align-items: center; gap: 12px; }
.right { display: flex; align-items: center; gap: 12px; }
.user { display: inline-flex; align-items: center; gap: 6px; cursor: pointer; }
.only-mobile { display: none; }
.main { background: var(--el-bg-color); }
@media (max-width: 900px) { .only-mobile { display: inline-flex; } }
</style>
