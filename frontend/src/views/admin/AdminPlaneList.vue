<template>
  <div class="wrap">
    <!-- 顶部工具条 -->
    <el-row :gutter="12" class="toolbar">
      <el-col :span="12">
        <el-button type="primary" @click="openAdd">新增飞机</el-button>
        <el-button v-if="searchMode" text type="warning" @click="exitSearch">退出搜索</el-button>
      </el-col>

      <el-col :span="12" style="text-align:right">
        <el-space>
          <el-input v-model.trim="q.name" placeholder="飞机名称（可选）" clearable style="width: 240px" />
          <el-button type="primary" plain @click="onSearch">搜索</el-button>
          <el-button @click="onResetQuery">清空</el-button>
        </el-space>
      </el-col>
    </el-row>

    <!-- 列表（仅显示 ID / 名称） -->
    <el-card shadow="never">
      <el-table
          :data="records"
          v-loading="loading"
          border
          style="width:100%"
          :header-cell-style="{ fontWeight: 600 }">
        <el-table-column prop="id" label="ID" width="100" />
        <el-table-column prop="name" label="飞机名称" min-width="260" show-overflow-tooltip />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="info" @click="openDetail(row)">详情</el-button>
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 服务端分页 -->
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

    <!-- 新增/编辑 Dialog（含舱位多选 + 数量 + 每行人数） -->
    <el-dialog v-model="dlg.visible" :title="dlgTitle" width="780px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item v-if="dlg.mode==='edit'" label="ID">
          <el-input v-model="form.id" disabled />
        </el-form-item>

        <el-form-item label="飞机名称" prop="name">
          <el-input v-model.trim="form.name" placeholder="例如：A320-200 / B737-800" />
        </el-form-item>

        <el-form-item label="舱位类型" prop="seatTypeNames">
          <el-select
              v-model="form.seatTypeNames"
              :loading="optLoading"
              multiple
              filterable
              clearable
              placeholder="选择需要配置的舱位类型（可多选）"
              style="width:100%"
              @change="syncSeatCountsOnTypesChange"
          >
            <el-option v-for="opt in seatTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
          <div class="hint">为每个舱位设置“数量（座位总数）”与“每行人数（rowNumber）”。</div>
        </el-form-item>

        <el-form-item v-if="form.seatTypeNames.length>0" label="舱位容量设置" required>
          <el-table :data="seatRows" border style="width:100%">
            <el-table-column label="舱位" prop="seatTypeName" />
            <el-table-column label="数量（count）" width="220">
              <template #default="{ row }">
                <el-input-number
                    v-model="row.count"
                    :min="0" :max="99999" :step="1"
                    controls-position="right"
                />
              </template>
            </el-table-column>
            <el-table-column label="每行人数（rowNumber）" width="260">
              <template #default="{ row }">
                <el-input-number
                    v-model="row.rowNumber"
                    :min="1" :max="20" :step="1"
                    controls-position="right"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-space>
          <el-button @click="dlg.visible=false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="onSubmit">
            {{ dlg.mode==='add' ? '提交' : '保存' }}
          </el-button>
        </el-space>
      </template>
    </el-dialog>

    <!-- 详情 Dialog（只读，显示舱位配置 + 自动生成座位图） -->
    <el-dialog v-model="detail.visible" title="飞机详情" width="820px" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ detail.data?.id }}</el-descriptions-item>
        <el-descriptions-item label="飞机名称">{{ detail.data?.planeName || detail.data?.name }}</el-descriptions-item>
      </el-descriptions>

      <div style="margin-top:12px;">
        <el-card shadow="never" header="舱位配置">
          <el-table :data="detailSeatRows" border>
            <el-table-column label="舱位" prop="seatTypeName" />
            <el-table-column label="数量" prop="count" width="140" />
            <el-table-column label="每行人数" prop="rowNumber" width="160" />
          </el-table>
          <div v-if="!detailSeatRows.length" style="color:#999; padding:8px 0;">无</div>
        </el-card>
      </div>

      <!-- ▼▼ 新增：自动座位图 ▼▼ -->
      <div v-if="seatCharts.length" class="chart-section">
        <h4 class="chart-title">座位图（自动生成）</h4>

        <div
            v-for="(chart, idx) in seatCharts"
            :key="idx"
            class="seat-block"
        >
          <div class="seat-block__header">
            <strong>{{ chart.seatTypeName }}</strong>
            <span class="meta">共 {{ chart.count }} 座，{{ chart.rows }} 排 × {{ chart.cols }} 列</span>
          </div>

          <div class="seat-grid-wrap">
            <div class="window-tag left">靠窗</div>

            <div
                class="seat-grid"
                :style="{
                gridTemplateColumns: 'repeat(' + chart.cols + ', 42px)',
                gridTemplateRows: 'repeat(' + chart.rows + ', 38px)'
              }"
            >
              <template v-for="(cell, i) in chart.cells" :key="i">
                <div
                    class="seat-cell"
                    :class="{
                    blank: cell.blank,
                    window: cell.isWindow
                  }"
                >
                  <span v-if="!cell.blank">{{ cell.label }}</span>
                </div>
              </template>
            </div>

            <div class="window-tag right">靠窗</div>
          </div>

          <div class="legend">
            <span class="legend-item"><i class="legend-box"></i> 正常座位</span>
            <span class="legend-item"><i class="legend-box window"></i> 靠窗座位</span>
            <span class="legend-item"><i class="legend-box blank"></i> 空位（占位）</span>
          </div>
        </div>
      </div>
      <!-- ▲▲ 新增：自动座位图 ▲▲ -->

      <template #footer>
        <el-button @click="detail.visible=false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  pagePlanes, searchPlanes, addPlane, updatePlane, deletePlane, getPlane
} from '@/api/plane'
import { fetchSeatTypeOptions } from '@/api/seatType'

/** 列表/分页/搜索 */
const loading = ref(false)
const records = ref([])
const total = ref(0)
const page = reactive({ current: 1, size: 10 })
const q = reactive({ name: '' })
const searchMode = ref(false)

function onPageChange(p) { searchMode.value ? fetchSearchPage(p) : fetchPage(p) }
async function onSearch() { await fetchSearchPage(1) }
function onResetQuery() { q.name = '' }
function exitSearch() { q.name = ''; fetchPage(1) }

async function fetchPage(p = 1) {
  loading.value = true
  try {
    page.current = p
    const pr = await pagePlanes(page.current, page.size)
    records.value = pr?.records ?? []
    total.value = Number(pr?.total ?? 0)
    searchMode.value = false
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}
async function fetchSearchPage(p = 1) {
  loading.value = true
  try {
    page.current = p
    const pr = await searchPlanes(q.name || '', page.current, page.size)
    records.value = pr?.records ?? []
    total.value = Number(pr?.total ?? 0)
    searchMode.value = true
  } catch (e) {
    ElMessage.error(e?.message || '搜索失败')
  } finally {
    loading.value = false
  }
}

/** 舱位类型选项（用于新增/编辑） */
const optLoading = ref(false)
const seatTypeOptions = ref([]) // [{label, value}]
async function ensureSeatOptions() {
  if (seatTypeOptions.value.length) return
  optLoading.value = true
  try {
    seatTypeOptions.value = await fetchSeatTypeOptions()
  } catch (e) {
    ElMessage.error(e?.message || '加载舱位类型失败')
  } finally {
    optLoading.value = false
  }
}

/** 新增/编辑对话框 */
const dlg = reactive({ visible: false, mode: 'add' }) // add | edit
const dlgTitle = computed(() => (dlg.mode === 'add' ? '新增飞机' : '编辑飞机'))
const formRef = ref(null)
const form = reactive({
  id: null,
  name: '',
  seatTypeNames: [],        // 多选舱位名
  seatTypeCounts: []        // [{ seatTypeName, count, rowNumber }]
})
const saving = ref(false)

/** 舱位行（跟随多选） */
const seatRows = computed(() =>
    form.seatTypeNames.map(n => {
      const found = form.seatTypeCounts.find(x => x.seatTypeName === n)
      return {
        seatTypeName: n,
        count: found ? Number(found.count ?? 0) : 0,
        rowNumber: found ? Number(found.rowNumber ?? 1) : 1
      }
    })
)

/** 校验规则 */
const rules = {
  name: [{ required: true, message: '请输入飞机名称', trigger: 'blur' }],
  seatTypeNames: [{
    validator: (_, v, cb) => (!v || v.length === 0) ? cb(new Error('请至少选择一个舱位类型')) : cb(),
    trigger: 'change'
  }]
}

/** 初始化 */
onMounted(async () => {
  await ensureSeatOptions()
  await fetchPage(1)
})

/** 新增 */
function openAdd() {
  dlg.mode = 'add'
  Object.assign(form, { id: null, name: '', seatTypeNames: [], seatTypeCounts: [] })
  dlg.visible = true
  setTimeout(() => formRef.value?.clearValidate(), 0)
}

/** 编辑（回填 count / rowNumber） */
function openEdit(row) {
  dlg.mode = 'edit'
  const list = Array.isArray(row.seatTypeCounts) ? row.seatTypeCounts : []
  Object.assign(form, {
    id: row.id,
    name: row.name,
    seatTypeNames: list.map(s => s.seatTypeName),
    seatTypeCounts: list.map(s => ({
      seatTypeName: s.seatTypeName,
      count: Number(s.count || 0),
      rowNumber: Number(s.rowNumber || 1)
    }))
  })
  dlg.visible = true
  setTimeout(() => formRef.value?.clearValidate(), 0)
}

/** 详情（只读弹窗） */
const detail = reactive({ visible: false, data: null })
const detailSeatRows = computed(() => {
  const arr = detail.data?.seatTypeCounts || []
  return Array.isArray(arr) ? arr : []
})
async function openDetail(row) {
  try {
    const data = await getPlane(row.id) // 期望返回 PlaneVO { id, planeName/name, seatTypeCounts[] }
    detail.data = {
      id: data?.id ?? row.id,
      planeName: data?.planeName ?? data?.name ?? row.name,
      seatTypeCounts: Array.isArray(data?.seatTypeCounts) ? data.seatTypeCounts : []
    }
    detail.visible = true
  } catch (e) {
    ElMessage.error(e?.message || '加载详情失败')
  }
}

/** 多选变化时，同步 seatTypeCounts（保留已有 count/rowNumber） */
function syncSeatCountsOnTypesChange() {
  form.seatTypeCounts = form.seatTypeNames.map(n => {
    const old = form.seatTypeCounts.find(x => x.seatTypeName === n)
    return {
      seatTypeName: n,
      count: old ? Number(old.count ?? 0) : 0,
      rowNumber: old ? Number(old.rowNumber ?? 1) : 1
    }
  })
}

/** 提交新增/编辑：映射为 PlaneDTO { planeName, seatTypeCounts } */
async function onSubmit() {
  await formRef.value?.validate()

  // 前端校验
  for (const r of seatRows.value) {
    if (!Number.isInteger(r.count) || r.count < 0) {
      ElMessage.error(`舱位「${r.seatTypeName}」数量需为非负整数`)
      return
    }
    if (!Number.isInteger(r.rowNumber) || r.rowNumber < 1) {
      ElMessage.error(`舱位「${r.seatTypeName}」每行人数需为 >= 1 的整数`)
      return
    }
  }

  form.seatTypeCounts = seatRows.value.map(x => ({
    seatTypeName: x.seatTypeName,
    count: x.count,
    rowNumber: x.rowNumber
  }))

  saving.value = true
  try {
    const dto = { planeName: form.name, seatTypeCounts: form.seatTypeCounts }
    if (dlg.mode === 'add') {
      const created = await addPlane(dto)
      ElMessage.success(`新增成功（ID=${created?.id ?? '-' }）`)
      await fetchPage(1)
    } else {
      await updatePlane({ id: form.id, ...dto }) // 如需 path 传参，可改成 updatePlane(form.id, dto)
      ElMessage.success('保存成功')
      searchMode.value ? await fetchSearchPage(page.current) : await fetchPage(page.current)
    }
    dlg.visible = false
  } catch (e) {
    ElMessage.error(e?.message || (dlg.mode==='add' ? '新增失败' : '保存失败'))
  } finally {
    saving.value = false
  }
}

/* ======================
 * 自动座位图（详情用）
 * ====================== */

/** 将 seatTypeCounts 转为渲染块（含 rows/cols/cells） */
const seatCharts = computed(() => {
  const data = detailSeatRows.value
  const charts = []

  for (const item of data) {
    const name = item.seatTypeName
    const count = Number(item.count || 0)
    const cols  = Math.max(1, Number(item.rowNumber || 1)) // 每行人数
    const rows  = Math.ceil(count / cols)

    const cells = buildSeatCells(rows, cols, count) // 扁平数组

    charts.push({
      seatTypeName: name,
      count,
      rows,
      cols,
      cells
    })
  }
  return charts
})

/** 生成某舱位的全部单元格（包含空白占位） */
function buildSeatCells(rows, cols, count) {
  const cells = []
  const letters = alphabet(cols) // ['A','B','C',...]

  for (let r = 0; r < rows; r++) {
    for (let c = 0; c < cols; c++) {
      const idx = r * cols + c
      if (idx < count) {
        // 标签左->右：A01 B01 C01；上->下：A01 A02 ...
        const label = `${letters[c]}${String(r + 1).padStart(2, '0')}`
        cells.push({
          label,
          isWindow: (c === 0 || c === cols - 1),
          blank: false
        })
      } else {
        cells.push({ blank: true, isWindow: (c === 0 || c === cols - 1) })
      }
    }
  }
  return cells
}

/** 生成 A,B,C,... 序列（cols<=26 足够用；超过可扩展为 AA, AB...） */
function alphabet(n) {
  const arr = []
  for (let i = 0; i < n; i++) {
    arr.push(String.fromCharCode(65 + (i % 26))) // A=65
  }
  return arr
}

/** 删除 */
async function onDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除飞机 #${row.id}（${row.name}）？`, '删除确认', { type: 'warning' })
  } catch { return }
  try {
    await deletePlane(row.id)
    ElMessage.success('删除成功')
    const last = records.value.length === 1 && page.current > 1
    if (last) page.current -= 1
    searchMode.value ? await fetchSearchPage(page.current) : await fetchPage(page.current)
  } catch (e) {
    ElMessage.error(e?.message || '删除失败')
  }
}
</script>

<style scoped>
.wrap { padding: 16px; }
.toolbar { margin-bottom: 12px; }
.pager { display:flex; justify-content:flex-end; margin-top:12px; }
.hint { color:#888; font-size:12px; margin-top:6px; }

/* 座位图样式 */
.chart-section { margin-top: 16px; }
.chart-title { margin: 12px 0; font-size: 15px; font-weight: 700; color: #333; }

.seat-block {
  border: 1px dashed #cbd5e1;
  border-radius: 10px;
  padding: 12px;
  margin-top: 12px;
  background: #fcfcfd;
}
.seat-block__header {
  display: flex; align-items: baseline; gap: 10px; margin-bottom: 10px;
}
.seat-block__header .meta { color: #666; font-size: 12px; }

.seat-grid-wrap {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 8px;
  align-items: center;
}

.window-tag {
  writing-mode: vertical-rl;
  text-orientation: mixed;
  color: #64748b;
  font-size: 12px;
  user-select: none;
}
.window-tag.left { text-align: right; }
.window-tag.right { text-align: left; }

.seat-grid {
  display: grid;
  gap: 6px;
  justify-content: center;
  align-items: center;
  padding: 8px 10px;
  background: repeating-linear-gradient(
      to bottom,
      #f8fafc 0,
      #f8fafc 28px,
      #f1f5f9 28px,
      #f1f5f9 29px
  );
  border: 1px solid #e2e8f0;
  border-radius: 12px;
}

.seat-cell {
  width: 42px;
  height: 38px;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px;
  background: #fff;
  color: #334155;
  box-shadow: 0 1px 0 rgba(0,0,0,0.02);
}
.seat-cell.window { border-color: #60a5fa; box-shadow: 0 0 0 1px rgba(96,165,250,0.15) inset; }
.seat-cell.blank { background: #f8fafc; color: transparent; border-style: dashed; }

.legend { margin-top: 8px; color: #64748b; font-size: 12px; display: flex; gap: 16px; flex-wrap: wrap; }
.legend-item { display: inline-flex; align-items: center; gap: 6px; }
.legend-box {
  width: 14px; height: 12px; border: 1px solid #cbd5e1; border-radius: 3px; display: inline-block; background: #fff;
}
.legend-box.window { border-color: #60a5fa; box-shadow: 0 0 0 1px rgba(96,165,250,0.15) inset; }
.legend-box.blank { background: #f8fafc; border-style: dashed; }
</style>
