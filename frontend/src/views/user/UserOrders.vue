<template>
  <div class="wrap">
    <el-card shadow="never" class="block">
      <template #header>
        <div class="card-header">
          <span>我的订单</span>
          <el-space>
            <el-button :loading="loading" @click="fetchOrders()">刷新</el-button>
          </el-space>
        </div>
      </template>

      <el-table
          :data="records"
          v-loading="loading"
          border
          style="width:100%"
          :header-cell-style="{ fontWeight: 600 }"
          row-key="orderId"
      >
        <el-table-column prop="orderId" label="订单号" min-width="160" />
        <el-table-column prop="flightName" label="航班" min-width="120" />
        <el-table-column label="航线" min-width="220">
          <template #default="{ row }">
            {{ row.from_airport }} → {{ row.to_airport }}
          </template>
        </el-table-column>

        <!-- 新增：起飞/到达时间与飞行时长 -->
        <el-table-column prop="departureTime" label="起飞时间" min-width="180">
          <template #default="{ row }">{{ formatDateTime(row.departureTime) }}</template>
        </el-table-column>
        <el-table-column prop="arrivalTime" label="到达时间" min-width="180">
          <template #default="{ row }">{{ formatDateTime(row.arrivalTime) }}</template>
        </el-table-column>
        <el-table-column label="飞行时长" min-width="120">
          <template #default="{ row }">
            {{ flightDuration(row.departureTime, row.arrivalTime) }}
          </template>
        </el-table-column>

        <el-table-column label="舱位" width="120">
          <template #default="{ row }">
            <el-tag>{{ seatTypeName(row.seatTypeName) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="120">
          <template #default="{ row }">{{ money(row.price) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ orderStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column type="expand">
          <template #default="{ row }">
            <el-card shadow="never" class="passenger-card">
              <el-table :data="row.passengers || []" border>
                <el-table-column prop="name" label="姓名" />
                <el-table-column prop="gender" label="性别" width="100" />
                <el-table-column prop="idNo" label="证件号" min-width="220" />
                <el-table-column prop="phone" label="手机号" min-width="160" />
              </el-table>
            </el-card>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button
                v-if="canCancel(row.status)"
                size="small"
                type="danger"
                @click="onCancel(row)"
                :loading="cancelingId === row.orderId"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && total === 0" class="empty">
        暂无订单
      </div>

      <div class="pager" v-else>
        <el-pagination
            background
            layout="total, ->, prev, pager, next, jumper"
            :total="total"
            :current-page="page.current"
            :page-size="page.size"
            @current-change="onPageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listOrders, cancelOrder, orderStatusText, money, formatDateTime } from '@/api/orders'
import http from '@/api/http'

// 列表/分页
const loading = ref(false)
const cancelingId = ref(null)
const records = ref([])
const total = ref(0)
const page = reactive({ current: 1, size: 10 })

async function fetchOrders(p = page.current) {
  loading.value = true
  try {
    page.current = p
    const pr = await listOrders({ page: page.current, size: page.size })
    records.value = pr?.records ?? []
    total.value = Number(pr?.total ?? 0)
  } catch (e) {
    ElMessage.error(e?.message || '加载订单失败')
  } finally {
    loading.value = false
  }
}
function onPageChange(p) { fetchOrders(p) }

// 取消订单（示例：仅 0待支付/1已支付 可取消）
function canCancel(status) { return status === 0 || status === 1 }
function statusTagType(status) {
  return { 0: 'warning', 1: 'success', 2: 'info', 3: 'success' }[status] || 'info'
}
async function onCancel(row) {
  try {
    await ElMessageBox.confirm(`确定取消订单 ${row.orderId} ?`, '取消确认', { type: 'warning' })
  } catch { return }
  cancelingId.value = row.orderId
  try {
    await cancelOrder(row.orderId)
    ElMessage.success('已取消')
    await fetchOrders()
  } catch (e) {
    ElMessage.error(e?.message || '取消失败')
  } finally {
    cancelingId.value = null
  }
}

// 舱位 id -> 名称（可选：如果没有选项接口，则显示 #id）
const seatTypeDict = ref(new Map())
function seatTypeName(id) {
  return seatTypeDict.value.get(id) || `#${id}`
}
async function ensureSeatTypeDict() {
  const endpoints = [
    '/api/options/seatTypes',
    '/api/seatType/options',
    '/admin/seatType/options',
    '/api/seatTypes'
  ]
  for (const url of endpoints) {
    try {
      const list = await http.get(url) // 期望 [{label, value}]
      if (Array.isArray(list) && list.length) {
        const m = new Map()
        list.forEach(o => m.set(o.value, o.label))
        seatTypeDict.value = m
        break
      }
    } catch {}
  }
}

/** 计算飞行时长（兼容字符串 "yyyy-MM-dd HH:mm:ss" 或 Date/时间数组） */
function toDate(x) {
  if (!x) return null
  if (x instanceof Date) return x
  if (Array.isArray(x)) {
    // 形如 [2025,11,12,1,33,8]
    const [Y, M, D, h=0, m=0, s=0] = x
    return new Date(Y, (M || 1) - 1, D || 1, h, m, s)
  }
  if (typeof x === 'string') return new Date(x.replace(/-/g, '/'))
  return new Date(x)
}
function flightDuration(start, end) {
  const s = toDate(start), e = toDate(end)
  if (!s || !e) return '-'
  const ms = e - s
  if (!Number.isFinite(ms) || ms <= 0) return '-'
  const h = Math.floor(ms / 3600000)
  const m = Math.round((ms % 3600000) / 60000)
  return `${h}小时${m}分`
}

onMounted(async () => {
  await ensureSeatTypeDict()
  await fetchOrders(1)
})
</script>

<style scoped>
.wrap { max-width: 1100px; margin: 16px auto; padding: 0 12px; }
.block { border-radius: 14px; }
.card-header { display:flex; align-items:center; justify-content:space-between; }
.passenger-card { margin: 8px 0; }
.pager { display:flex; justify-content:flex-end; margin-top:12px; }
.empty { color:#999; text-align:center; padding: 18px 0; }
</style>
