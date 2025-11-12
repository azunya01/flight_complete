<!-- src/views/admin/AdminFlightList.vue -->
<template>
  <div class="wrap">
    <!-- 工具条 -->
    <el-row :gutter="12" class="toolbar">
      <el-col :span="12">
        <el-button type="primary" @click="openAdd">新增航班</el-button>
      </el-col>
      <el-col :span="12" style="text-align:right">
        <el-space>
          <el-button @click="refresh" :loading="loading">刷新</el-button>
        </el-space>
      </el-col>
    </el-row>

    <!-- 列表：只展示 7 个字段 -->
    <el-card shadow="never">
      <el-table
          :data="records"
          v-loading="loading"
          border
          style="width:100%"
          :header-cell-style="{ fontWeight: 600 }"
      >
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="name" label="航班号" min-width="120" />
        <el-table-column prop="planeName" label="机型" min-width="160" show-overflow-tooltip />
        <el-table-column prop="departureAirportName" label="出发机场" min-width="180" show-overflow-tooltip />
        <el-table-column prop="arrivalAirportName" label="到达机场" min-width="180" show-overflow-tooltip />
        <el-table-column prop="departureTime" label="起飞时间" min-width="180">
          <template #default="{ row }">{{ row.departureTime || '-' }}</template>
        </el-table-column>
        <el-table-column prop="arrivalTime" label="到达时间" min-width="180">
          <template #default="{ row }">{{ row.arrivalTime || '-' }}</template>
        </el-table-column>

        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="info" @click="openDetail(row)">详情</el-button>
            <el-button size="small" @click="openEdit(row)">修改</el-button>
            <el-button size="small" type="danger" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
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

    <!-- 新增航班 Dialog（FlightDTO） -->
    <el-dialog v-model="dlgAdd.visible" title="新增航班" width="760px" destroy-on-close>
      <el-form ref="addRef" :model="addForm" :rules="rulesAdd" label-position="top">
        <el-form-item label="航班号" prop="name">
          <el-input v-model.trim="addForm.name" placeholder="例如：MU1234" />
        </el-form-item>

        <el-form-item label="机型" prop="planeId">
          <el-select v-model="addForm.planeId" :loading="optLoading" filterable placeholder="选择机型" style="width:100%">
            <el-option v-for="p in planeOptions" :key="p.value" :label="p.label" :value="p.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="航线" prop="airlineId">
          <el-select v-model="addForm.airlineId" :loading="optLoading" filterable placeholder="选择航线（出发→到达）" style="width:100%">
            <el-option v-for="a in airlineOptions" :key="a.value" :label="a.label" :value="a.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="起飞时间" prop="departureTime">
          <el-date-picker
              v-model="addForm.departureTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="选择起飞时间"
              style="width:100%"
          />
        </el-form-item>

        <el-form-item label="到达时间" prop="arrivalTime">
          <el-date-picker
              v-model="addForm.arrivalTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="选择到达时间"
              style="width:100%"
          />
        </el-form-item>

        <!-- 舱位定价（seatPrices） -->
        <el-form-item label="舱位类型（定价）">
          <el-select
              v-model="addForm.seatTypeIds"
              multiple filterable clearable
              :loading="optLoading"
              placeholder="选择需要定价的舱位类型（可多选）"
              style="width:100%"
              @change="syncAddSeatPriceRows"
          >
            <el-option v-for="s in seatTypeOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
          <div class="hint">选择后在下方表格中逐一填写价格。</div>
        </el-form-item>

        <el-form-item v-if="addSeatPriceRows.length" label="舱位定价" required>
          <el-table :data="addSeatPriceRows" border>
            <el-table-column label="舱位" prop="seatTypeLabel" />
            <el-table-column label="价格" width="260">
              <template #default="{ row }">
                <el-input v-model="row.price" placeholder="请输入价格（数字）" />
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-space>
          <el-button @click="dlgAdd.visible=false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="onAddSubmit">提交</el-button>
        </el-space>
      </template>
    </el-dialog>

    <!-- 修改航班 Dialog（FlightUpdateDTO） -->
    <el-dialog v-model="dlgEdit.visible" title="修改航班" width="820px" destroy-on-close>
      <el-form ref="editRef" :model="editForm" :rules="rulesEdit" label-position="top">
        <el-form-item label="ID">
          <el-input :value="editForm.id" disabled />
        </el-form-item>

        <el-form-item label="航班号" prop="name">
          <el-input v-model.trim="editForm.name" />
        </el-form-item>

        <el-form-item label="机型" prop="planeId">
          <el-select v-model="editForm.planeId" :loading="optLoading" filterable placeholder="选择机型" style="width:100%">
            <el-option v-for="p in planeOptions" :key="p.value" :label="p.label" :value="p.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="航线" prop="airlineId">
          <el-select v-model="editForm.airlineId" :loading="optLoading" filterable placeholder="选择航线（出发→到达）" style="width:100%">
            <el-option v-for="a in airlineOptions" :key="a.value" :label="a.label" :value="a.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="起飞时间" prop="departureTime">
          <el-date-picker
              v-model="editForm.departureTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="选择起飞时间"
              style="width:100%"
          />
        </el-form-item>

        <el-form-item label="到达时间" prop="arrivalTime">
          <el-date-picker
              v-model="editForm.arrivalTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="选择到达时间"
              style="width:100%"
          />
        </el-form-item>

        <!-- 舱位可售与价格（flightSeatInventoryDTOS） -->
        <el-form-item label="舱位可售与价格">
          <el-table :data="editSeatRows" border>
            <el-table-column prop="seatTypeName" label="舱位" min-width="140" />
            <el-table-column label="可售(available)" width="200">
              <template #default="{ row }">
                <el-input-number v-model="row.available" :min="0" :max="999999" :step="1" controls-position="right" />
              </template>
            </el-table-column>
            <el-table-column label="价格(price)" width="220">
              <template #default="{ row }">
                <el-input v-model="row.price" placeholder="价格（数字）" />
              </template>
            </el-table-column>
            <el-table-column prop="total" label="总座位(只读)" width="120" />
          </el-table>
          <div class="hint">提示：提交时将自动映射为 flightSeatInventoryDTOS[{ seatTypeId, available, price }]</div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-space>
          <el-button @click="dlgEdit.visible=false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="onEditSubmit">保存</el-button>
        </el-space>
      </template>
    </el-dialog>

    <!-- 详情 Dialog（展示 FlightVO 全部信息） -->
    <el-dialog v-model="detail.visible" title="航班详情" width="820px" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ detail.data?.id }}</el-descriptions-item>
        <el-descriptions-item label="航班号">{{ detail.data?.name }}</el-descriptions-item>
        <el-descriptions-item label="机型">{{ detail.data?.planeName }}</el-descriptions-item>
        <el-descriptions-item label="出发机场">{{ detail.data?.departureAirportName }}</el-descriptions-item>
        <el-descriptions-item label="到达机场">{{ detail.data?.arrivalAirportName }}</el-descriptions-item>
        <el-descriptions-item label="起飞时间">{{ detail.data?.departureTime }}</el-descriptions-item>
        <el-descriptions-item label="到达时间">{{ detail.data?.arrivalTime }}</el-descriptions-item>
      </el-descriptions>

      <el-card shadow="never" header="座位与价格" style="margin-top:16px;">
        <el-table :data="detail.data?.flightSeatVOS || []" border>
          <el-table-column prop="seatTypeName" label="舱位" min-width="120" />
          <el-table-column prop="price" label="价格" min-width="120">
            <template #default="{ row }">{{ formatPrice(row.price) }}</template>
          </el-table-column>
          <el-table-column prop="available" label="可售" width="100" />
          <el-table-column prop="total" label="总座位" width="100" />
        </el-table>
        <div v-if="!(detail.data?.flightSeatVOS || []).length" class="muted">无座位数据</div>
      </el-card>

      <template #footer>
        <el-button @click="detail.visible=false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageFlights, getFlight, deleteFlight, addFlight, updateFlight } from '@/api/flight'
import { fetchPlaneOptions, fetchAirlineOptions, fetchSeatTypeOptions } from '@/api/options'

/** 列表/分页 */
const loading = ref(false)
const records = ref([])
const total = ref(0)
const page = reactive({ current: 1, size: 10 })
function onPageChange(p) { fetchPage(p) }
function refresh() { fetchPage(page.current) }

async function fetchPage(p = 1) {
  loading.value = true
  try {
    page.current = p
    const pr = await pageFlights(page.current, page.size)
    records.value = pr?.records ?? []
    total.value = Number(pr?.total ?? 0)
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

/** 详情 */
const detail = reactive({ visible: false, data: null })
async function openDetail(row) {
  try {
    const data = await getFlight(row.id)
    detail.data = data || row
    detail.visible = true
  } catch (e) {
    ElMessage.error(e?.message || '加载详情失败')
  }
}

/** 删除 */
async function onDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除航班 #${row.id}（${row.name}）？`, '删除确认', { type: 'warning' })
  } catch { return }
  try {
    await deleteFlight(row.id)
    ElMessage.success('删除成功')
    const onlyOne = records.value.length === 1 && page.current > 1
    if (onlyOne) page.current -= 1
    await fetchPage(page.current)
  } catch (e) {
    ElMessage.error(e?.message || '删除失败')
  }
}

/** 下拉选项 */
const optLoading = ref(false)
const planeOptions = ref([])     // [{value:id, label:name}]
const airlineOptions = ref([])   // [{value:id, label:'PEK→SHA'}]
const seatTypeOptions = ref([])  // [{value:seatTypeId, label:seatTypeName}]
async function ensureOptions() {
  if (planeOptions.value.length && airlineOptions.value.length && seatTypeOptions.value.length) return
  optLoading.value = true
  try {
    const [planes, airlines, seats] = await Promise.all([
      fetchPlaneOptions(),
      fetchAirlineOptions(),
      fetchSeatTypeOptions()
    ])
    planeOptions.value = planes
    airlineOptions.value = airlines
    seatTypeOptions.value = seats
  } catch (e) {
    ElMessage.error(e?.message || '加载下拉选项失败')
  } finally {
    optLoading.value = false
  }
}

/** 新增对话框（FlightDTO） */
const dlgAdd = reactive({ visible: false })
const addRef = ref(null)
const saving = ref(false)
const addForm = reactive({
  name: '',
  planeId: null,
  airlineId: null,
  departureTime: '',
  arrivalTime: '',
  seatTypeIds: [],       // 多选舱位
  seatPrices: []         // [{ seatTypeId, price }]
})
const addSeatPriceRows = ref([]) // [{ seatTypeId, seatTypeLabel, price }]
const rulesAdd = {
  name: [{ required: true, message: '请输入航班号', trigger: 'blur' }],
  planeId: [{ required: true, message: '请选择机型', trigger: 'change' }],
  airlineId: [{ required: true, message: '请选择航线', trigger: 'change' }],
  departureTime: [{ required: true, message: '请选择起飞时间', trigger: 'change' }],
  arrivalTime: [{ required: true, message: '请选择到达时间', trigger: 'change' }]
}

function openAdd() {
  Object.assign(addForm, {
    name: '', planeId: null, airlineId: null,
    departureTime: '', arrivalTime: '',
    seatTypeIds: []
  })
  addSeatPriceRows.value = []
  dlgAdd.visible = true
  setTimeout(() => addRef.value?.clearValidate(), 0)
}

function syncAddSeatPriceRows() {
  const old = new Map(addSeatPriceRows.value.map(r => [r.seatTypeId, r.price]))
  addSeatPriceRows.value = addForm.seatTypeIds.map(id => ({
    seatTypeId: id,
    seatTypeLabel: seatTypeOptions.value.find(s => s.value === id)?.label || `#${id}`,
    price: old.get(id) ?? ''
  }))
}

async function onAddSubmit() {
  await addRef.value?.validate()
  for (const r of addSeatPriceRows.value) {
    if (r.price === '' || isNaN(Number(r.price))) {
      ElMessage.error(`舱位「${r.seatTypeLabel}」价格需为数字`)
      return
    }
  }
  const dto = {
    name: addForm.name,
    planeId: addForm.planeId,
    airlineId: addForm.airlineId,
    departureTime: addForm.departureTime,  // 'YYYY-MM-DD HH:mm:ss'
    arrivalTime: addForm.arrivalTime,
    seatPrices: addSeatPriceRows.value.map(r => ({
      seatTypeId: r.seatTypeId,
      price: Number(r.price)
    }))
  }
  try {
    saving.value = true
    await addFlight(dto)
    ElMessage.success('新增成功')
    dlgAdd.visible = false
    await fetchPage(1)
  } catch (e) {
    ElMessage.error(e?.message || '新增失败')
  } finally {
    saving.value = false
  }
}

/** 修改对话框（FlightUpdateDTO） */
const dlgEdit = reactive({ visible: false })
const editRef = ref(null)
const editForm = reactive({
  id: null,
  name: '',
  planeId: null,
  airlineId: null,
  departureTime: '',
  arrivalTime: '',
  // UI 使用：把 VO 的 flightSeatVOS 映射成可编辑行
  flightSeatVOS: [] // [{ seatTypeId, seatTypeName, available, price, total }]
})
const rulesEdit = {
  name: [{ required: true, message: '请输入航班号', trigger: 'blur' }],
  planeId: [{ required: true, message: '请选择机型', trigger: 'change' }],
  airlineId: [{ required: true, message: '请选择航线', trigger: 'change' }],
  departureTime: [{ required: true, message: '请选择起飞时间', trigger: 'change' }],
  arrivalTime: [{ required: true, message: '请选择到达时间', trigger: 'change' }]
}
const editSeatRows = computed(() => editForm.flightSeatVOS || [])

async function openEdit(row) {
  try {
    await ensureOptions()
    const vo = await getFlight(row.id) // FlightVO
    // 将 VO 数据回填到表单
    Object.assign(editForm, {
      id: vo.id,
      name: vo.name,
      planeId: findPlaneIdByName(vo.planeName),
      airlineId: findAirlineIdByNames(vo.departureAirportName, vo.arrivalAirportName),
      departureTime: vo.departureTime,   // 已是 "yyyy-MM-dd HH:mm:ss" 格式
      arrivalTime: vo.arrivalTime,
      flightSeatVOS: (vo.flightSeatVOS || []).map(s => ({
        seatTypeId: findSeatTypeIdByName(s.seatTypeName), // 通过名称映射 id
        seatTypeName: s.seatTypeName,
        available: Number(s.available ?? 0),
        price: (s.price ?? '') + '',
        total: Number(s.total ?? 0)
      }))
    })
    dlgEdit.visible = true
    setTimeout(() => editRef.value?.clearValidate(), 0)
  } catch (e) {
    ElMessage.error(e?.message || '加载航班用于编辑失败')
  }
}

// 名称 -> id 的辅助（用下拉缓存）
function findPlaneIdByName(name) {
  const hit = planeOptions.value.find(p => p.label === name)
  return hit?.value ?? null
}
function findAirlineIdByNames(depName, arrName) {
  // airline 下拉 label 形如 "北京首都(PEK) → 上海虹桥(SHA)"（按你的实现）
  // 简单包含判断（可根据你的实际 label 规则调整）
  const hit = airlineOptions.value.find(a => a.label.includes(depName) && a.label.includes(arrName))
  return hit?.value ?? null
}
function findSeatTypeIdByName(n) {
  return seatTypeOptions.value.find(s => s.label === n)?.value ?? null
}

async function onEditSubmit() {
  await editRef.value?.validate()
  // 校验可售/价格
  for (const r of editSeatRows.value) {
    if (r.available == null || r.available < 0 || !Number.isInteger(r.available)) {
      ElMessage.error(`舱位「${r.seatTypeName}」可售需为>=0的整数`)
      return
    }
    if (r.price === '' || isNaN(Number(r.price))) {
      ElMessage.error(`舱位「${r.seatTypeName}」价格需为数字`)
      return
    }
    if (!r.seatTypeId) {
      ElMessage.error(`未找到舱位「${r.seatTypeName}」的 seatTypeId，请检查字典数据`)
      return
    }
  }
  const dto = {
    id: editForm.id,
    name: editForm.name,
    planeId: editForm.planeId,
    airlineId: editForm.airlineId,
    departureTime: editForm.departureTime,
    arrivalTime: editForm.arrivalTime,
    flightSeatInventoryDTOS: editSeatRows.value.map(r => ({
      seatTypeId: r.seatTypeId,
      available: Number(r.available),
      price: Number(r.price)
    }))
  }
  try {
    saving.value = true
    await updateFlight(dto)
    ElMessage.success('保存成功')
    dlgEdit.visible = false
    await fetchPage(page.current)
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

/** 生命周期 */
onMounted(async () => {
  await ensureOptions()
  await fetchPage(1)
})

/** 工具 */
function formatPrice(v) {
  if (v == null) return '-'
  const n = typeof v === 'string' ? Number(v) : v
  if (Number.isNaN(n)) return String(v)
  return '￥' + n.toFixed(2)
}
</script>

<style scoped>
.wrap { padding: 16px; }
.toolbar { margin-bottom: 12px; }
.pager { display:flex; justify-content:flex-end; margin-top:12px; }
.hint { color:#888; font-size:12px; margin-top:6px; }
.muted { color:#999; padding: 8px 0; }
</style>
