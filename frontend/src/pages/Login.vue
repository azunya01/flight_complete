<template>
  <div class="auth-wrap">
    <el-card class="auth-card" shadow="hover">
      <template #header><b>登录</b></template>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="onSubmit">
        <el-form-item label="用户名" prop="name">
          <el-input v-model.trim="form.name" placeholder="请输入用户名" clearable :disabled="loading" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password :disabled="loading" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" :disabled="loading" @click="onSubmit" style="width:100%">登录</el-button>
        </el-form-item>

        <el-alert v-if="err" :title="err" type="error" show-icon :closable="false" />
        <div class="meta">还没有账号？ <RouterLink to="/register">去注册</RouterLink></div>
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
const err = ref('')

const form = ref({ name: '', password: '' })
const rules = {
  name: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function onSubmit() {
  err.value = ''
  await formRef.value?.validate()
  loading.value = true
  try {
    const r = await http.post('/api/user/login', form.value)
    const { id, name, type, token } = r || {}
    if (!token) throw new Error('未获取到 token')
    store.setAuth({ id, name, type, token })
    localStorage.setItem('userName', name)
    const target = (route.query.redirect ?? (type === 1 ? '/admin' : '/home')) + ''
    ElMessage.success('登录成功')
    await router.replace(target)
  } catch (e) {
    err.value = e?.response?.data?.msg || e?.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-wrap { min-height: 100vh; display: grid; place-items: center; background: var(--el-bg-color); padding: 24px; }
.auth-card { width: 420px; max-width: 92vw; }
.meta { margin-top: 8px; color: var(--el-text-color-secondary) }
</style>
