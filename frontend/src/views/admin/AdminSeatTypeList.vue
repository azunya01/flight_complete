<template>
  <div class="wrap">
    <!-- 工具条 -->
    <el-row :gutter="12" class="toolbar">
      <el-col :span="12">
        <el-button type="primary" @click="openAdd">新增舱位类型</el-button>
      </el-col>

      <el-col :span="12" style="text-align:right">
        <el-space>
          <el-input v-model.trim="q.name" placeholder="按名称搜索" clearable style="width: 200px" />
          <el-input v-model.trim="q.code" placeholder="按代码搜索" clearable style="width: 160px" />
          <el-button type="primary" plain @click="onSearch">搜索</el-button>
          <el-button @click="onReset">清空</el-button>
        </el-space>
      </el-col>
    </el-row>

    <!-- 列表 -->
    <el-card shadow="never">
      <el-table
          :data="paged"
          v-loading="loading"
          border
          style="width:100%"
          :header-cell-style="{ fontWeight: 600 }"
      >
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="name" label="名称" min-width="200" />
        <el-table-column prop="code" label="代码" min-width="140" />

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 前端分页 -->
      <div class="pager">
        <el-pagination
            background
            layout="total, ->, prev, pager, next, jumper"
            :total="filtered.length"
            :current-page="page.current"
            :page-size="page.size"
            @current-change="(p)=>page.current=p"
        />
      </div>
    </el-card>

    <!-- 新增/编辑 Dialog -->
    <el-dialog
        v-model="dlg.visible"
        :title="dlg.mode==='add' ? '新增舱位类型' : '编辑舱位类型'"
        width="520px"
        destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item v-if="dlg.mode==='edit'" label="ID">
          <el-input v-model="form.id" disabled />
        </el-form-item>

        <el-form-item label="名称" prop="name">
          <el-input v-model.trim="form.name" placeholder="例如：经济舱 / 商务舱 / 头等舱" />
        </el-form-item>

        <el-form-item label="代码" prop="code">
          <el-input v-model.trim="form.code" placeholder="例如：Y / C / F" />
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
  listSeatTypes,
  addSeatType,
  updateSeatType,
  deleteSeatType
} from '@/api/seatType'

const loading = ref(false)
const full = ref([])                         // 全量列表
const q = reactive({ name: '', code: '' })   // 本地搜索条件
const page = reactive({ current: 1, size: 10 })

// 过滤后的列表（本地）
const filtered = computed(() => {
  const name = q.name?.toLowerCase() ?? ''
  const code = q.code?.toLowerCase() ?? ''
  return full.value.filter(x => {
    const okName = !name || String(x.name ?? '').toLowerCase().includes(name)
    const okCode = !code || String(x.code ?? '').toLowerCase().includes(code)
    return okName && okCode
  })
})

// 当前页数据
const paged = computed(() => {
  const start = (page.current - 1) * page.size
  return filtered.value.slice(start, start + page.size)
})

// 初始化拉取
onMounted(fetchAll)

async function fetchAll() {
  loading.value = true
  try {
    const list = await listSeatTypes()
    full.value = Array.isArray(list) ? list : []
    page.current = 1
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function onSearch() {
  page.current = 1
}
function onReset() {
  q.name = ''
  q.code = ''
  page.current = 1
}

// Dialog & 表单
const dlg = reactive({ visible: false, mode: 'add' }) // add | edit
const formRef = ref(null)
const form = reactive({ id: null, name: '', code: '' })
const saving = ref(false)

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  code: [
    { required: true, message: '请输入代码', trigger: 'blur' },
    { min: 1, max: 6, message: '长度 1~6', trigger: 'blur' }
  ]
}

function openAdd() {
  dlg.mode = 'add'
  Object.assign(form, { id: null, name: '', code: '' })
  dlg.visible = true
  setTimeout(() => formRef.value?.clearValidate(), 0)
}

function openEdit(row) {
  dlg.mode = 'edit'
  Object.assign(form, { id: row.id, name: row.name, code: row.code })
  dlg.visible = true
  setTimeout(() => formRef.value?.clearValidate(), 0)
}

async function onSubmit() {
  await formRef.value?.validate()
  // 简单重复校验（名称+代码不重复）——前端提醒，最终以后端为准
  const dup = full.value.find(x =>
      x.name === form.name && x.code === form.code && x.id !== form.id
  )
  if (dup) {
    ElMessage.warning('已存在相同的名称+代码组合')
    return
  }

  saving.value = true
  try {
    if (dlg.mode === 'add') {
      const created = await addSeatType({ name: form.name, code: form.code })
      ElMessage.success(`新增成功（ID=${created?.id ?? '-' }）`)
    } else {
      await updateSeatType({ id: form.id, name: form.name, code: form.code })
      ElMessage.success('保存成功')
    }
    dlg.visible = false
    await fetchAll()
  } catch (e) {
    ElMessage.error(e?.message || (dlg.mode==='add' ? '新增失败' : '保存失败'))
  } finally {
    saving.value = false
  }
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除舱位类型 #${row.id}（${row.name}/${row.code}）？`, '删除确认', { type: 'warning' })
  } catch { return }
  try {
    await deleteSeatType(row.id)
    ElMessage.success('删除成功')
    // 若当前页删空且非第一页，自动回退一页
    const isLast = paged.value.length === 1 && page.current > 1
    if (isLast) page.current -= 1
    await fetchAll()
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
