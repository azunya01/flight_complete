<template>
  <div class="wrap">
    <!-- 工具条 -->
    <el-row :gutter="12" class="toolbar">
      <el-col :span="12">
        <el-button type="primary" @click="openAdd">新增航线</el-button>
      </el-col>
      <el-col :span="12" style="text-align:right">
        <el-space>
          <!-- 搜索：必须成对填写（已换成下拉） -->
          <el-select v-model="query.from" :loading="optLoading" filterable clearable placeholder="出发机场（必选）" style="width:220px">
            <el-option v-for="opt in airportOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
          <el-select v-model="query.to" :loading="optLoading" filterable clearable placeholder="到达机场（必选）" style="width:220px">
            <el-option v-for="opt in airportOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
          <el-button type="primary" plain @click="onSearch">搜索</el-button>
          <el-button @click="onResetQuery">重置</el-button>
        </el-space>
      </el-col>
    </el-row>

    <!-- 表格 -->
    <el-card shadow="never">
      <el-table
          :data="records"
          v-loading="loading"
          border
          style="width: 100%"
          :header-cell-style="{ fontWeight: 600 }"
      >
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="fromAirport" label="出发机场" min-width="200" />
        <el-table-column prop="toAirport" label="到达机场" min-width="200" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
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

    <!-- 新增/编辑 Dialog -->
    <el-dialog v-model="dlg.visible" :title="dlg.mode==='add' ? '新增航线' : '编辑航线'" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item v-if="dlg.mode==='edit'" label="ID">
          <el-input v-model="form.id" disabled />
        </el-form-item>

        <el-form-item label="出发机场" prop="fromAirport">
          <el-select v-model="form.fromAirport" :loading="optLoading" filterable placeholder="请选择出发机场" style="width:100%">
            <el-option v-for="opt in airportOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="到达机场" prop="toAirport">
          <el-select v-model="form.toAirport" :loading="optLoading" filterable placeholder="请选择到达机场" style="width:100%">
            <el-option v-for="opt in airportOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-space>
          <el-button @click="dlg.visible=false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="onSubmit">{{ dlg.mode==='add' ? '提交' : '保存' }}</el-button>
        </el-space>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  pageAirlines,
  pageAirlinesByFromTo,
  addAirline,
  updateAirline,
  deleteAirline
} from '@/api/airline'
import { fetchAirportOptions } from '@/api/airport'

const loading = ref(false)
const records = ref([])           // 当前页数据
const total = ref(0)              // 总条数
const page = reactive({ current: 1, size: 10 })

// 搜索区：服务端分页（必须成对）
const query = reactive({ from: '', to: '' })
const searchMode = ref(false)     // 当前是否处于搜索分页模式

// 机场下拉
const airportOptions = ref([])
const optLoading = ref(false)

// 表单 / 弹窗
const dlg = reactive({ visible: false, mode: 'add' }) // add | edit
const formRef = ref(null)
const form = reactive({ id: null, fromAirport: '', toAirport: '' })
const saving = ref(false)

const rules = {
  fromAirport: [{ required: true, message: '请选择出发机场', trigger: 'change' }],
  toAirport: [
    { required: true, message: '请选择到达机场', trigger: 'change' },
    {
      validator: (_, v, cb) => {
        if (v && v === form.fromAirport) cb(new Error('出发与到达不能相同'))
        else cb()
      },
      trigger: 'change'
    }
  ]
}

onMounted(async () => {
  await ensureAirportOptions()
  await fetchPage(1)
})

async function ensureAirportOptions() {
  if (airportOptions.value.length > 0) return
  optLoading.value = true
  try {
    airportOptions.value = await fetchAirportOptions()
  } catch (e) {
    ElMessage.error(e?.message || '加载机场选项失败')
  } finally {
    optLoading.value = false
  }
}

// 拉取列表：普通模式（按页）
async function fetchPage(p = 1) {
  loading.value = true
  try {
    page.current = p
    const pr = await pageAirlines(page.current, page.size)
    records.value = pr?.records ?? []
    total.value = Number(pr?.total ?? 0)
    searchMode.value = false
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// 拉取列表：搜索模式（按出发+到达分页）
async function fetchSearchPage(p = 1) {
  loading.value = true
  try {
    page.current = p
    const pr = await pageAirlinesByFromTo(query.from, query.to, page.current, page.size)
    records.value = pr?.records ?? []
    total.value = Number(pr?.total ?? 0)
    searchMode.value = true
  } catch (e) {
    ElMessage.error(e?.message || '搜索失败')
  } finally {
    loading.value = false
  }
}

function onPageChange(p) {
  if (searchMode.value) fetchSearchPage(p)
  else fetchPage(p)
}

async function onSearch() {
  if (!query.from || !query.to) {
    ElMessage.warning('搜索需要同时选择“出发机场 + 到达机场”')
    return
  }
  await fetchSearchPage(1)
}

async function onResetQuery() {
  query.from = ''
  query.to = ''
  await fetchPage(1)
}

// 新增
function openAdd() {
  dlg.mode = 'add'
  Object.assign(form, { id: null, fromAirport: '', toAirport: '' })
  dlg.visible = true
  setTimeout(() => formRef.value?.clearValidate(), 0)
}

// 编辑
function openEdit(row) {
  dlg.mode = 'edit'
  Object.assign(form, { id: row.id, fromAirport: row.fromAirport, toAirport: row.toAirport })
  dlg.visible = true
  setTimeout(() => formRef.value?.clearValidate(), 0)
}

async function onSubmit() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (dlg.mode === 'add') {
      const created = await addAirline({ fromAirport: form.fromAirport, toAirport: form.toAirport })
      ElMessage.success(`新增成功（ID=${created?.id ?? '-' }）`)
    } else {
      await updateAirline({ id: form.id, fromAirport: form.fromAirport, toAirport: form.toAirport })
      ElMessage.success('保存成功')
    }
    dlg.visible = false
    // 刷新当前分页（保持当前模式）
    if (searchMode.value) await fetchSearchPage(page.current)
    else await fetchPage(page.current)
  } catch (e) {
    ElMessage.error(e?.message || (dlg.mode === 'add' ? '新增失败' : '保存失败'))
  } finally {
    saving.value = false
  }
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除航线 #${row.id}（${row.fromAirport} → ${row.toAirport}）？`, '删除确认', { type: 'warning' })
  } catch { return }
  try {
    await deleteAirline(row.id)
    ElMessage.success('删除成功')
    // 删除后：如果当前页删空且非第一页，往前翻一页更友好
    const isLastItemOnPage = records.value.length === 1 && page.current > 1
    if (isLastItemOnPage) page.current -= 1
    if (searchMode.value) await fetchSearchPage(page.current)
    else await fetchPage(page.current)
  } catch (e) {
    ElMessage.error(e?.message || '删除失败')
  }
}
</script>

<style scoped>
.wrap { padding: 16px; }
.toolbar { margin-bottom: 12px; }
.pager { display:flex; justify-content:flex-end; margin-top:12px; }
</style>
