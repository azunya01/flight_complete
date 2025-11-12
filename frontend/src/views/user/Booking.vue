<!-- src/views/user/UserBooking.vue -->
<template>
  <div class="wrap">
    <!-- 航班信息 -->
    <el-card shadow="never" class="block">
      <template #header>
        <div class="card-header">
          <span>航班详情与订票</span>
          <el-space>
            <el-button :loading="loading" @click="reload">刷新</el-button>
          </el-space>
        </div>
      </template>

      <el-skeleton :loading="loading" animated :rows="4">
        <template #default>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="航班号">{{ detail?.name || '-' }}</el-descriptions-item>
            <el-descriptions-item label="飞行时长">
              {{ formatDuration(detail?.departureTime, detail?.arrivalTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="出发机场">{{ detail?.departureAirportName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="到达机场">{{ detail?.arrivalAirportName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="起飞时间">{{ detail?.departureTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="到达时间">{{ detail?.arrivalTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="最低价">{{ formatPrice(detail?.leastPrice) }}</el-descriptions-item>
          </el-descriptions>
        </template>
      </el-skeleton>
    </el-card>

    <!-- 舱位选择（不展示ID，内部绑定 seatTypeId） -->
    <el-card shadow="never" class="block">
      <template #header>
        <div class="card-header">
          <span>选择舱位与价格</span>
          <el-tag v-if="!seatRows.length" type="info">该航班暂无可售舱位</el-tag>
        </div>
      </template>

      <el-table
          :data="seatRows"
          border
          style="width:100%"
          :header-cell-style="{ fontWeight: 600 }"
          empty-text="该航班暂无可售舱位"
      >
        <el-table-column label="选择" width="90">
          <template #default="{ row }">
            <el-radio
                :label="row.seatTypeId"
                v-model="form.seatTypeId"
                :disabled="row.available === 0"
            />
          </template>
        </el-table-column>
        <el-table-column prop="seatTypeName" label="舱位" min-width="140" />
        <el-table-column prop="price" label="价格" min-width="140">
          <template #default="{ row }">{{ formatPrice(row.price) }}</template>
        </el-table-column>
        <el-table-column prop="available" label="可售" width="100">
          <template #default="{ row }">
            <el-tag :type="row.available === 0 ? 'info' : 'success'">
              {{ row.available === 0 ? '无' : '有' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="total" label="总座位" width="100">
          <template #default="{ row }">{{ row.total ?? '-' }}</template>
        </el-table-column>
      </el-table>

      <div class="tip" v-if="seatRows.length">
        <el-text type="info">请选择一个舱位继续填写乘客信息。</el-text>
      </div>
    </el-card>

    <!-- 乘客信息（支持远程选择常用乘客） -->
    <el-card shadow="never" class="block">
      <template #header>
        <div class="card-header">
          <span>乘客信息</span>
          <el-space>
            <el-button type="primary" plain @click="addPassenger">添加乘客</el-button>
          </el-space>
        </div>
      </template>

      <el-table :data="form.passengers" border style="width:100%">
        <el-table-column label="#" width="60">
          <template #default="{ $index }">{{ $index + 1 }}</template>
        </el-table-column>

        <el-table-column label="选择常用乘客" min-width="260">
          <template #default="{ row, $index }">
            <el-select
                v-model="row._selectedId"
                filterable
                remote
                clearable
                placeholder="搜索姓名/证件号/手机号"
                :remote-method="(kw) => remotePassengers(kw, $index)"
                :loading="passengerLoading[$index]"
                style="width:100%"
                @change="(val) => onPickPassenger(val, $index)"
            >
              <el-option
                  v-for="opt in passengerOptions[$index] || []"
                  :key="opt.id"
                  :label="formatPassengerLabel(opt)"
                  :value="opt.id"
              />
            </el-select>
          </template>
        </el-table-column>

        <el-table-column label="姓名" min-width="140">
          <template #default="{ row }">
            <el-input v-model.trim="row.name" placeholder="真实姓名" />
          </template>
        </el-table-column>

        <el-table-column label="性别" width="120">
          <template #default="{ row }">
            <el-select v-model="row.gender" placeholder="性别">
              <el-option label="男" value="男" />
              <el-option label="女" value="女" />
            </el-select>
          </template>
        </el-table-column>

        <el-table-column label="证件号" min-width="200">
          <template #default="{ row }">
            <el-input v-model.trim="row.idNo" placeholder="身份证号/护照号" />
          </template>
        </el-table-column>

        <el-table-column label="手机号" min-width="160">
          <template #default="{ row }">
            <el-input v-model.trim="row.phone" placeholder="联系电话" />
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120">
          <template #default="{ $index }">
            <el-button type="danger" text @click="removePassenger($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="tip">
        <el-text type="info">可从常用乘客下拉选择并自动填充，也可手动编辑。至少添加一位乘客。</el-text>
      </div>
    </el-card>

    <!-- 提交 -->
    <div class="submit-bar">
      <el-space>
        <el-button @click="goBack">返回</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">下单并支付</el-button>
      </el-space>
    </div>

    <!-- 下单成功 -->
    <el-dialog v-model="order.visible" title="下单成功" width="520px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="订单号">{{ order.data?.orderId }}</el-descriptions-item>
        <el-descriptions-item label="航班">{{ order.data?.flightName }}</el-descriptions-item>
        <el-descriptions-item label="总价">{{ formatPrice(order.data?.totalPrice) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ order.data?.createTime }}</el-descriptions-item>
      </el-descriptions>
      <el-card shadow="never" header="乘客列表" style="margin-top:12px;">
        <el-table :data="order.data?.passengers || []" border>
          <el-table-column prop="name" label="姓名" />
          <el-table-column prop="gender" label="性别" width="100" />
          <el-table-column prop="idNo" label="证件号" />
          <el-table-column prop="phone" label="手机号" />
        </el-table>
      </el-card>
      <template #footer>
        <el-button type="primary" @click="finish">完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '@/api/http'

const route = useRoute()
const router = useRouter()

/** 航班详情 */
const loading = ref(false)
const submitting = ref(false)
const flightId = computed(() => Number(route.query.flightId || route.params.flightId))
const detail = ref(null)

/** 舱位行（优先使用后端 seatTypeId；UI 不展示 id，但 v-model 绑定该 id） */
const seatRows = computed(() => {
  const listNew = detail.value?.seatTypes
  if (Array.isArray(listNew)) {
    return listNew.map(s => ({
      seatTypeId: s.seatTypeId ?? s.id,         // ← 关键：直接用后端 id
      seatTypeName: s.seatTypeName || '舱位',
      price: s.price,
      available: s.haveAvailable ? 1 : 0,
      total: null
    }))
  }
  const listOld = detail.value?.flightSeatVOS || []
  return (Array.isArray(listOld) ? listOld : []).map(s => ({
    seatTypeId: s.seatTypeId ?? s.id,
    seatTypeName: s.seatTypeName ?? s.name ?? '舱位',
    price: s.price,
    available: s.available ?? 0,
    total: s.total ?? null
  }))
})

/** 表单（乘客行带 _selectedId 用于 select） */
const form = reactive({
  seatTypeId: null,                          // 绑定真实 seatTypeId
  passengers: [
    { _selectedId: null, name: '', gender: '', idNo: '', phone: '' }
  ]
})

/** 远程常用乘客下拉：每行独立 options/loading */
const passengerOptions = reactive({})   // { rowIndex: [ {id,name,gender,idNo,phone}, ... ] }
const passengerLoading = reactive({})   // { rowIndex: boolean }

function formatPassengerLabel(p) {
  const id = p.idNo || ''
  return `${p.name || ''}（${p.gender || '-'}｜${id}｜${p.phone || ''}）`
}

async function remotePassengers(keyword = '', rowIndex = 0) {
  passengerLoading[rowIndex] = true
  try {
    // 你的后端：GET /api/user/passengers?keyword=xx&page=1&size=10
    const pr = await http.get('/api/user/passengers', {
      params: { keyword, page: 1, size: 10 }
    })
    passengerOptions[rowIndex] = pr?.records ?? []
  } catch (e) {
    passengerOptions[rowIndex] = []
  } finally {
    passengerLoading[rowIndex] = false
  }
}

function onPickPassenger(selectedId, rowIndex) {
  const list = passengerOptions[rowIndex] || []
  const picked = list.find(x => x.id === selectedId)
  if (!picked) return
  const row = form.passengers[rowIndex]
  row.name = picked.name || ''
  row.gender = picked.gender || ''
  row.idNo = picked.idNo || ''
  row.phone = picked.phone || ''
}

/** 行增删 */
function addPassenger() {
  form.passengers.push({ _selectedId: null, name: '', gender: '', idNo: '', phone: '' })
}
function removePassenger(i) {
  if (form.passengers.length <= 1) {
    ElMessage.warning('至少需要一位乘客')
    return
  }
  form.passengers.splice(i, 1)
}

/** 拉详情（GET /api/user/book?flightId=...），并默认选中第一个有票的舱位 */
async function fetchDetail() {
  if (!flightId.value) {
    ElMessage.error('缺少 flightId')
    return
  }
  loading.value = true
  try {
    const res = await http.get('/api/user/book', { params: { flightId: flightId.value } })
    detail.value = res
    // 默认选中一个可售舱位
    const firstAvailable = seatRows.value.find(x => x.available > 0)
    form.seatTypeId = firstAvailable ? firstAvailable.seatTypeId : null
  } catch (e) {
    ElMessage.error(e?.message || '获取航班详情失败')
  } finally {
    loading.value = false
  }
}

/** 提交下单（POST /api/user/book） */
async function submit() {
  if (!flightId.value) return ElMessage.error('缺少 flightId')
  if (!form.seatTypeId) return ElMessage.warning('请先选择舱位')
  for (const [i, p] of form.passengers.entries()) {
    if (!p.name || !p.gender || !p.idNo || !p.phone) {
      ElMessage.warning(`第 ${i + 1} 位乘客信息不完整`)
      return
    }
  }
  const dto = {
    flightId: flightId.value,
    seatTypeId: Number(form.seatTypeId),      // ← 传真实 seatTypeId（确保是数字）
    passengers: form.passengers.map(p => ({
      name: p.name,
      gender: p.gender,
      idNo: p.idNo,
      phone: p.phone
    }))
  }
  submitting.value = true
  try {
    const data = await http.post('/api/user/book', dto)
    order.data = data
    order.visible = true
  } catch (e) {
    ElMessage.error(e?.message || '下单失败')
  } finally {
    submitting.value = false
  }
}

/** 其它 */
function reload() { fetchDetail() }
function goBack() { router.back() }
const order = reactive({ visible: false, data: null })
function finish() {
  order.visible = false
  // router.replace({ name: 'UserOrders' })
}

/** 工具 */
function formatPrice(v) {
  if (v == null) return '-'
  const n = typeof v === 'string' ? Number(v) : v
  if (Number.isNaN(n)) return String(v)
  return '￥' + n.toFixed(2)
}
function formatDuration(start, end) {
  if (!start || !end) return '-'
  const s = new Date(String(start).replace(/-/g, '/'))
  const e = new Date(String(end).replace(/-/g, '/'))
  const ms = e - s
  if (!Number.isFinite(ms) || ms <= 0) return '-'
  const h = Math.floor(ms / 3600000)
  const m = Math.round((ms % 3600000) / 60000)
  return `${h}小时${m}分`
}

onMounted(fetchDetail)
</script>

<style scoped>
.wrap { max-width: 1100px; margin: 16px auto; padding: 0 12px; }
.block { margin-bottom: 14px; border-radius: 14px; }
.card-header { display:flex; align-items:center; justify-content:space-between; }
.tip { margin-top: 10px; }
.submit-bar { display:flex; justify-content:flex-end; margin-top: 12px; padding-bottom: 16px; }
</style>
