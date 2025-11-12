<!-- src/pages/Home.vue -->
<template>
  <div class="wrap">
    <!-- 顶部欢迎区 -->
    <el-card shadow="never" class="hero">
      <div class="hero-left">
        <h2 class="title">欢迎回来，{{ userName }}！</h2>
        <p class="sub">在这里可以快速搜索并预订合适的航班。</p>
        <div class="actions">
          <el-button type="primary" @click="$refs.searchBlock?.scrollIntoView({behavior:'smooth'})">
            立即查询航班
          </el-button>
          <el-button @click="logout">退出登录</el-button>
        </div>
      </div>
      <div class="hero-right">
        <el-statistic title="今日可选航班(示例)" :value="quickStat" />
      </div>
    </el-card>

    <!-- 查询区 -->
    <el-card shadow="never" class="block" ref="searchBlock">
      <template #header>
        <div class="card-header">
          <span>按城市查询航班</span>
          <el-space>
            <el-button :loading="loading" @click="refresh">刷新</el-button>
          </el-space>
        </div>
      </template>

      <el-row :gutter="12" class="toolbar">
        <el-col :span="8">
          <el-form-item label="出发城市">
            <el-select
                v-model="query.fromCity"
                filterable
                remote
                clearable
                :remote-method="remoteCityFrom"
                :loading="citiesLoading"
                placeholder="请输入并选择城市，如 北京"
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
                filterable
                remote
                clearable
                :remote-method="remoteCityTo"
                :loading="citiesLoading"
                placeholder="请输入并选择城市，如 上海"
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

      <!-- 结果表 -->
      <el-table
          :data="records"
          v-loading="loading"
          border
          style="width:100%; margin-top:12px;"
          :header-cell-style="{ fontWeight: 600 }"
          empty-text="请先输入城市与日期进行查询"
      >
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="name" label="航班号" min-width="120" />
        <el-table-column prop="departureAirportName" label="出发机场" min-width="180" show-overflow-tooltip />
        <el-table-column prop="arrivalAirportName" label="到达机场" min-width="180" show-overflow-tooltip />
        <el-table-column prop="departureTime" label="起飞时间" min-width="180" />
        <el-table-column prop="arrivalTime" label="到达时间" min-width="180" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" text @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager" v-if="total>0">
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

    <!-- 详情弹窗（可按需替换为真正的用户详情页） -->
    <el-dialog v-model="detail.visible" title="航班详情" width="700px" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ detail.data?.id }}</el-descriptions-item>
        <el-descriptions-item label="航班号">{{ detail.data?.name }}</el-descriptions-item>
        <el-descriptions-item label="出发机场">{{ detail.data?.departureAirportName }}</el-descriptions-item>
        <el-descriptions-item label="到达机场">{{ detail.data?.arrivalAirportName }}</el-descriptions-item>
        <el-descriptions-item label="起飞时间">{{ detail.data?.departureTime }}</el-descriptions-item>
        <el-descriptions-item label="到达时间">{{ detail.data?.arrivalTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-space>
          <el-button @click="detail.visible=false">关闭</el-button>
          <el-button type="primary" @click="toBook(detail.data)">去预订（占位）</el-button>
        </el-space>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '@/api/http'

// 欢迎区
const router = useRouter()
const userName = computed(() => localStorage.getItem('userName') || '用户')
const quickStat = 128 // 示例统计数

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('userType')
  localStorage.removeItem('userName')
  router.replace('/login')
}

// 查询相关状态
const loading = ref(false)
const query = reactive({
  fromCity: '',
  toCity: '',
  date: ''
})

const page = reactive({ current: 1, size: 10 })
const records = ref([])
const total = ref(0)

// 远程城市选择
const citiesLoading = ref(false)
const cityOptionsFrom = ref([])
const cityOptionsTo = ref([])

async function fetchCities(keyword = '') {
  // GET /api/flights/cities?keyword=xxx
  return await http.get('/api/flights/cities', { params: { keyword } })
}

async function remoteCityFrom(keyword) {
  citiesLoading.value = true
  try {
    cityOptionsFrom.value = await fetchCities(keyword)
  } finally {
    citiesLoading.value = false
  }
}
async function remoteCityTo(keyword) {
  citiesLoading.value = true
  try {
    cityOptionsTo.value = await fetchCities(keyword)
  } finally {
    citiesLoading.value = false
  }
}

// 查询航班
async function fetchFlights(p = 1) {
  if (!query.fromCity || !query.toCity || !query.date) {
    ElMessage.warning('请完整选择出发城市、到达城市与出发日期')
    return
  }
  loading.value = true
  try {
    page.current = p
    // GET /api/flights/search?fromCity=...&toCity=...&date=yyyy-MM-dd&page=1&size=10
    const pr = await http.get('/api/flights/search', {
      params: {
        fromCity: query.fromCity,
        toCity: query.toCity,
        date: query.date,
        page: page.current,
        size: page.size
      }
    })
    records.value = pr?.records ?? []
    total.value = Number(pr?.total ?? 0)
  } catch (e) {
    ElMessage.error(e?.message || '查询失败')
  } finally {
    loading.value = false
  }
}

function onSearch() {
  fetchFlights(1)
}
function refresh() {
  if (total.value === 0) return
  fetchFlights(page.current)
}
function onReset() {
  query.fromCity = ''
  query.toCity = ''
  query.date = ''
  records.value = []
  total.value = 0
  page.current = 1
}
function onPageChange(p) {
  fetchFlights(p)
}

// 详情
const detail = reactive({ visible: false, data: null })
function viewDetail(row) {
  detail.data = row
  detail.visible = true
}
function toBook(row) {
  ElMessage.info(`预订流程待接入（航班 #${row?.id || '-'}）`)
}
</script>

<style scoped>
.wrap { padding: 16px; }
.hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px;
  margin-bottom: 12px;
  border-radius: 14px;
}
.hero-left .title { margin: 0 0 6px; font-size: 20px; font-weight: 700; }
.hero-left .sub { margin: 0 0 12px; color: #666; }
.actions { display: flex; gap: 10px; }
.hero-right { min-width: 220px; display: flex; justify-content: flex-end; }
.block { margin-top: 12px; border-radius: 14px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.toolbar { margin-bottom: 8px; }
.toolbar-btns { display:flex; justify-content:flex-end; }
.pager { display:flex; justify-content:flex-end; margin-top:12px; }
</style>
