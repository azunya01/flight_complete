<!-- src/views/user/UserPassengers.vue -->
<template>
  <div class="wrap">
    <!-- 顶部工具条 -->
    <el-row :gutter="12" class="toolbar">
      <el-col :span="12">
        <el-button type="primary" @click="openAdd">新增常用乘客</el-button>
        <el-button :loading="loading" @click="refresh" text>刷新</el-button>
      </el-col>
      <el-col :span="12" style="text-align:right">
        <el-space>
          <el-input
              v-model.trim="query.keyword"
              placeholder="按姓名 / 证件号 / 手机号 搜索"
              clearable
              style="width:260px"
              @keyup.enter="onSearch"
          />
          <el-button type="primary" plain @click="onSearch">搜索</el-button>
          <el-button @click="onReset">清空</el-button>
        </el-space>
      </el-col>
    </el-row>

    <!-- 列表 -->
    <el-card shadow="never">
      <el-table
          :data="records"
          v-loading="loading"
          border
          style="width: 100%"
          :header-cell-style="{ fontWeight: 600 }"
          empty-text="暂无常用乘客，点击上方“新增常用乘客”开始添加"
      >
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="name" label="姓名" min-width="140" />
        <el-table-column prop="gender" label="性别" width="100" />
        <el-table-column prop="idNo" label="证件号" min-width="220" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" min-width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

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

    <!-- 新增 / 编辑 弹窗 -->
    <el-dialog
        v-model="dlg.visible"
        :title="dlg.mode === 'add' ? '新增常用乘客' : '编辑常用乘客'"
        width="560px"
        destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="姓名" prop="name">
          <el-input v-model.trim="form.name" placeholder="请输入乘客姓名" />
        </el-form-item>

        <el-form-item label="性别" prop="gender">
          <el-select v-model="form.gender" placeholder="请选择" style="width:100%">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>

        <el-form-item label="证件号" prop="idNO">
          <el-input v-model.trim="form.idNO" placeholder="请输入身份证号或证件号" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model.trim="form.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-space>
          <el-button @click="dlg.visible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="onSubmit">
            {{ dlg.mode === 'add' ? '提交' : '保存' }}
          </el-button>
        </el-space>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listPassengers,
  addPassenger,
  updatePassenger,
  deletePassenger,
} from '@/api/passenger'

// 查询 / 分页
const loading = ref(false)
const records = ref([])
const total = ref(0)
const page = reactive({ current: 1, size: 10 })
const query = reactive({ keyword: '' })

async function fetchPage(p = 1) {
  loading.value = true
  try {
    page.current = p
    const res = await listPassengers({ keyword: query.keyword, page: page.current, size: page.size })
    // 预期返回 PageResult<PassengerVO>：{ total, records, page, size }
    records.value = res?.records ?? []
    total.value = Number(res?.total ?? 0)
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function onSearch() { fetchPage(1) }
function onReset() { query.keyword = ''; fetchPage(1) }
function refresh() { fetchPage(page.current) }
function onPageChange(p) { fetchPage(p) }

// 弹窗 / 表单
const dlg = reactive({ visible: false, mode: 'add', editId: null }) // mode: add | edit
const formRef = ref(null)
const form = reactive({
  name: '',
  gender: '',
  idNO: '',
  phone: ''
})
const saving = ref(false)

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  idNO: [
    { required: true, message: '请输入证件号', trigger: 'blur' },
    { min: 6, message: '证件号长度不正确', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

function openAdd() {
  dlg.mode = 'add'
  dlg.editId = null
  Object.assign(form, { name: '', gender: '', idNO: '', phone: '' })
  dlg.visible = true
  setTimeout(() => formRef.value?.clearValidate(), 0)
}

function openEdit(row) {
  dlg.mode = 'edit'
  dlg.editId = row.id
  // VO 字段：name / gender / idNo / phone
  Object.assign(form, {
    name: row.name || '',
    gender: row.gender || '',
    idNO: row.idNo || '',   // 注意 VO 是 idNo，DTO 需要 idNO
    phone: row.phone || ''
  })
  dlg.visible = true
  setTimeout(() => formRef.value?.clearValidate(), 0)
}

async function onSubmit() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (dlg.mode === 'add') {
      await addPassenger({
        name: form.name,
        gender: form.gender,
        idNo: form.idNO,
        phone: form.phone
      })
      ElMessage.success('新增成功')
      dlg.visible = false
      await fetchPage(1)
    } else {
      await updatePassenger(dlg.editId, {
        name: form.name,
        gender: form.gender,
        idNo: form.idNO,
        phone: form.phone
      })
      ElMessage.success('保存成功')
      dlg.visible = false
      await fetchPage(page.current)
    }
  } catch (e) {
    ElMessage.error(e?.message || (dlg.mode === 'add' ? '新增失败' : '保存失败'))
  } finally {
    saving.value = false
  }
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除乘客「${row.name}」？`, '删除确认', { type: 'warning' })
  } catch { return }
  try {
    await deletePassenger(row.id)
    ElMessage.success('删除成功')
    const last = records.value.length === 1 && page.current > 1
    if (last) page.current -= 1
    await fetchPage(page.current)
  } catch (e) {
    ElMessage.error(e?.message || '删除失败')
  }
}

onMounted(() => fetchPage(1))
</script>

<style scoped>
.wrap { padding: 16px; }
.toolbar { margin-bottom: 12px; }
.pager { display: flex; justify-content: flex-end; margin-top: 12px; }
</style>
