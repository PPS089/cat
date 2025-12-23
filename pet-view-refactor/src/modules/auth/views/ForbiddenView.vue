<template>
  <div class="forbidden-container">
    <div class="forbidden-content">
      <div class="error-icon">
        <el-icon :size="80">
          <CloseBold />
        </el-icon>
      </div>
      <h1 class="error-title">{{ t('error.forbidden') || '403 - 禁止访问' }}</h1>
      <p class="error-description">
        {{ reason || t('error.forbiddenDescription') || '您没有权限访问此资源，如需帮助请联系管理员' }}
      </p>

      <div class="error-details" v-if="showDetails">
        <div class="detail-item">
          <span class="label">错误代码：</span>
          <span class="value">{{ errorCode }}</span>
        </div>
        <div class="detail-item" v-if="errorMessage">
          <span class="label">错误信息：</span>
          <span class="value">{{ errorMessage }}</span>
        </div>
      </div>

      <div class="action-buttons">
        <el-button type="primary" size="large" @click="handleBack">
          {{ t('error.goBack') || '返回首页' }}
        </el-button>
        <el-button size="large" @click="handleLogout">
          {{ t('error.logout') || '退出登录' }}
        </el-button>
      </div>

      <div class="contact-admin">
        <p>{{ t('error.contactAdmin') || '如有疑问，请联系系统管理员' }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/app/store/modules/user'
import { CloseBold } from '@element-plus/icons-vue'
import { computed } from 'vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const { t } = useI18n()

const reason = computed(() => {
  return (route.query.reason as string) || ''
})

const errorCode = computed(() => {
  return (route.query.code as string) || '403'
})

const errorMessage = computed(() => {
  return (route.query.message as string) || ''
})

const showDetails = computed(() => {
  return !!(errorCode.value || errorMessage.value)
})

const handleBack = () => {
  const isAdmin = route.path.includes('/admin')
  router.push(isAdmin ? '/admin' : '/user')
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped lang="css">
.forbidden-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  padding: 20px;
}

.forbidden-content {
  background: white;
  border-radius: 12px;
  padding: 60px 40px;
  text-align: center;
  max-width: 500px;
  width: 100%;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.error-icon {
  margin-bottom: 30px;
  color: #e6a23c;
}

.error-icon :deep(.el-icon) {
  display: inline-flex;
}

.error-title {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 16px 0;
}

.error-description {
  font-size: 16px;
  color: #606266;
  margin: 0 0 30px 0;
  line-height: 1.6;
}

.error-details {
  background: #fdf6ec;
  border-radius: 8px;
  padding: 20px;
  margin: 30px 0;
  text-align: left;
  border-left: 4px solid #e6a23c;
}

.detail-item {
  display: flex;
  margin-bottom: 12px;
  font-size: 14px;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-item .label {
  font-weight: 600;
  color: #303133;
  min-width: 100px;
}

.detail-item .value {
  color: #606266;
  word-break: break-all;
}

.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 30px;
}

.action-buttons :deep(.el-button) {
  min-width: 140px;
}

.contact-admin {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  color: #909399;
  font-size: 14px;
}

@media (max-width: 600px) {
  .forbidden-content {
    padding: 40px 20px;
  }

  .error-title {
    font-size: 24px;
  }

  .error-description {
    font-size: 14px;
  }

  .action-buttons {
    flex-direction: column;
  }
}
</style>
