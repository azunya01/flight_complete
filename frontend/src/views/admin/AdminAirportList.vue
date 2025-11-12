<template>
  <div class="wrap">
    <!-- 顶部工具条 -->
    <el-row :gutter="12" class="toolbar">
      <el-col :span="12">
        <el-button type="primary" @click="openAdd">新增机场</el-button>
        <el-button v-if="searchMode" @click="exitSearch" text type="warning">退出搜索</el-button>
      </el-col>

      <el-col :span="12" style="text-align:right">
        <el-space>
          <el-input v-model.trim="q.name" placeholder="机场名" clearable style="width: 180px" />
          <el-input v-model.trim="q.city" placeholder="城市" clearable style="width: 160px" />
          <el-input v-model.trim="q.address" placeholder="地址" clearable style="width: 220px" />
          <el-button type="primary" plain @click="onSearch">搜索</el-button>
          <el-button @click="onResetQuery">清空</el-button>
        </el-space>
      </el-col>
    </el-row>

    <!-- 列表卡片 -->
    <el-card shadow="never">
      <el-table
          :data="tableData"
          border
          v-loading="loading"
          style="width:100%"
          :header-cell-style="{ fontWeight: 600 }"
      >
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="name" label="机场名" min-width="160" />
        <el-table-column prop="city" label="城市" min-width="120" />
        <el-table-column prop="address" label="地址" min-width="220" show-overflow-tooltip />
        <el-table-column prop="tel" label="电话" min-width="140" />

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页：列表模式用后端 total；搜索模式用本地 total -->
      <div class="pager">
        <el-pagination
            background
            layout="prev, pager, next, jumper, ->, total"
            :total="total"
            :current-page="page.current"
            :page-size="page.size"
            @current-change="onPageChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑 Dialog -->
    <el-dialog
        v-model="dlg.visible"
        :title="dlg.mode==='add' ? '新增机场' : '编辑机场'"
        width="560px"
        destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item v-if="dlg.mode==='edit'" label="ID">
          <el-input v-model="form.id" disabled />
        </el-form-item>

        <el-form-item label="机场名" prop="name">
          <el-input v-model.trim="form.name" placeholder="例如：首都国际机场" />
        </el-form-item>

        <el-form-item label="城市" prop="city">
          <el-input v-model.trim="form.city" placeholder="例如：北京" />
        </el-form-item>

        <el-form-item label="地址" prop="address">
          <el-input v-model.trim="form.address" placeholder="例如：顺义区..." />
        </el-form-item>

        <el-form-item label="电话" prop="tel">
          <el-input v-model.trim="form.tel" placeholder="例如：010-xxxxxxx（可留空）" />
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
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listAirports,
  searchAirports,
  addAirport,
  updateAirport,
  deleteAirport
} from '@/api/airport'

const loading = ref(false)

// 列表模式：服务端分页；搜索模式：本地分页
const searchMode = ref(false)

// 分页状态
const page = reactive({ current: 1, size: 10 })
const total = ref(0)          // 列表模式：来自后端；搜索模式：本地集合长度

// 表格数据源（两种模式统一写到 tableData）
const list = ref([])          // 列表模式：后端 page.records
const searchFull = ref([])    // 搜索模式：完整 List（本地分页）
const tableData = computed(() => {
  if (!searchMode.value) return list.value
  const start = (page.current - 1) * page.size
  return searchFull.value.slice(start, start + page.size)
})

// 查询条件
const q = reactive({ name: '', city: '', address: '' })

// 表单 & 对话框
const formRef = ref(null)
const dlg = reactive({ visible: false, mode: 'add' }) // add | edit
const form = reactive({ id: null, name: '', city: '', address: '', tel: '' })
const saving = ref(false)

const rules = {
  name: [{ required: true, message: '请输入机场名', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }],
  tel: [
    { validator: (_, v, cb) => {
        if (!v) return cb()
        // 简单的电话格式提示（不强校验）
        const ok = /^[\d\-+()\s]+$/.test(v)
        ok ? cb() : cb(new Error('电话格式不正确'))
      }, trigger: 'blur'
    }
  ]
}

// 生命周期：加载第一页
onMounted(() => { fetchPage(1) })

// 拉列表（服务端分页）
async function fetchPage(p = 1) {
  searchMode.value = false
  loading.value = true
  try {
    page.current = p
    const pr = await listAirports(page.current, page.size)
    // 兼容 PageResult 命名
    const records = pr?.records ?? pr?.items ?? []
    total.value = Number(pr?.total ?? records.length ?? 0)
    list.value = records
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// 搜索（任意组合 name/city/address；后端返回 List，前端本地分页）
async function onSearch() {
  if (!q.name && !q.city && !q.address) {
    ElMessage.warning('请输入任意一个搜索条件')
    return
  }
  loading.value = true
  try {
    const res = await searchAirports({
      name: q.name || undefined,
      city: q.city || undefined,
      address: q.address || undefined
    })
    const arr = Array.isArray(res) ? res : []
    searchFull.value = arr
    total.value = arr.length
    page.current = 1
    searchMode.value = true
  } catch (e) {
    ElMessage.error(e?.message || '搜索失败')
  } finally {
    loading.value = false
  }
}

function exitSearch() {
  q.name = q.city = q.address = ''
  fetchPage(1)
}

function onResetQuery() {
  q.name = q.city = q.address = ''
}

function onPageChange(p) {
  if (searchMode.value) {
    page.current = p // 本地分页
  } else {
    fetchPage(p)     // 服务端分页
  }
}

// 打开新增
function openAdd() {
  dlg.mode = 'add'
  Object.assign(form, { id: null, name: '', city: '', address: '', tel: '' })
  dlg.visible = true
  setTimeout(() => formRef.value?.clearValidate(), 0)
}

// 打开编辑
function openEdit(row) {
  dlg.mode = 'edit'
  Object.assign(form, row)
  dlg.visible = true
  setTimeout(() => formRef.value?.clearValidate(), 0)
}

// 提交新增/编辑
async function onSubmit() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (dlg.mode === 'add') {
      const created = await addAirport({
        name: form.name, city: form.city, address: form.address, tel: form.tel
      })
      ElMessage.success(`新增成功（ID=${created?.id ?? '-' }）`)
    } else {
      await updateAirport({
        id: form.id, name: form.name, city: form.city, address: form.address, tel: form.tel
      })
      ElMessage.success('保存成功')
    }
    dlg.visible = false
    // 刷新：保持当前模式体验一致
    if (searchMode.value) {
      await onSearch()
    } else {
      await fetchPage(page.current)
    }
  } catch (e) {
    ElMessage.error(e?.message || (dlg.mode === 'add' ? '新增失败' : '保存失败'))
  } finally {
    saving.value = false
  }
}

// 删除
async function onDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除机场 #${row.id}（${row.name}）？`, '删除确认', { type: 'warning' })
  } catch { return }
  try {
    await deleteAirport(row.id)
    ElMessage.success('删除成功')
    if (searchMode.value) await onSearch()
    else await fetchPage(page.current)
  } catch (e) {
    ElMessage.error(e?.message || '删除失败')
  }
}
</script>

<style scoped>
.wrap { padding: 16px; }
.toolbar { margin-bottom: 12px; }
.pager { display: flex; justify-content: flex-end; margin-top: 12px; }
</style>
