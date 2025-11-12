<template>
  <div class="auth-wrap">
    <el-card class="auth-card" shadow="hover">
      <template #header><b>注册</b></template>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="onSubmit">
        <el-form-item label="用户名" prop="name">
          <el-input v-model.trim="form.name" placeholder="例如：alice" clearable :disabled="loading" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="不少于 6 位" show-password :disabled="loading" />
        </el-form-item>
        <el-form-item label="身份类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择身份" style="width:100%" :disabled="loading">
            <el-option :value="1" label="管理员" />
            <el-option :value="2" label="用户" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" :disabled="loading" @click="onSubmit" style="width:100%">注册</el-button>
        </el-form-item>

        <el-alert v-if="msg" :title="msg" type="info" show-icon :closable="false" />
        <div class="meta">已有账号？ <RouterLink to="/login">去登录</RouterLink></div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute, RouterLink } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '@/api/http'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route  = useRoute()
const store  = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const msg = ref('')

const form = ref({ name: '', password: '', type: 2 })
const rules = {
  name: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码不少于 6 位', trigger: 'blur' }
  ],
  type: [{ required: true, message: '请选择身份', trigger: 'change' }]
}

async function onSubmit() {
  msg.value = ''
  await formRef.value?.validate()
  loading.value = true
  try {
    const payload = { name: form.value.name, password: form.value.password, type: form.value.type }
    const r = await http.post('/api/user/register', payload)
    const { id, name, type, token } = r || {}

    if (token) {
      store.setAuth({ id, name, type, token })
      localStorage.setItem('userName', name)
      const target = (route.query.redirect ?? (type === 1 ? '/admin' : '/home')) + ''
      ElMessage.success('注册并登录成功')
      await router.replace(target)
    } else {
      ElMessage.success('注册成功，请登录')
      const redirect = (route.query.redirect ?? (form.value.type === 1 ? '/admin' : '/home')) + ''
      await router.replace({ path: '/login', query: { redirect } })
    }
  } catch (e) {
    msg.value = e?.response?.data?.msg || e?.message || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-wrap { min-height: 100vh; display: grid; place-items: center; background: var(--el-bg-color); padding: 24px; }
.auth-card { width: 460px; max-width: 92vw; }
.meta { margin-top: 8px; color: var(--el-text-color-secondary) }
</style>
