<template>
  <div class="wrap">
    <el-card shadow="never" class="block">
      <template #header>
        <div class="card-header">
          <span>按城市查询航班</span>
          <el-space>
            <el-button :loading="loading" @click="refresh">刷新</el-button>
          </el-space>
        </div>
      </template>

      <!-- 查询条件 -->
      <el-row :gutter="12" class="toolbar">
        <el-col :span="8">
          <el-form-item label="出发城市">
            <el-select
                v-model="query.fromCity"
                filterable remote clearable
                :remote-method="remoteCityFrom"
                :loading="citiesLoading"
                placeholder="如：北京"
                style="width:100%"
            >
              <el-option v-for="c in cityOptionsFrom" :key="c" :label="c" :value="c" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="到达城市">
            <el-select
                v-model="query.toCity"
                filterable remote clearable
                :remote-method="remoteCityTo"
                :loading="citiesLoading"
                placeholder="如：上海"
                style="width:100%"
            >
              <el-option v-for="c in cityOptionsTo" :key="c" :label="c" :value="c" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="出发日期">
            <el-date-picker
                v-model="query.date"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="选择日期"
                style="width:100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <div class="toolbar-btns">
        <el-space>
          <el-button type="primary" :loading="loading" @click="onSearch">搜索</el-button>
          <el-button @click="onReset">清空</el-button>
        </el-space>
      </div>

      <!-- 结果表格 -->
      <el-table
          :data="records"
          v-loading="loading"
          border
          style="width:100%; margin-top:12px;"
          :header-cell-style="{ fontWeight: 600 }"
          :empty-text="searched ? '没有合适的航班' : '请先输入城市与日期进行查询'"
      >
        <el-table-column prop="name" label="航班号" min-width="120" />
        <el-table-column prop="departureAirportName" label="出发机场" min-width="160" show-overflow-tooltip />
        <el-table-column prop="arrivalAirportName" label="到达机场" min-width="160" show-overflow-tooltip />
        <el-table-column prop="departureTime" label="起飞时间" min-width="160" />
        <el-table-column prop="arrivalTime" label="到达时间" min-width="160" />

        <!-- 新增：飞行时长（前端计算） -->
        <el-table-column label="时长" min-width="110">
          <template #default="{ row }">
            {{ formatDuration(row.departureTime, row.arrivalTime) }}
          </template>
        </el-table-column>

        <!-- 新增：最低价 -->
        <el-table-column prop="leastPrice" label="最低价" min-width="110">
          <template #default="{ row }">
            {{ formatPrice(row.leastPrice) }}
          </template>
        </el-table-column>

        <!-- 新增：剩余票数 -->
        <el-table-column prop="seatNumber" label="剩余票数" width="100">
          <template #default="{ row }">
            {{ row.seatNumber ?? '-' }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="large" @click="goBook(row)">订票</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空态 -->
      <el-empty
          v-if="searched && !loading && total === 0"
          description="没有合适的航班"
      />

      <!-- 分页 -->
      <div class="pager" v-if="total > 0">
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

    <!-- 详情弹窗：补充展示最低价/剩余票数/时长 -->
    <el-dialog v-model="detail.visible" title="航班详情" width="720px" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ detail.data?.id }}</el-descriptions-item>
        <el-descriptions-item label="航班号">{{ detail.data?.name }}</el-descriptions-item>
        <el-descriptions-item label="出发机场">{{ detail.data?.departureAirportName }}</el-descriptions-item>
        <el-descriptions-item label="到达机场">{{ detail.data?.arrivalAirportName }}</el-descriptions-item>
        <el-descriptions-item label="起飞时间">{{ detail.data?.departureTime }}</el-descriptions-item>
        <el-descriptions-item label="到达时间">{{ detail.data?.arrivalTime }}</el-descriptions-item>
        <el-descriptions-item label="时长">
          {{ formatDuration(detail.data?.departureTime, detail.data?.arrivalTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="最低价">
          {{ formatPrice(detail.data?.leastPrice) }}
        </el-descriptions-item>
        <el-descriptions-item label="剩余票数">
          {{ detail.data?.seatNumber ?? '-' }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-space>
          <el-button @click="detail.visible=false">关闭</el-button>
          <el-button type="primary" @click="goBook(detail.data)">去订票</el-button>
        </el-space>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listCities, searchFlights } from '@/api/useFlight.js'

const router = useRouter()

/** 查询状态 */
const loading = ref(false)
const searched = ref(false)
const query = reactive({ fromCity: '', toCity: '', date: '' })
const page = reactive({ current: 1, size: 10 })
const records = ref([])
const total = ref(0)

/** 远程城市 */
const citiesLoading = ref(false)
const cityOptionsFrom = ref([])
const cityOptionsTo = ref([])

async function remoteCityFrom(keyword) {
  citiesLoading.value = true
  try {
    const arr = await listCities(keyword)
    cityOptionsFrom.value = Array.isArray(arr) ? arr : []
  } finally { citiesLoading.value = false }
}
async function remoteCityTo(keyword) {
  citiesLoading.value = true
  try {
    const arr = await listCities(keyword)
    cityOptionsTo.value = Array.isArray(arr) ? arr : []
  } finally { citiesLoading.value = false }
}

/** 拉取航班 */
async function fetchFlights(p = 1) {
  if (!query.fromCity || !query.toCity || !query.date) {
    ElMessage.warning('请完整选择出发城市、到达城市与出发日期')
    return
  }
  loading.value = true
  try {
    page.current = p
    const pr = await searchFlights({
      fromCity: query.fromCity,
      toCity: query.toCity,
      date: query.date,
      page: page.current,
      size: page.size
    })
    // 期待后端返回 { total, page, size, records: UserFlightVO[] }
    records.value = pr?.records ?? []
    total.value = Number(pr?.total ?? 0)
  } catch (e) {
    ElMessage.error(e?.message || '查询失败')
  } finally {
    searched.value = true
    loading.value = false
  }
}

function onSearch() { fetchFlights(1) }
function onReset() {
  query.fromCity = ''
  query.toCity = ''
  query.date = ''
  records.value = []
  total.value = 0
  page.current = 1
  searched.value = false
}
function refresh() { if (searched.value) fetchFlights(page.current) }
function onPageChange(p) { fetchFlights(p) }

/** 详情 & 订票 */
const detail = reactive({ visible: false, data: null })
function viewDetail(row) { detail.data = row; detail.visible = true }
function goBook(row) {
  router.push({ name: 'UserBooking', query: { flightId: row?.id } })
}

/** 工具：价格与时长格式化 */
function formatPrice(v) {
  if (v == null) return '-'
  const n = typeof v === 'string' ? Number(v) : v
  if (!isFinite(n)) return String(v)
  return '￥' + n.toFixed(2)
}
function formatDuration(departure, arrival) {
  if (!departure || !arrival) return '-'
  // 兼容 Safari：把 'YYYY-MM-DD HH:mm:ss' 转成 'YYYY/MM/DD HH:mm:ss'
  const d = new Date(departure.replace(/-/g, '/'))
  const a = new Date(arrival.replace(/-/g, '/'))
  const ms = a.getTime() - d.getTime()
  if (!isFinite(ms) || ms < 0) return '-'
  const mins = Math.floor(ms / 60000)
  const h = Math.floor(mins / 60)
  const m = mins % 60
  return `${h}小时${m}分`
}
</script>

<style scoped>
.wrap { padding: 16px; }
.block { border-radius: 14px; }
.card-header { display:flex; align-items:center; justify-content:space-between; }
.toolbar { margin-bottom: 8px; }
.toolbar-btns { display:flex; justify-content:flex-end; }
.pager { display:flex; justify-content:flex-end; margin-top:12px; }
</style>
