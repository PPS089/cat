<template>
  <AuthFormShell
    :title="t('register.createAccount')"
    :subtitle="t('common.appName')"
    :description="t('register.fillInfo')"
    :hero-title="t('register.welcomeTitle') || t('register.createAccount')"
    :hero-description="t('register.welcomeSubtitle') || t('register.subtitle')"
    :hero-image="heroImage"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="0" class="auth-form register-form">
      <el-form-item prop="username">
        <el-input
          v-model="form.username"
          :placeholder="t('register.usernamePlaceholder')"
          size="large"
        >
          <template #prefix>
            <el-icon><User /></el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="phone">
        <el-input
          v-model="form.phone"
          :placeholder="t('register.phonePlaceholder')"
          size="large"
        >
          <template #prefix>
            <el-icon><Phone /></el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="email">
        <el-input
          v-model="form.email"
          :placeholder="t('register.emailPlaceholder')"
          size="large"
        >
          <template #prefix>
            <el-icon><Message /></el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="form.password"
          type="password"
          show-password
          :placeholder="t('register.passwordPlaceholder')"
          size="large"
        >
          <template #prefix>
            <el-icon><Lock /></el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="confirmPassword">
        <el-input
          v-model="form.confirmPassword"
          type="password"
          show-password
          :placeholder="t('register.confirmPasswordPlaceholder')"
          size="large"
        >
          <template #prefix>
            <el-icon><Lock /></el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="large" :loading="loading" @click="onSubmit">
          {{ t('register.btnText') }}
        </el-button>
      </el-form-item>
    </el-form>
    <template #footer>
      <p class="auth-form__footer-text">
        {{ t('register.hasAccount') }}
        <el-link type="primary" :underline="false" @click="goToLogin">
          {{ t('register.loginNow') }}
        </el-link>
      </p>
    </template>
  </AuthFormShell>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElForm, ElMessage } from 'element-plus'
import type { FormRules } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { authService } from '@/modules/auth/services/auth'
import AuthFormShell from '@/modules/auth/components/AuthFormShell.vue'
import { User, Phone, Lock, Message } from '@element-plus/icons-vue'
import heroImage from '@/assets/img/1.jpg'

const { t } = useI18n()
const router = useRouter()
const formRef = ref<InstanceType<typeof ElForm>>()
const loading = ref(false)

const form = reactive({
  username: '',
  phone: '',
  email: '',
  password: '',
  confirmPassword: '',
})

const rules = reactive<FormRules>({
  username: [
    { required: true, message: t('register.usernameRequired'), trigger: 'blur' },
    { min: 1, max: 10, message: t('user.usernameLength'), trigger: 'blur' },
  ],
  phone: [
    { required: true, message: t('register.phoneRequired'), trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: t('register.phoneInvalid'), trigger: 'blur' },
  ],
  email: [
    { required: true, message: t('register.emailRequired'), trigger: 'blur' },
    { type: 'email', message: t('register.emailInvalid'), trigger: 'blur' },
  ],
  password: [
    { required: true, message: t('register.passwordRequired'), trigger: 'blur' },
    { pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)\S{6,12}$/, message: t('register.passwordFormatError'), trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: t('register.confirmPasswordRequired'), trigger: 'blur' },
    {
      validator: (_: unknown, value: string, callback: (err?: Error) => void) => {
        if (value !== form.password) {
          callback(new Error(t('register.passwordMismatch')))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
})

const onSubmit = async () => {
  await formRef.value?.validate(async valid => {
    if (!valid) return
    loading.value = true
    try {
      await authService.register({
        username: form.username.trim(),
        phone: form.phone.trim(),
        email: form.email.trim(),
        password: form.password.trim(),
      })
      ElMessage.success(t('register.success'))
      router.push('/login')
    } catch (error: any) {
      console.error('[Register] register failed', error)
    } finally {
      loading.value = false
    }
  })
}

const goToLogin = () => router.push('/login')
</script>
