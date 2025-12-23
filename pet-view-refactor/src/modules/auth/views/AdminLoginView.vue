<template>
  <AuthFormShell
    title="管理员登录"
    subtitle="后台控制台"
    description="需要管理员权限才能进入后台"
    hero-title="Welcome Admin"
    hero-description="请输入管理员账号登录管理后台"
    :hero-image="heroImage"
    :class="'admin-login-shell'"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="0" class="auth-form login-form">
      <el-form-item prop="username">
        <el-input
          v-model="form.username"
          placeholder="管理员账号"
          size="large"
          autocomplete="username"
        >
          <template #prefix>
            <el-icon><User /></el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="form.password"
          type="password"
          placeholder="密码"
          autocomplete="current-password"
          show-password
          size="large"
        >
          <template #prefix>
            <el-icon><Lock /></el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="large" :loading="loading" class="admin-primary" @click="onSubmit">
          进入后台
        </el-button>
      </el-form-item>
    </el-form>
  </AuthFormShell>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElForm, ElMessage } from 'element-plus'
import { useUserStore } from '@/app/store/modules/user'
import AuthFormShell from '@/modules/auth/components/AuthFormShell.vue'
import { User, Lock } from '@element-plus/icons-vue'
import { consumeAuthNotice } from '@/shared/utils/request'
import heroImage from '@/assets/img/3.jpg'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const formRef = ref<InstanceType<typeof ElForm>>()
const form = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [
    { required: true, message: '请输入管理员账号', trigger: 'blur' },
    { min: 1, max: 10, message: '用户名长度需在1-10字符内', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 12, message: '密码长度需在6-12位', trigger: 'blur' },
  ],
}

onMounted(() => {
  const notice = consumeAuthNotice()
  if (notice === 'expired' || notice === 'refresh_failed') {
    ElMessage.error('登录已过期，请重新登录')
  } else if (notice === 'unauthorized') {
    ElMessage.error('请先登录')
  }
})

const onSubmit = async () => {
  await formRef.value?.validate(async valid => {
    if (!valid) return
    loading.value = true
    try {
      const loginResp = await userStore.login({ username: form.username.trim(), password: form.password.trim() })
      await userStore.fetchProfile()
      const isAdmin = (loginResp.role || '').toUpperCase() === 'ADMIN'
      if (!isAdmin) {
        ElMessage.error('当前账号不是管理员，无法进入后台')
        return
      }
      const rawRedirect = route.query.redirect
      const redirect =
        typeof rawRedirect === 'string' && rawRedirect.startsWith('/admin') ? rawRedirect : '/admin'
      router.push(redirect)
    } catch (error) {
      console.error('[AdminLogin] failed', error)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.auth-form {
  max-width: 420px;
}

.admin-login-shell :deep(.auth-card) {
  background: rgba(12, 22, 31, 0.92);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 28px 70px rgba(0, 0, 0, 0.42);
}

.admin-login-shell :deep(.auth-card::before),
.admin-login-shell :deep(.auth-card::after) {
  background: radial-gradient(circle, rgba(246, 199, 141, 0.32), transparent 72%);
}

.admin-login-shell :deep(.auth-card__media) {
  background: linear-gradient(160deg, rgba(12, 22, 31, 0.8), rgba(12, 22, 31, 0.4));
}

.admin-login-shell :deep(.auth-card__overlay) {
  background: linear-gradient(220deg, rgba(12, 16, 26, 0.4), rgba(12, 16, 26, 0.82));
  color: #eaf0f6;
}

.admin-login-shell :deep(.auth-card__panel) {
  background: rgba(16, 26, 37, 0.92);
  color: #eaf0f6;
}

.admin-login-shell :deep(.auth-panel__header h1) {
  color: #fdf8f1;
}

.admin-login-shell :deep(.auth-panel__header .eyebrow),
.admin-login-shell :deep(.description) {
  color: rgba(234, 240, 246, 0.78);
}

.admin-login-shell :deep(.auth-shell__hero) {
  background: linear-gradient(140deg, rgba(15, 25, 36, 0.92), rgba(17, 38, 55, 0.88));
  color: #eaf0f6;
}

.admin-login-shell :deep(.auth-shell__title) {
  color: #fdf8f1;
}

.admin-primary {
  background: linear-gradient(135deg, #f6c78d, #8ad6b5);
  border: none;
  color: #0b111a;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.3);
}

.admin-primary:hover {
  filter: brightness(1.05);
}
</style>
