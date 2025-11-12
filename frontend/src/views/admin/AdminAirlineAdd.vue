<template>
  <div class="wrap">
    <!-- 顶部工具栏 -->
    <el-row :gutter="12" class="toolbar">
      <el-col :span="12">
        <el-button type="primary" @click="openAdd">新增航线</el-button>
      </el-col>
      <el-col :span="12" style="text-align:right">
        <el-space>
          <!-- 下拉选择出发/到达 -->
          <el-select
              v-model="search.from"
              placeholder="出发机场（必选）"
              filterable clearable style="width: 220px"
          >
            <el-option
                v-for="ap in airportOptions"
                :key="ap.id ?? ap.name"
                :label="ap.name"
                :value="ap.name"
            />
          </el-select>

          <el-select
              v-model="search.to"
              placeholder="到达机场（必选）"
              filterable clearable style="width: 220px"
          >
            <el-option
                v-for="ap in airportOptions"
                :key="`to-${ap.id ?? ap.name}`"
                :label="ap.name"
                :value="ap.name"
            />
          </el-select>

          <el-button @click="onSearch" type="primary" plain>搜索</el-button>
          <el-button @click="onResetSearch">重置</el-button>
        </el-space>
      </el-col>
    </el-row>

    <!-- 表格 -->
    <el-card shadow="never">
      <el-table
          :data="list.records"
          v-loading="loading"
          border
          style="width: 100%"
          :header-cell-style="{ fontWeight: 600 }"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="fromAirport" label="出发机场" min-width="180" />
        <el-table-column prop="toAirport" label="到达机场" min-width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 后端分页 -->
      <div style="display:flex; justify-content:flex-end; margin-top:12px;">
        <el-pagination
            background
            layout="prev, pager, next, sizes, jumper, ->, total"
            :total="list.total"
            :current-page="page.current"
            :page-size="page.size"
            :page-sizes="[10,20,50,100]"
            @current-change="onPageChange"
            @size-change="onSizeChange"
        />
      </div>
    </el-card>

    <!-- 新增 / 编辑 弹窗 -->
    <el-dialog v-model="dialog.visible" :title="dialog.mode==='add'?'新增航线':'编辑航线'" width="460px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item v-if="dialog.mode==='edit'" label="ID">
          <el-input v-model="form.id" disabled />
        </el-form-item>

        <el-form-item label="出发机场" prop="fromAirport">
          <el-select v-model="form.fromAirport" filterable placeholder="选择出发机场" style="width: 100%">
            <el-option
                v-for="ap in airportOptions"
                :key="`form-from-${ap.id ?? ap.name}`"
                :label="ap.name"
                :value="ap.name"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="到达机场" prop="toAirport">
          <el-select v-model="form.toAirport" filterable placeholder="选择到达机场" style="width: 100%">
            <el-option
                v-for="ap in airportOptions"
                :key="`form-to-${ap.id ?? ap.name}`"
                :label="ap.name"
                :value="ap.name"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-space>
          <el-button @click="dialog.visible=false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="onSubmit">
            {{ dialog.mode==='add' ? '提交' : '保存' }}
          </el-button>
        </el-space>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listAirlines,
  queryAirlinesByFromTo,
  addAirline,
  updateAirline,
  deleteAirline
} from '@/api/airline'
import { listAirportOptions } from '@/api/airport'

const loading = ref(false)
const list = reactive({ total: 0, records: [] })
const page = reactive({ current: 1, size: 10 })

// 搜索条件
const search = reactive({ from: '', to: '' })

// 机场下拉选项
const airportOptions = ref([])

// 弹窗与表单
const dialog = reactive({ visible: false, mode: 'add' }) // add | edit
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
  await Promise.all([fetchAirportOptions(), fetchList()])
})

async function fetchAirportOptions() {
  try {
    const data = await listAirportOptions({ page: 1, size: 1000 })
    airportOptions.value = data?.records ?? []
  } catch (e) {
    ElMessage.error(e?.message || '加载机场列表失败')
  }
}

// 根据搜索状态决定调用哪个接口
async function fetchList() {
  loading.value = true
  try {
    let data
    if (search.from && search.to) {
      data = await queryAirlinesByFromTo({
        fromAirport: search.from,
        toAirport: search.to,
        page: page.current,
        size: page.size
      })
    } else {
      data = await listAirlines({ page: page.current, size: page.size })
    }
    list.total = data?.total ?? 0
    list.records = data?.records ?? []
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function onSearch() {
  if (!search.from || !search.to) {
    ElMessage.warning('搜索需要同时选择“出发机场 + 到达机场”')
    return
  }
  page.current = 1
  await fetchList()
}

async function onResetSearch() {
  search.from = ''
  search.to = ''
  page.current = 1
  await fetchList()
}

function onPageChange(p) {
  page.current = p
  fetchList()
}
function onSizeChange(s) {
  page.size = s
  page.current = 1
  fetchList()
}

function openAdd() {
  dialog.mode = 'add'
  Object.assign(form, { id: null, fromAirport: '', toAirport: '' })
  dialog.visible = true
  setTimeout(() => formRef.value?.clearValidate(), 0)
}
function openEdit(row) {
  dialog.mode = 'edit'
  Object.assign(form, { id: row.id, fromAirport: row.fromAirport, toAirport: row.toAirport })
  dialog.visible = true
  setTimeout(() => formRef.value?.clearValidate(), 0)
}

async function onSubmit() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (dialog.mode === 'add') {
      const created = await addAirline({ fromAirport: form.fromAirport, toAirport: form.toAirport })
      ElMessage.success(`新增成功（ID=${created?.id ?? '-' }）`)
    } else {
      await updateAirline({ id: form.id, fromAirport: form.fromAirport, toAirport: form.toAirport })
      ElMessage.success('保存成功')
    }
    dialog.visible = false
    await fetchList()
  } catch (e) {
    ElMessage.error(e?.message || (dialog.mode==='add'?'新增失败':'保存失败'))
  } finally {
    saving.value = false
  }
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除航线 #${row.id}（${row.fromAirport} → ${row.toAirport}）？`, '删除确认', {
      type: 'warning'
    })
  } catch { return }

  try {
    await deleteAirline(row.id)
    ElMessage.success('删除成功')
    await fetchList()
  } catch (e) {
    ElMessage.error(e?.message || '删除失败')
  }
}
</script>

<style scoped>
.wrap { padding: 16px; }
.toolbar { margin-bottom: 12px; }
</style>
